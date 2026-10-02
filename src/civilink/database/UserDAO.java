package civilink.database;

import civilink.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO – Data Access Object for Users, Officers, and Managers.
 * Contains all SQL queries related to the Users table.
 */
public class UserDAO {

    private final Connection conn;

    public UserDAO() {
        this.conn = DBHandler.getInstance().getConnection();
    }

    // ────────────────────────────────────────────────────
    //  INSERT
    // ────────────────────────────────────────────────────

    /**
     * Inserts a new user (citizen) into the Users table.
     * Returns the generated userID, or -1 on failure.
     */
    public int insertCitizen(Citizen c) {
        String sql = "INSERT INTO Users (name, email, phone, username, passwordHash, role) " +
                     "VALUES (?, ?, ?, ?, ?, 'CITIZEN')";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getName());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getUsername());
            ps.setString(5, c.getPasswordHash());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[UserDAO] insertCitizen error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return -1;
    }

    /**
     * Inserts a new officer into Users + Officers tables.
     */
    public int insertOfficer(Officer o) {
        String sqlUser = "INSERT INTO Users (name, email, phone, username, passwordHash, role) " +
                         "VALUES (?, ?, ?, ?, ?, 'OFFICER')";
        String sqlOfficer = "INSERT INTO Officers (officerID, department) VALUES (?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            // Insert into Users
            ps = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, o.getName());
            ps.setString(2, o.getEmail());
            ps.setString(3, o.getPhone());
            ps.setString(4, o.getUsername());
            ps.setString(5, o.getPasswordHash());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            int newID = -1;
            if (rs.next()) newID = rs.getInt(1);
            DBHandler.close(rs, ps);

            if (newID == -1) return -1;

            // Insert into Officers
            ps = conn.prepareStatement(sqlOfficer);
            ps.setInt(1, newID);
            ps.setString(2, o.getDepartment());
            ps.executeUpdate();
            return newID;

        } catch (SQLException e) {
            System.err.println("[UserDAO] insertOfficer error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return -1;
    }

    // ────────────────────────────────────────────────────
    //  SELECT
    // ────────────────────────────────────────────────────

    /**
     * Authenticates a user by username + password.
     * Returns the matching User (Citizen/Officer/Manager), or null.
     */
    public User login(String username, String passwordHash) {
        String sql = "SELECT u.*, o.department FROM Users u " +
                     "LEFT JOIN Officers o ON u.userID = o.officerID " +
                     "WHERE u.username = ? AND u.passwordHash = ? AND u.isActive = 1";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRowToUser(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] login error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return null;
    }

    /**
     * Returns all active officers (for manager assignment screen).
     */
    public List<Officer> getAllActiveOfficers() {
        List<Officer> list = new ArrayList<>();
        String sql = "SELECT u.*, o.department FROM Users u " +
                     "JOIN Officers o ON u.userID = o.officerID " +
                     "WHERE u.isActive = 1";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add((Officer) mapRowToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] getAllActiveOfficers error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }

    /**
     * Returns a User by ID.
     */
    public User getUserByID(int userID) {
        String sql = "SELECT u.*, o.department FROM Users u " +
                     "LEFT JOIN Officers o ON u.userID = o.officerID " +
                     "WHERE u.userID = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            if (rs.next()) return mapRowToUser(rs);
        } catch (SQLException e) {
            System.err.println("[UserDAO] getUserByID error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return null;
    }

    // ────────────────────────────────────────────────────
    //  UPDATE
    // ────────────────────────────────────────────────────

    /**
     * Deactivates an officer account (sets isActive = 0).
     */
    public boolean deactivateOfficer(int officerID) {
        String sql = "UPDATE Users SET isActive = 0 WHERE userID = ? AND role = 'OFFICER'";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, officerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] deactivateOfficer error: " + e.getMessage());
        } finally {
            DBHandler.close(ps);
        }
        return false;
    }

    /**
     * Counts how many active complaints are assigned to an officer (for workload).
     */
    public int getActiveComplaintCountForOfficer(int officerID) {
        String sql = "SELECT COUNT(*) FROM Assignments a " +
                     "JOIN Complaints c ON a.complaintID = c.complaintID " +
                     "WHERE a.officerID = ? AND c.status NOT IN ('RESOLVED','CLOSED')";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, officerID);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[UserDAO] getActiveComplaintCountForOfficer error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return 0;
    }

    // ────────────────────────────────────────────────────
    //  Private helper
    // ────────────────────────────────────────────────────

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int    id    = rs.getInt("userID");
        String name  = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String uname = rs.getString("username");
        String pwd   = rs.getString("passwordHash");
        String role  = rs.getString("role");

        switch (role) {
            case "CITIZEN":
                Citizen c = new Citizen(id, name, email, phone, uname, pwd);
                return c;
            case "OFFICER":
                String dept = rs.getString("department");
                return new Officer(id, name, email, phone, uname, pwd, dept);
            case "MANAGER":
                return new Manager(id, name, email, phone, uname, pwd);
            default:
                throw new SQLException("Unknown role: " + role);
        }
    }
}
