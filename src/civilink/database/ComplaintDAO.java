package civilink.database;

import civilink.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ComplaintDAO – Data Access Object for Complaints and ComplaintTimeline.
 */
public class ComplaintDAO {

    private final Connection conn;

    public ComplaintDAO() {
        this.conn = DBHandler.getInstance().getConnection();
    }

    // ────────────────────────────────────────────────────
    //  INSERT
    // ────────────────────────────────────────────────────

    /**
     * Inserts a new complaint. Returns the generated complaintID or -1.
     */
    public int insertComplaint(Complaint c) {
        String sql = "INSERT INTO Complaints " +
                     "(trackingID, citizenID, categoryID, description, location, expectedResDate, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'PENDING')";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getTrackingID());
            ps.setInt(2, c.getCitizenID());
            ps.setInt(3, c.getCategoryID());
            ps.setString(4, c.getDescription());
            ps.setString(5, c.getLocation());
            // expectedResDate may be null if SLA not yet calculated
            if (c.getExpectedResDate() != null)
                ps.setDate(6, Date.valueOf(c.getExpectedResDate()));
            else
                ps.setNull(6, Types.DATE);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] insertComplaint error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return -1;
    }

    /**
     * Adds a timeline entry for a complaint (status change event).
     */
    public boolean insertTimelineEntry(int complaintID, String status, String notes) {
        String sql = "INSERT INTO ComplaintTimeline (complaintID, status, notes) VALUES (?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, complaintID);
            ps.setString(2, status);
            ps.setString(3, notes);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] insertTimelineEntry error: " + e.getMessage());
        } finally {
            DBHandler.close(ps);
        }
        return false;
    }

    // ────────────────────────────────────────────────────
    //  SELECT
    // ────────────────────────────────────────────────────

    /**
     * Fetches a complaint by its trackingID (citizen-facing lookup).
     */
    public Complaint getByTrackingID(String trackingID) {
        String sql = "SELECT * FROM Complaints WHERE trackingID = ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, trackingID);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] getByTrackingID error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return null;
    }

    /**
     * Returns all complaints assigned to a specific officer that are not resolved/closed.
     */
    public List<Complaint> getAssignedToOfficer(int officerID) {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.* FROM Complaints c " +
                     "JOIN Assignments a ON c.complaintID = a.complaintID " +
                     "WHERE a.officerID = ? AND c.status NOT IN ('RESOLVED','CLOSED') " +
                     "ORDER BY c.submissionDate ASC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, officerID);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] getAssignedToOfficer error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }

    /**
     * Returns all complaints with status PENDING (unassigned queue).
     */
    public List<Complaint> getUnassignedComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT * FROM Complaints WHERE status = 'PENDING' ORDER BY submissionDate ASC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] getUnassignedComplaints error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }

    /**
     * Returns all complaints (manager view-all).
     */
    public List<Complaint> getAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT * FROM Complaints ORDER BY submissionDate DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] getAllComplaints error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }

    /**
     * Returns all overdue complaints (isOverdue = 1).
     */
    public List<Complaint> getOverdueComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT * FROM Complaints WHERE isOverdue = 1 " +
                     "AND status NOT IN ('RESOLVED','CLOSED') " +
                     "ORDER BY expectedResDate ASC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] getOverdueComplaints error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }

    /**
     * Returns all timeline entries for a specific complaint.
     */
    public List<ComplaintTimeline> getTimeline(int complaintID) {
        List<ComplaintTimeline> list = new ArrayList<>();
        String sql = "SELECT * FROM ComplaintTimeline WHERE complaintID = ? ORDER BY updateTime ASC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, complaintID);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ComplaintTimeline(
                    rs.getInt("timelineID"),
                    rs.getInt("complaintID"),
                    rs.getString("status"),
                    rs.getTimestamp("updateTime").toLocalDateTime(),
                    rs.getString("notes")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] getTimeline error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }

    // ────────────────────────────────────────────────────
    //  UPDATE
    // ────────────────────────────────────────────────────

    /**
     * Updates the status of a complaint.
     */
    public boolean updateStatus(int complaintID, String newStatus) {
        String sql = "UPDATE Complaints SET status = ? WHERE complaintID = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, newStatus);
            ps.setInt(2, complaintID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] updateStatus error: " + e.getMessage());
        } finally {
            DBHandler.close(ps);
        }
        return false;
    }

    /**
     * Flags all active complaints whose expectedResDate has passed.
     * Called by the scheduled overdue check.
     */
    public int flagOverdueComplaints() {
        String sql = "UPDATE Complaints SET isOverdue = 1 " +
                     "WHERE expectedResDate < CAST(GETDATE() AS DATE) " +
                     "AND status NOT IN ('RESOLVED','CLOSED') " +
                     "AND isOverdue = 0";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            return ps.executeUpdate();  // returns count of newly flagged rows
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] flagOverdueComplaints error: " + e.getMessage());
        } finally {
            DBHandler.close(ps);
        }
        return 0;
    }

    /**
     * Reassigns all active complaints of a deactivated officer back to PENDING.
     */
    public boolean reassignComplaintsToUnassigned(int officerID) {
        String sql = "UPDATE Complaints SET status = 'PENDING' " +
                     "WHERE complaintID IN " +
                     "  (SELECT complaintID FROM Assignments WHERE officerID = ?) " +
                     "AND status NOT IN ('RESOLVED','CLOSED')";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, officerID);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[ComplaintDAO] reassignComplaintsToUnassigned error: " + e.getMessage());
        } finally {
            DBHandler.close(ps);
        }
        return false;
    }

    // ────────────────────────────────────────────────────
    //  Private helper
    // ────────────────────────────────────────────────────

    private Complaint mapRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintID(rs.getInt("complaintID"));
        c.setTrackingID(rs.getString("trackingID"));
        c.setCitizenID(rs.getInt("citizenID"));
        c.setCategoryID(rs.getInt("categoryID"));
        c.setDescription(rs.getString("description"));
        c.setLocation(rs.getString("location"));
        c.setSubmissionDate(rs.getTimestamp("submissionDate").toLocalDateTime());
        Date expDate = rs.getDate("expectedResDate");
        if (expDate != null) c.setExpectedResDate(expDate.toLocalDate());
        c.setStatus(rs.getString("status"));
        c.setOverdue(rs.getBoolean("isOverdue"));
        return c;
    }
}
