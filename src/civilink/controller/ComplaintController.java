package civilink.controller;

import civilink.database.AssignmentDAO;
import civilink.database.ComplaintDAO;
import civilink.model.*;
import civilink.service.NotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * ComplaintController – GRASP Controller for all complaint-related use cases.
 *
 * Use cases handled:
 *   UC1  Submit Complaint
 *   UC2  View Complaint Status
 *   UC3  View Complaint Timeline
 *   UC4  Reopen Complaint
 *   UC7  Update Complaint Status (officer)
 *   UC10 Monitor and Flag Overdue Complaints
 *
 * GRASP patterns applied:
 *   Controller        – routes all complaint actions
 *   Information Expert– delegates persistence to ComplaintDAO
 *   Creator           – creates Complaint objects
 *   Low Coupling      – uses NotificationService (separate class) for alerts
 *   High Cohesion     – only complaint-related logic lives here
 */
public class ComplaintController {

    private final ComplaintDAO        complaintDAO;
    private final AssignmentDAO       assignmentDAO;   // ← NEW: needed for ownership check
    private final NotificationService notificationService;

    private static ComplaintController instance;

    private ComplaintController() {
        this.complaintDAO        = new ComplaintDAO();
        this.assignmentDAO       = new AssignmentDAO();  // ← NEW
        this.notificationService = NotificationService.getInstance();
    }

    public static synchronized ComplaintController getInstance() {
        if (instance == null) instance = new ComplaintController();
        return instance;
    }

    // ────────────────────────────────────────────────────
    //  UC1 – Submit Complaint
    // ────────────────────────────────────────────────────

    /**
     * Creates a new complaint and persists it.
     * GRASP Creator: ComplaintController creates Complaint.
     * Generates unique trackingID automatically.
     *
     * @return generated trackingID on success, null on failure
     */
    public String createComplaint(int citizenID, int categoryID,
                                  String description, String location,
                                  int slaDefaultDays) {
        if (description == null || description.isBlank()) {
            System.err.println("[ComplaintController] Description cannot be empty.");
            return null;
        }
        if (location == null || location.isBlank()) {
            System.err.println("[ComplaintController] Location cannot be empty.");
            return null;
        }

        String trackingID = "CVL-" + UUID.randomUUID()
                                         .toString()
                                         .replace("-", "")
                                         .substring(0, 8)
                                         .toUpperCase();

        LocalDate expectedResDate = LocalDate.now().plusDays(slaDefaultDays);

        Complaint complaint = new Complaint(trackingID, citizenID, categoryID,
                                            description, location, expectedResDate);

        int newID = complaintDAO.insertComplaint(complaint);
        if (newID == -1) return null;

        complaint.setComplaintID(newID);

        complaintDAO.insertTimelineEntry(newID, "PENDING",
                "Complaint submitted. Awaiting assignment.");

        notificationService.sendNotification(citizenID,
                "Your complaint has been submitted. Tracking ID: " + trackingID);

        return trackingID;
    }

    // ────────────────────────────────────────────────────
    //  UC2 – View Complaint Status
    // ────────────────────────────────────────────────────

    public Complaint trackComplaint(String trackingID) {
        if (trackingID == null || trackingID.isBlank()) return null;
        return complaintDAO.getByTrackingID(trackingID.trim().toUpperCase());
    }

    // ────────────────────────────────────────────────────
    //  UC3 – View Complaint Timeline
    // ────────────────────────────────────────────────────

    public List<ComplaintTimeline> getTimeline(int complaintID) {
        return complaintDAO.getTimeline(complaintID);
    }

    // ────────────────────────────────────────────────────
    //  UC4 – Reopen Complaint
    // ────────────────────────────────────────────────────

    public boolean reopenComplaint(String trackingID, String reason, int citizenID) {
        if (reason == null || reason.isBlank()) {
            System.err.println("[ComplaintController] Reopen reason is required.");
            return false;
        }

        Complaint c = complaintDAO.getByTrackingID(trackingID);
        if (c == null) {
            System.err.println("[ComplaintController] Complaint not found: " + trackingID);
            return false;
        }
        if (!c.canBeReopened()) {
            System.err.println("[ComplaintController] Cannot reopen – status is: " + c.getStatus());
            return false;
        }

        boolean updated = complaintDAO.updateStatus(c.getComplaintID(), "REOPENED");
        if (updated) {
            complaintDAO.insertTimelineEntry(c.getComplaintID(), "REOPENED",
                    "Citizen reopened: " + reason);

            notificationService.sendNotification(citizenID,
                    "Your complaint " + trackingID + " has been reopened.");
        }
        return updated;
    }

