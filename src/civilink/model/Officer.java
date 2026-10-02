package civilink.model;

// ============================================================
//  Officer.java
// ============================================================
public class Officer extends User {

    private int    officerID;   // same as userID
    private String department;

    public Officer(int userID, String name, String email,
                   String phone, String username, String passwordHash,
                   String department) {
        super(userID, name, email, phone, username, passwordHash);
        this.officerID  = userID;
        this.department = department;
    }

    @Override
    public String getRole() { return "OFFICER"; }

    public int    getOfficerID()  { return officerID; }
    public String getDepartment() { return department; }
    public void   setDepartment(String d) { this.department = d; }

    @Override
    public String toString() {
        return super.toString() + " | Dept: " + department;
    }
}
