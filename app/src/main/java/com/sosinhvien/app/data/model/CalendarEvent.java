package com.sosinhvien.app.data.model;

public class CalendarEvent {
    public static final String TYPE_EVENT = "event";
    public static final String TYPE_TASK = "task";
    public static final String TYPE_SLEEP = "sleep";

    private final String id;
    private final String title;
    private final String timeRange;
    private final String type;
    private final String priority; // null for non-task
    private final boolean completed;

    public CalendarEvent(String id, String title, String timeRange, String type,
                         String priority, boolean completed) {
        this.id = id;
        this.title = title;
        this.timeRange = timeRange;
        this.type = type;
        this.priority = priority;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public String getType() {
        return type;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }
}
