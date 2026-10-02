package civilink.model;

import java.time.LocalDateTime;

class SLA {

    private int slaID;
    private int categoryID;
    private int defaultDays;

    public SLA(int slaID, int categoryID, int defaultDays) {
        this.slaID       = slaID;
        this.categoryID  = categoryID;
        this.defaultDays = defaultDays;
    }

    public int getSlaID()       { return slaID; }
    public int getCategoryID()  { return categoryID; }
    public int getDefaultDays() { return defaultDays; }

    /** Returns true if the complaint is past its deadline. */
    public boolean checkDeadline(java.time.LocalDate submissionDate) {
        return submissionDate.plusDays(defaultDays).isBefore(java.time.LocalDate.now());
    }
}