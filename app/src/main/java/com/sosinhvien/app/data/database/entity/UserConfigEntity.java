package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "user_configs",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "email",
        childColumns = "user_email",
        onDelete = ForeignKey.CASCADE
    )
)
public class UserConfigEntity {
    @PrimaryKey
    @ColumnInfo(name = "user_email")
    @NonNull
    @SerializedName("user_email")
    public String userEmail;

    @ColumnInfo(name = "sleep_start_time")
    @NonNull
    @SerializedName("sleep_start_time")
    public String sleepStartTime; // Format: HH:MM

    @ColumnInfo(name = "sleep_end_time")
    @NonNull
    @SerializedName("sleep_end_time")
    public String sleepEndTime; // Format: HH:MM

    @ColumnInfo(name = "buffer_time")
    @SerializedName("buffer_time")
    public int bufferTime; // in minutes, default 15

    @ColumnInfo(name = "min_interval_duration")
    @SerializedName("min_interval_duration")
    public int minIntervalDuration; // in minutes, default 30

    @ColumnInfo(name = "concentration_threshold")
    @SerializedName("concentration_threshold")
    public int concentrationThreshold; // in minutes, default 90

    @ColumnInfo(name = "activity_interests")
    @Nullable
    @SerializedName("focus_topics")
    public String activityInterests; // comma separated or JSON

    @ColumnInfo(name = "personal_goals")
    @Nullable
    @SerializedName("improvement_goals")
    public String personalGoals;

    @ColumnInfo(name = "unavailable_hours")
    @Nullable
    public String unavailableHours; // JSON representing weekly slot exclusions

    @ColumnInfo(name = "preferred_hours")
    @Nullable
    public String preferredHours; // JSON representing preferred hours

    @ColumnInfo(name = "budget_alert_enabled")
    @SerializedName("budget_alert_enabled")
    public boolean budgetAlertEnabled; // default true

    @ColumnInfo(name = "deadline_reminder_hours")
    @NonNull
    @SerializedName("deadline_reminder_hours")
    public String deadlineReminderHours; // default "24,3" (24h and 3h)

    @ColumnInfo(name = "event_reminder_minutes")
    @SerializedName("event_reminder_minutes")
    public int eventReminderMinutes; // default 15 min

    @ColumnInfo(name = "record_reminder_time")
    @NonNull
    @SerializedName("record_reminder_time")
    public String recordReminderTime; // default "21:00"

    @ColumnInfo(name = "budget_reminder_enabled")
    @SerializedName("budget_reminder_enabled")
    public boolean budgetReminderEnabled; // default true

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    public UserConfigEntity(@NonNull String userEmail, @NonNull String sleepStartTime, @NonNull String sleepEndTime,
                            int bufferTime, int minIntervalDuration, int concentrationThreshold,
                            @Nullable String activityInterests, @Nullable String personalGoals,
                            @Nullable String unavailableHours, @Nullable String preferredHours,
                            boolean budgetAlertEnabled, @NonNull String deadlineReminderHours,
                            int eventReminderMinutes, @NonNull String recordReminderTime,
                            boolean budgetReminderEnabled) {
        this.userEmail = userEmail;
        this.sleepStartTime = sleepStartTime;
        this.sleepEndTime = sleepEndTime;
        this.bufferTime = bufferTime;
        this.minIntervalDuration = minIntervalDuration;
        this.concentrationThreshold = concentrationThreshold;
        this.activityInterests = activityInterests;
        this.personalGoals = personalGoals;
        this.unavailableHours = unavailableHours;
        this.preferredHours = preferredHours;
        this.budgetAlertEnabled = budgetAlertEnabled;
        this.deadlineReminderHours = deadlineReminderHours;
        this.eventReminderMinutes = eventReminderMinutes;
        this.recordReminderTime = recordReminderTime;
        this.budgetReminderEnabled = budgetReminderEnabled;
    }
}
