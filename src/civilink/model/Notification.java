package civilink.model;

import java.time.LocalDateTime;

public class Notification {

    private int           notificationID;
    private int           userID;
    private String        message;
    private LocalDateTime date;
    private String        status;   // UNREAD | READ

    public Notification(int notificationID, int userID,
                        String message, LocalDateTime date, String status) {
        this.notificationID = notificationID;
        this.userID         = userID;
        this.message        = message;
        this.date           = date;
        this.status         = status;
    }

    public int           getNotificationID() { return notificationID; }
    public int           getUserID()         { return userID; }
    public String        getMessage()        { return message; }
    public LocalDateTime getDate()           { return date; }
    public String        getStatus()         { return status; }
    public void          setStatus(String s) { this.status = s; }

    @Override
    public String toString() { return "[" + date + "] " + message; }
}

