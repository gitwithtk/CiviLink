package civilink.model;

import java.time.LocalDateTime;

// ============================================================
//  Assignment.java
// ============================================================

/** Links a Complaint to an Officer via a Manager action. */
public class Assignment {

    private int           assignmentID;
    private int           complaintID;
    private int           officerID;
    private int           managerID;
    private LocalDateTime assignedDate;

    public Assignment(int assignmentID, int complaintID,
                      int officerID, int managerID, LocalDateTime assignedDate) {
        this.assignmentID = assignmentID;
        this.complaintID  = complaintID;
        this.officerID    = officerID;
        this.managerID    = managerID;
        this.assignedDate = assignedDate;
    }

    public int           getAssignmentID() { return assignmentID; }
    public int           getComplaintID()  { return complaintID; }
    public int           getOfficerID()    { return officerID; }
    public int           getManagerID()    { return managerID; }
    public LocalDateTime getAssignedDate() { return assignedDate; }
}
