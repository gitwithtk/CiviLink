package civilink.model;

import java.time.LocalDateTime;

public class Report {

    private int           reportID;
    private int           managerID;
    private String        reportType;
    private LocalDateTime generatedDate;
    private String        status;

    public Report(int reportID, int managerID, String reportType,
                  LocalDateTime generatedDate, String status) {
        this.reportID      = reportID;
        this.managerID     = managerID;
        this.reportType    = reportType;
        this.generatedDate = generatedDate;
        this.status        = status;
    }

    public int           getReportID()      { return reportID; }
    public int           getManagerID()     { return managerID; }
    public String        getReportType()    { return reportType; }
    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public String        getStatus()        { return status; }
}