package civilink.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

// ============================================================
//  Complaint.java
// ============================================================

/**
 * Complaint – core domain entity.
 * Information Expert: knows its own status and can validate itself.
 */
public class Complaint {

    private int           complaintID;
    private String        trackingID;
    private int           citizenID;
    private int           categoryID;
    private String        description;
    private String        location;
    private LocalDateTime submissionDate;
    private LocalDate     expectedResDate;
    private String        status;
    private boolean       isOverdue;

    // ── No-arg constructor for DAO mapping ────────────────
    public Complaint() {}

    public Complaint(String trackingID, int citizenID, int categoryID,
                     String description, String location, LocalDate expectedResDate) {
        this.trackingID      = trackingID;
        this.citizenID       = citizenID;
        this.categoryID      = categoryID;
        this.description     = description;
        this.location        = location;
        this.submissionDate  = LocalDateTime.now();
        this.expectedResDate = expectedResDate;
        this.status          = "PENDING";
        this.isOverdue       = false;
    }

    // ── Getters ────────────────────────────────────────────
    public int           getComplaintID()    { return complaintID; }
    public String        getTrackingID()     { return trackingID; }
    public int           getCitizenID()      { return citizenID; }
    public int           getCategoryID()     { return categoryID; }
    public String        getDescription()    { return description; }
    public String        getLocation()       { return location; }
    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public LocalDate     getExpectedResDate(){ return expectedResDate; }
    public String        getStatus()         { return status; }
    public boolean       isOverdue()         { return isOverdue; }

    // ── Setters ────────────────────────────────────────────
    public void setComplaintID(int id)               { this.complaintID    = id; }
    public void setTrackingID(String t)              { this.trackingID     = t; }
    public void setCitizenID(int id)                 { this.citizenID      = id; }
    public void setCategoryID(int id)                { this.categoryID     = id; }
    public void setDescription(String d)             { this.description    = d; }
    public void setLocation(String l)                { this.location       = l; }
    public void setSubmissionDate(LocalDateTime dt)  { this.submissionDate = dt; }
    public void setExpectedResDate(LocalDate d)      { this.expectedResDate= d; }
    public void setStatus(String s)                  { this.status         = s; }
    public void setOverdue(boolean o)                { this.isOverdue      = o; }

    /** Convenience: is this complaint eligible to be reopened? */
    public boolean canBeReopened() {
        return "RESOLVED".equals(status);
    }

    @Override
    public String toString() {
        return "[" + trackingID + "] " + status + " | " + location;
    }
}
