package civilink.model;

import java.time.LocalDateTime;

// ============================================================
//  ComplaintTimeline.java
// ============================================================

/** Represents one status-change event on a complaint's lifecycle. */
public class ComplaintTimeline {

    private int           timelineID;
    private int           complaintID;
    private String        status;
    private LocalDateTime updateTime;
    private String        notes;

    public ComplaintTimeline(int timelineID, int complaintID,
                             String status, LocalDateTime updateTime, String notes) {
        this.timelineID  = timelineID;
        this.complaintID = complaintID;
        this.status      = status;
        this.updateTime  = updateTime;
        this.notes       = notes;
    }

    public int           getTimelineID()  { return timelineID; }
    public int           getComplaintID() { return complaintID; }
    public String        getStatus()      { return status; }
    public LocalDateTime getUpdateTime()  { return updateTime; }
    public String        getNotes()       { return notes; }

    @Override
    public String toString() {
        return updateTime + "  →  " + status + (notes != null ? "  | " + notes : "");
    }
}
