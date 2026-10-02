package civilink.model;

// ============================================================
//  Citizen.java
// ============================================================
public class Citizen extends User {

    private int citizenID;  // same as userID, alias for clarity

    public Citizen(int userID, String name, String email,
                   String phone, String username, String passwordHash) {
        super(userID, name, email, phone, username, passwordHash);
        this.citizenID = userID;
    }

    @Override
    public String getRole() { return "CITIZEN"; }

    public int getCitizenID() { return citizenID; }
}
