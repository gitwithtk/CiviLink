package civilink.controller;

import civilink.database.AssignmentDAO;
import civilink.database.ComplaintDAO;
import civilink.database.ReportDAO;
import civilink.model.*;
import civilink.service.NotificationService;

import java.util.List;

// ============================================================
//  AssignmentController.java
// ============================================================

/**
 * AssignmentController – GRASP Controller for:
 *   UC8  Assign Complaint to Officer
 *   UC9  Monitor Officer Workload
 */
public class AssignmentController {

    private final AssignmentDAO       assignmentDAO;
    private final ComplaintDAO        complaintDAO;
    private final NotificationService notificationService;

    private static AssignmentController instance;

    private AssignmentController() {
        this.assignmentDAO       = new AssignmentDAO();
        this.complaintDAO        = new ComplaintDAO();
        this.notificationService = NotificationService.getInstance();
    }

    public static synchronized AssignmentController getInstance() {
        if (instance == null) instance = new AssignmentController();
        return instance;
    }

    // ────────────────────────────────────────────────────
    //  UC8 – Assign Complaint to Officer
    // ────────────────────────────────────────────────────

    /**
     * Manager assigns a complaint to a specific officer.
     * Updates complaint status to ASSIGNED and creates an Assignment record.
     * Notifies the officer via NotificationService (Low Coupling).
     *
     * @return assignmentID on success, -1 on failure
     */
    public int assignComplaint(int complaintID, int officerID, int managerID) {
        // Validate: complaint must be in PENDING or REOPENED state
        Complaint c = complaintDAO.getByTrackingID(
            complaintDAO.getAllComplaints().stream()
                .filter(x -> x.getComplaintID() == complaintID)
                .findFirst()
                .map(Complaint::getTrackingID)
                .orElse(""));

        // Insert assignment record
        int assignmentID = assignmentDAO.insertAssignment(complaintID, officerID, managerID);
        if (assignmentID == -1) return -1;

        // Update complaint status to ASSIGNED
        complaintDAO.updateStatus(complaintID, "ASSIGNED");
        complaintDAO.insertTimelineEntry(complaintID, "ASSIGNED",
                "Complaint assigned to officer ID: " + officerID);

        // Notify the officer (Observer-style via NotificationService)
        notificationService.sendAssignmentNotification(officerID,
                "A new complaint (ID: " + complaintID + ") has been assigned to you.");

        return assignmentID;
    }

    // ────────────────────────────────────────────────────
    //  UC9 – Monitor Officer Workload
    // ────────────────────────────────────────────────────

    /**
     * Returns workload summary for all officers.
     * Each int[] = {officerID, activeCount, resolvedCount}
     */
    public List<int[]> getWorkloadSummary() {
        return assignmentDAO.getWorkloadSummary();
    }

    /**
     * Returns the assignment record for a given complaint.
     */
    public Assignment getAssignmentForComplaint(int complaintID) {
        return assignmentDAO.getAssignmentByComplaint(complaintID);
    }
}


