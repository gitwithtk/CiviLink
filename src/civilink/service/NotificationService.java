package civilink.service;

import civilink.database.NotificationDAO;

/**
 * NotificationService – GoF-inspired Service / Observer pattern.
 * Decoupled from all controllers (Low Coupling GRASP).
 * Both ComplaintController and AssignmentController use this
 * without knowing the persistence details.
 *
 * Pattern: Singleton (one shared notification dispatcher)
 */
public class NotificationService {

    private final NotificationDAO notificationDAO;
    private static NotificationService instance;

    private NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    public static synchronized NotificationService getInstance() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }

    /**
     * Sends a general notification to a user.
     */
    public void sendNotification(int userID, String message) {
        boolean ok = notificationDAO.insertNotification(userID, message);
        if (ok) {
            System.out.println("[NotificationService] Sent to user " + userID + ": " + message);
        } else {
            System.err.println("[NotificationService] Failed to send notification to user " + userID);
        }
    }

    /**
     * Sends a complaint-assignment notification to an officer.
     */
    public void sendAssignmentNotification(int officerID, String message) {
        sendNotification(officerID, "ASSIGNMENT: " + message);
    }

    /**
     * Returns all unread notifications for a user (called on dashboard load).
     */
    public java.util.List<civilink.model.Notification> getUnreadNotifications(int userID) {
        return notificationDAO.getUnreadForUser(userID);
    }

    /**
     * Marks a notification as read.
     */
    public boolean markRead(int notificationID) {
        return notificationDAO.markAsRead(notificationID);
    }
}
