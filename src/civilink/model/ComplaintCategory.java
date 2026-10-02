package civilink.model;

import java.time.LocalDateTime;

class ComplaintCategory {

    private int    categoryID;
    private String categoryName;

    public ComplaintCategory(int categoryID, String categoryName) {
        this.categoryID   = categoryID;
        this.categoryName = categoryName;
    }

    public int    getCategoryID()   { return categoryID; }
    public String getCategoryName() { return categoryName; }

    @Override
    public String toString() { return categoryName; }
}