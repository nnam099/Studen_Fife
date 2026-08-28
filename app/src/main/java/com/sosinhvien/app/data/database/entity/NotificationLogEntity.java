package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "notification_logs",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "email",
        childColumns = "user_email",
        onDelete = ForeignKey.CASCADE
    )
)
public class NotificationLogEntity {
    @PrimaryKey
    @NonNull
    public String id;

    @ColumnInfo(name = "user_email")
    @NonNull
    public String userEmail;

    @NonNull
    public String title;

    @NonNull
    public String subtitle;

    @NonNull
    public String type; // 'budget_80', 'budget_100', 'task_deadline', 'event_upcoming', 'daily_record', 'monthly_budget'

    @ColumnInfo(name = "accent_color")
    @NonNull
    public String accentColor; // 'primary', 'warning', 'danger'

    public long timestamp;

    @ColumnInfo(name = "is_read")
    public boolean isRead;

    @ColumnInfo(name = "entity_id")
    @Nullable
    public String entityId;

    public NotificationLogEntity(@NonNull String id, @NonNull String userEmail, @NonNull String title,
                                 @NonNull String subtitle, @NonNull String type, @NonNull String accentColor,
                                 long timestamp, boolean isRead, @Nullable String entityId) {
        this.id = id;
        this.userEmail = userEmail;
        this.title = title;
        this.subtitle = subtitle;
        this.type = type;
        this.accentColor = accentColor;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.entityId = entityId;
    }
}
