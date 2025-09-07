package com.example.chemapp.Utils;

public class HistoryItem {
    public static final int ELEMENT_HISTORY_ITEM   = 1;
    public static final int MOLARITY_HISTORY_ITEM   = 2;
    public static final int PPM_HISTORY_ITEM = 3;
    public static final int DILUTION_HISTORY_ITEM  = 4;

    private long id;
    private int type;
    private String title;
    private String description;

    public HistoryItem(long id, int type, String title, String description) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.description = description;
    }

    public long getId() { return id; }
    public int getType() { return type; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
