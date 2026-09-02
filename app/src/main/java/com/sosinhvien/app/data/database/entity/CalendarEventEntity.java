package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "calendar_events",
    indices = {
        @androidx.room.Index("user_id"),
        @androidx.room.Index("task_id")
    },
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = TaskEntity.class,
            parentColumns = "id",
            childColumns = "task_id",
            onDelete = ForeignKey.SET_NULL
        ),
        @ForeignKey(
            entity = CalendarEventEntity.class,
            parentColumns = "id",
            childColumns = "parent_event_id",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class CalendarEventEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    public String id;

    @ColumnInfo(name = "user_id")
    @NonNull
    @SerializedName("user_id")
    public String userId;

    @NonNull
    @SerializedName("title")
    public String title;

    @ColumnInfo(name = "start_time")
    @SerializedName("start_time")
    public long startTime;

    @ColumnInfo(name = "end_time")
    @SerializedName("end_time")
    public long endTime;

    @NonNull
    @SerializedName("type")
    public String type; // 'event', 'task', 'sleep'

    @Nullable
    @SerializedName("priority")
    public String priority; // 'Thấp', 'Trung bình', 'Cao' (derived from task)

    @ColumnInfo(name = "task_id")
    @Nullable
    @SerializedName("task_id")
    public String taskId;

    @ColumnInfo(name = "session_status")
    @Nullable
    @SerializedName("session_status")
    public String sessionStatus; // 'Đã lên lịch', 'Đã hoàn thành', 'Đã bỏ'

    @ColumnInfo(name = "session_actual_duration")
    @Nullable
    @SerializedName("session_actual_duration")
    public Integer sessionActualDuration; // in minutes

    @ColumnInfo(name = "is_recurring")
    @SerializedName("is_recurrence_exception")
    public boolean isRecurring;

    @ColumnInfo(name = "recurrence_rule")
    @Nullable
    @SerializedName("recurrence_rule")
    public String recurrenceRule; // e.g., 'weekly'

    @ColumnInfo(name = "recurrence_end_date")
    @Nullable
    @SerializedName("recurrence_exception_date")
    public Long recurrenceEndDate;

    @ColumnInfo(name = "parent_event_id")
    @Nullable
    @SerializedName("original_event_id")
    public String parentEventId; // links exception to parent event series

    public boolean deleted;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    @SerializedName("is_deleted")
    public int isDeleted = 0;

    public CalendarEventEntity(@NonNull String id, @NonNull String userId, @NonNull String title,
                               long startTime, long endTime, @NonNull String type,
                               @Nullable String priority, @Nullable String taskId,
                               @Nullable String sessionStatus, @Nullable Integer sessionActualDuration,
                               boolean isRecurring, @Nullable String recurrenceRule,
                               @Nullable Long recurrenceEndDate, @Nullable String parentEventId,
                               boolean deleted) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.priority = priority;
        this.taskId = taskId;
        this.sessionStatus = sessionStatus;
        this.sessionActualDuration = sessionActualDuration;
        this.isRecurring = isRecurring;
        this.recurrenceRule = recurrenceRule;
        this.recurrenceEndDate = recurrenceEndDate;
        this.parentEventId = parentEventId;
        this.deleted = deleted;
        this.isDeleted = deleted ? 1 : 0;
    }
}
