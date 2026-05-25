package com.example.sambungayat;

public class Badge {
    private String title;
    private String description;
    private int iconResId;
    private boolean isUnlocked;

    public Badge(String title, String description, int iconResId, boolean isUnlocked) {
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.isUnlocked = isUnlocked;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
    public boolean isUnlocked() { return isUnlocked; }
}