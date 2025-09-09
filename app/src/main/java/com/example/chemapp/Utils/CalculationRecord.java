package com.example.chemapp.Utils;


public class CalculationRecord {
    public static final int ELEMENT_HISTORY_ITEM   = 1;
    public static final int MOLARITY_HISTORY_ITEM   = 2;
    public static final int PPM_HISTORY_ITEM = 3;
    public static final int DILUTION_HISTORY_ITEM  = 4;

    public final long id;

    public final String title;

    public final int type;
    public final String description;
    public final String[][] tableData;

    public CalculationRecord(long id, String title, int type, String description, String[][] data) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.tableData = data;
    }

    public long getId() { return id; }

    public int getType() { return type; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
