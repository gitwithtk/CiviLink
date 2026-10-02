package civilink.controller;

import civilink.database.ReportDAO;

// ============================================================
//  ReportController.java
// ============================================================

/**
 * ReportController – GRASP Controller for:
 *   UC11 Generate Performance Reports
 */
public class ReportController {

    private final ReportDAO reportDAO;

    private static ReportController instance;

    private ReportController() {
        this.reportDAO = new ReportDAO();
    }

    public static synchronized ReportController getInstance() {
        if (instance == null) instance = new ReportController();
        return instance;
    }

    /**
     * Generates a performance report for the given date range.
     * Returns a formatted string (displayed in UI TextArea or printed).
     *
     * @param fromDate  format: "yyyy-MM-dd"
     * @param toDate    format: "yyyy-MM-dd"
     * @param managerID the requesting manager's ID
     */
    public String generateReport(String fromDate, String toDate, int managerID) {
        double[] stats = reportDAO.getPerformanceStats(fromDate, toDate);

        // Persist report record
        reportDAO.insertReport(managerID, "PERFORMANCE");

        // Format output
        return String.format(
            "====================================\n" +
            "  CiviLink Performance Report\n" +
            "  Period: %s  →  %s\n" +
            "====================================\n" +
            "  Total Complaints Received : %.0f\n" +
            "  Total Resolved            : %.0f\n" +
            "  Overdue Complaints        : %.0f\n" +
            "  Avg. Resolution Time      : %.1f days\n" +
            "====================================\n",
            fromDate, toDate,
            stats[0], stats[1], stats[2], stats[3]
        );
    }
}
