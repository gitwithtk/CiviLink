package civilink.model;

// ============================================================
//  User.java  (abstract base – Abstraction + Inheritance)
// ============================================================

/**
 * Abstract base class for all system users.
 * Demonstrates: Abstraction, Encapsulation, Inheritance.
 */
public abstract class User {

    // ── Protected so subclasses can read, but outside code uses getters ──
    protected int    userID;
    protected String name;
    protected String email;
    protected String phone;
    protected String username;
    protected String passwordHash;
    protected boolean isActive;

    public User(int userID, String name, String email,
                String phone, String username, String passwordHash) {
        this.userID       = userID;
        this.name         = name;
        this.email        = email;
        this.phone        = phone;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.isActive     = true;
    }

    /** Returns the role string – subclasses must implement (Polymorphism). */
    public abstract String getRole();

    // ── Getters ────────────────────────────────────────────
    public int     getUserID()       { return userID; }
    public String  getName()         { return name; }
    public String  getEmail()        { return email; }
    public String  getPhone()        { return phone; }
    public String  getUsername()     { return username; }
    public String  getPasswordHash() { return passwordHash; }
    public boolean isActive()        { return isActive; }

    // ── Setters ────────────────────────────────────────────
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return "[" + getRole() + "] " + name + " (" + username + ")";
    }
}
