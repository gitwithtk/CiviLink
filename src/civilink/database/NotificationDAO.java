package civilink.database;

import civilink.model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private final Connection conn;

    public NotificationDAO() {
        this.conn = DBHandler.getInstance().getConnection();
    }

    public boolean insertNotification(int userID, String message) {
        String sql = "INSERT INTO Notifications (userID, message) VALUES (?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userID);
            ps.setString(2, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] insertNotification error: " + e.getMessage());
        } finally {
            DBHandler.close(ps);
        }
        return false;
    }

    public List<Notification> getUnreadForUser(int userID) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notifications WHERE userID = ? AND status = 'UNREAD' " +
                     "ORDER BY date DESC";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userID);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Notification(
                    rs.getInt("notificationID"),
                    rs.getInt("userID"),
                    rs.getString("message"),
                    rs.getTimestamp("date").toLocalDateTime(),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] getUnreadForUser error: " + e.getMessage());
        } finally {
            DBHandler.close(rs, ps);
        }
        return list;
    }

    public boolean markAsRead(int notificationID) {
        String sql = "UPDATE Notifications SET status = 'READ' WHERE notificationID = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setInt(1, notificationID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] markAsRead error: " + e.getMessage());
        } finally {
            DBHandler.close(ps);
        }
        return false;
    }
}