    // ────────────────────────────────────────────────────
    //  UC6 – View Assigned Complaints (Officer)
    // ────────────────────────────────────────────────────

    public List<Complaint> getComplaintsForOfficer(int officerID) {
        return complaintDAO.getAssignedToOfficer(officerID);
    }

    // ────────────────────────────────────────────────────
    //  UC7 – Update Complaint Status (Officer)
    // ────────────────────────────────────────────────────
    public boolean updateComplaintStatus(int complaintID, String newStatus,
                                         String notes, int officerID) {

        // ── Guard 1: resolution notes check (unchanged) ──
        if ("RESOLVED".equals(newStatus) && (notes == null || notes.isBlank())) {
            System.err.println("[ComplaintController] Resolution notes required.");
            return false;
        }

        Complaint c = complaintDAO.getByTrackingID(
            complaintDAO.getAllComplaints().stream()
                .filter(x -> x.getComplaintID() == complaintID)
                .findFirst()
                .map(Complaint::getTrackingID)
                .orElse(""));

        if (c != null) {
            String currentStatus = c.getStatus();
            if ("PENDING".equals(currentStatus) || "REOPENED".equals(currentStatus)) {
                System.err.println("[ComplaintController] Update blocked: complaint status is '"
                    + currentStatus + "'. Manager must assign an officer first.");
                return false;
            }
        }

        int currentAssignedOfficer = assignmentDAO.getCurrentAssignedOfficerID(complaintID);

        if (currentAssignedOfficer == -1) {
            // No assignment exists at all → complaint is unassigned, block update
            System.err.println("[ComplaintController] Update blocked: complaint #"
                + complaintID + " has no active assignment.");
            return false;
        }

        if (currentAssignedOfficer != officerID) {
            // Officer is NOT the current assignee (e.g. O1 after O2 was assigned)
            System.err.println("[ComplaintController] Update blocked: officer #"
                + officerID + " is not the current assignee for complaint #" + complaintID
                + ". Current assignee is officer #" + currentAssignedOfficer + ".");
            return false;
        }

        // ── All guards passed — perform update ───────────
        boolean updated = complaintDAO.updateStatus(complaintID, newStatus);
        if (updated) {
            complaintDAO.insertTimelineEntry(complaintID, newStatus, notes);

            notificationService.sendNotification(officerID,
                    "Complaint #" + complaintID + " status updated to: " + newStatus);
        }
        return updated;
    }

    // ────────────────────────────────────────────────────
    //  UC8 – Unassigned queue (for Manager assignment)
    // ────────────────────────────────────────────────────

    public List<Complaint> getUnassignedComplaints() {
        return complaintDAO.getUnassignedComplaints();
    }

    public List<Complaint> getAllComplaints() {
        return complaintDAO.getAllComplaints();
    }

    // ────────────────────────────────────────────────────
    //  UC10 – Flag Overdue Complaints
    // ────────────────────────────────────────────────────

    public int checkAndFlagOverdue() {
        int count = complaintDAO.flagOverdueComplaints();
        System.out.println("[ComplaintController] Overdue complaints flagged: " + count);
        return count;
    }

    public List<Complaint> getOverdueComplaints() {
        return complaintDAO.getOverdueComplaints();
    }

    // ────────────────────────────────────────────────────
    //  Officer deactivation helper
    // ────────────────────────────────────────────────────

    public void reassignComplaintsForDeactivatedOfficer(int officerID) {
        complaintDAO.reassignComplaintsToUnassigned(officerID);
    }

    // ────────────────────────────────────────────────────
    //  NEW HELPER – used by OfficerDashboard UI
    // ────────────────────────────────────────────────────

    /**
     * Returns true only if the given officer is allowed to update this complaint.
     *
     * The UI calls this to decide whether to ENABLE or DISABLE the
     * "Update Status" button before the officer even clicks it.
     *
     * Rules:
     *  • Complaint must NOT be in PENDING or REOPENED state
     *    (those require a fresh manager assignment first).
     *  • The officer must be the most recently assigned officer.
     */
    public boolean canOfficerUpdate(int complaintID, int officerID) {
        // Re-fetch fresh complaint status
        Complaint c = complaintDAO.getAllComplaints().stream()
            .filter(x -> x.getComplaintID() == complaintID)
            .findFirst()
            .orElse(null);

        if (c == null) return false;

        String status = c.getStatus();
        if ("PENDING".equals(status) || "REOPENED".equals(status)
                || "RESOLVED".equals(status) || "CLOSED".equals(status)) {
            return false;
        }

        int currentAssignee = assignmentDAO.getCurrentAssignedOfficerID(complaintID);
        return currentAssignee == officerID;
    }
}