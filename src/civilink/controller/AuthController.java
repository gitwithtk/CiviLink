package civilink.controller;

import civilink.database.UserDAO;
import civilink.model.*;

/**
 * AuthController – GRASP Controller for all authentication flows.
 * Routes login, registration, and officer account management.
 * Also satisfies GRASP Creator: creates Citizen and Officer objects.
 */
public class AuthController {

    // GRASP Information Expert: UserDAO knows everything about user persistence
    private final UserDAO userDAO;

    // Singleton-style: one controller per session
    private static AuthController instance;

    private AuthController() {
        this.userDAO = new UserDAO();
    }

    public static synchronized AuthController getInstance() {
        if (instance == null) instance = new AuthController();
        return instance;
    }

    // ────────────────────────────────────────────────────
    //  Login
    // ────────────────────────────────────────────────────

    /**
     * Authenticates any user (citizen/officer/manager).
     * Returns the User object on success, null on failure.
     * UI calls this and casts to the appropriate subtype.
     *
     * Sequence diagram match:
     *   Actor → AuthController.login() → UserDAO.login() → User
     */
    public User login(String username, String password) {
        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            return null;  // input validation
        }
        // In production: hash the password before passing to DAO
        return userDAO.login(username, password);
    }

    // ────────────────────────────────────────────────────
    //  Register Citizen
    // ────────────────────────────────────────────────────

    /**
     * Registers a new citizen account.
     * GRASP Creator: AuthController creates the Citizen object.
     * Returns true if registration succeeded.
     */
    public boolean registerCitizen(String name, String email, String phone,
                                   String username, String password) {
        // ── Input validation (BL layer, not UI) ──────────
        if (name.isBlank() || email.isBlank() ||
            username.isBlank() || password.isBlank()) {
            System.err.println("[AuthController] Registration failed: missing fields.");
            return false;
        }
        if (!email.contains("@")) {
            System.err.println("[AuthController] Registration failed: invalid email.");
            return false;
        }
        if (password.length() < 6) {
            System.err.println("[AuthController] Registration failed: password too short.");
            return false;
        }

        // GRASP Creator: AuthController creates Citizen
        Citizen c = new Citizen(0, name, email, phone, username, password);
        int newID = userDAO.insertCitizen(c);
        return newID > 0;
    }

    // ────────────────────────────────────────────────────
    //  Officer Account Management (Manager use)
    // ────────────────────────────────────────────────────

    /**
     * Creates a new officer account. Called by Manager UI.
     * GRASP Creator: AuthController creates Officer.
     */
    public boolean createOfficerAccount(String name, String email, String phone,
                                        String username, String tempPassword,
                                        String department, Manager callingManager) {
        if (callingManager == null) return false;  // must be authenticated manager

        // Validate fields
        if (name.isBlank() || email.isBlank() ||
            username.isBlank() || tempPassword.isBlank() || department.isBlank()) {
            System.err.println("[AuthController] createOfficerAccount: missing fields.");
            return false;
        }

        Officer o = new Officer(0, name, email, phone, username, tempPassword, department);
        int newID = userDAO.insertOfficer(o);
        return newID > 0;
    }

    /**
     * Deactivates an officer account.
     * Also triggers complaint reassignment (delegated to ComplaintController).
     */
    public boolean deactivateOfficer(int officerID, Manager callingManager) {
        if (callingManager == null) return false;

        // Reassign active complaints first
        ComplaintController.getInstance().reassignComplaintsForDeactivatedOfficer(officerID);

        return userDAO.deactivateOfficer(officerID);
    }

    /**
     * Returns all active officers (for manager's assignment dropdown).
     */
    public java.util.List<Officer> getAllActiveOfficers() {
        return userDAO.getAllActiveOfficers();
    }

    /**
     * Fetches a user by ID (used after login to refresh data).
     */
    public User getUserByID(int userID) {
        return userDAO.getUserByID(userID);
    }

    /**
     * Returns active complaint count for an officer (workload indicator).
     */
    public int getOfficerWorkloadCount(int officerID) {
        return userDAO.getActiveComplaintCountForOfficer(officerID);
    }
}
