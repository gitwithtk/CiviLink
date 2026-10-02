package civilink.main;

import civilink.database.DBHandler;
import civilink.controller.ComplaintController;
import civilink.ui.SplashScreen;
import javafx.application.Application;

/**
 * Main – CiviLink entry point.
 * Launches SplashScreen (2s animated) → Landing → Citizen or Staff portal.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║   CiviLink MIS  –  Starting...   ║");
        System.out.println("╚══════════════════════════════════╝");

        try {
            DBHandler db = DBHandler.getInstance();
            if (!db.isConnected()) { System.err.println("[Main] DB error."); System.exit(1); }
            System.out.println("[Main] Database connection OK.");
        } catch (Exception e) { System.err.println("[Main] DB error: " + e.getMessage()); System.exit(1); }

        try {
            int f = ComplaintController.getInstance().checkAndFlagOverdue();
            System.out.println("[Main] Overdue flagged: " + f);
        } catch (Exception e) { System.err.println("[Main] Overdue check: " + e.getMessage()); }

        System.out.println("[Main] Launching...");
        Application.launch(SplashScreen.class, args);

        DBHandler.getInstance().closeConnection();
    }
}
