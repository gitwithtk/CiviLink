package civilink.database;

import java.sql.*;

/**
 * DBHandler – Singleton (GoF Creational Pattern)
 * Manages a single JDBC connection to SQL Server.
 * All packages access the DB exclusively through this class.
 */
public class DBHandler {

    // ----- Connection settings ------
    private static final String SERVER   = "localhost";
    private static final String PORT     = "1433";
    private static final String DATABASE = "CiviLinkDB";
    private static final String USER     = "sa";
    private static final String PASSWORD = "Admin123!";

    private static final String URL =
        "jdbc:sqlserver://MyLaptop\\SQLEXPRESS;" +
        "databaseName=" + DATABASE +
        ";integratedSecurity=true;" +
        "encrypt=false;trustServerCertificate=true";

    // ------ Singleton instance ------
    private static DBHandler instance;
    private Connection connection;

    /** Private constructor – loads JDBC driver and opens connection. */
    private DBHandler() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(URL);
            System.out.println("[DBHandler] Connected to SQL Server successfully.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("[DBHandler] SQL Server JDBC driver not found.", e);
        } catch (SQLException e) {
            throw new RuntimeException("[DBHandler] Failed to connect: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the single DBHandler instance (thread-safe lazy init).
     */
    public static synchronized DBHandler getInstance() {
        if (instance == null || !instance.isConnected()) {
            instance = new DBHandler();
        }
        return instance;
    }

    /** Returns the live JDBC connection. */
    public Connection getConnection() {
        return connection;
    }

    /** Checks whether the connection is still alive. */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /** Closes the connection (call on application exit). */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DBHandler] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DBHandler] Error closing connection: " + e.getMessage());
        }
    }

    // ── Helper: close resources safely ───────────────────

    public static void close(ResultSet rs) {
        if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
    }

    public static void close(PreparedStatement ps) {
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
    }

    public static void close(ResultSet rs, PreparedStatement ps) {
        close(rs);
        close(ps);
    }
}
