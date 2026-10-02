package civilink.database;

import java.sql.*;

public class ReportDAO {

    private final Connection conn;

    public ReportDAO() {
        this.conn = DBHandler.getInstance().getConnection();
    }

    public int insertReport(int managerID, String reportType) {
        String sql = "INSERT INTO Reports (managerID, reportType) VALUES (?, ?)";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, managerID);
            ps.setString(2, reportType);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ReportDAO] insertReport error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return -1;
    }

    public double[] getPerformanceStats(String fromDate, String toDate) {
        String sql =
            "SELECT " +
            "  COUNT(*) AS total, " +
            "  SUM(CASE WHEN status = 'RESOLVED' THEN 1 ELSE 0 END) AS resolved, " +
            "  SUM(CASE WHEN isOverdue = 1 THEN 1 ELSE 0 END) AS overdue, " +
            "  AVG(CASE WHEN status = 'RESOLVED' " +
            "      THEN DATEDIFF(day, submissionDate, GETDATE()) ELSE NULL END) AS avgDays " +
            "FROM Complaints " +
            "WHERE submissionDate BETWEEN ? AND ?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new double[]{
                    rs.getInt("total"),
                    rs.getInt("resolved"),
                    rs.getInt("overdue"),
                    rs.getDouble("avgDays")
                };
            }
        } catch (SQLException e) {
            System.err.println("[ReportDAO] getPerformanceStats error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return new double[]{0, 0, 0, 0};
    }
}