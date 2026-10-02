package civilink.database;

import civilink.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {

    private final Connection conn;

    public AssignmentDAO() {
        this.conn = DBHandler.getInstance().getConnection();
    }

    public int insertAssignment(int complaintID, int officerID, int managerID) {
        String sql = "INSERT INTO Assignments (complaintID, officerID, managerID) VALUES (?, ?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, complaintID);
            ps.setInt(2, officerID);
            ps.setInt(3, managerID);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] insertAssignment error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return -1;
    }

    public Assignment getAssignmentByComplaint(int complaintID) {
        String sql = "SELECT TOP 1 * FROM Assignments WHERE complaintID = ? ORDER BY assignedDate DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, complaintID);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new Assignment(
                    rs.getInt("assignmentID"),
                    rs.getInt("complaintID"),
                    rs.getInt("officerID"),
                    rs.getInt("managerID"),
                    rs.getTimestamp("assignedDate").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] getAssignmentByComplaint error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return null;
    }

    public int getCurrentAssignedOfficerID(int complaintID) {
        // Pick the most recent assignment row for this complaint.
        String sql = "SELECT TOP 1 officerID FROM Assignments " +
                     "WHERE complaintID = ? ORDER BY assignedDate DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, complaintID);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("officerID");
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] getCurrentAssignedOfficerID error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return -1;   // -1 means complaint has no assignment at all (still PENDING/REOPENED, unassigned)
    }

    public List<int[]> getWorkloadSummary() {
        List<int[]> list = new ArrayList<>();
        String sql =
            "SELECT a.officerID, " +
            "  SUM(CASE WHEN c.status NOT IN ('RESOLVED','CLOSED') THEN 1 ELSE 0 END) AS active, " +
            "  SUM(CASE WHEN c.status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved " +
            "FROM Assignments a JOIN Complaints c ON a.complaintID = c.complaintID " +
            "GROUP BY a.officerID";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new int[]{
                    rs.getInt("officerID"),
                    rs.getInt("active"),
                    rs.getInt("resolved")
                });
            }
        } catch (SQLException e) {
            System.err.println("[AssignmentDAO] getWorkloadSummary error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }
}