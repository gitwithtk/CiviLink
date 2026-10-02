package civilink.model;

// ============================================================
//  Manager.java
// ============================================================
public class Manager extends User {

    private int managerID;  // alias of userID

    public Manager(int userID, String name, String email,
                   String phone, String username, String passwordHash) {
        super(userID, name, email, phone, username, passwordHash);
        this.managerID = userID;
    }

    @Override
    public String getRole() { return "MANAGER"; }

    public int getManagerID() { return managerID; }
}
