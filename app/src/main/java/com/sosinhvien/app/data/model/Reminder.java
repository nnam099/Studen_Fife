package com.sosinhvien.app.data.model;

public class Reminder {
    private final String title;
    private final String subtitle;
    private final String accentColor; // primary, warning, danger

    public Reminder(String title, String subtitle, String accentColor) {
        this.title = title;
        this.subtitle = subtitle;
        this.accentColor = accentColor;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getAccentColor() {
        return accentColor;
    }
}
