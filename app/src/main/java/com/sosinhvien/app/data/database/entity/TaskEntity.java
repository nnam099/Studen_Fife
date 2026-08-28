package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "tasks",
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "email",
        childColumns = "user_email",
        onDelete = ForeignKey.CASCADE
    )
)
public class TaskEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    public String id;

    @ColumnInfo(name = "user_email")
    @NonNull
    @SerializedName("user_email")
    public String userEmail;

    @NonNull
    @SerializedName("title")
    public String name;

    @SerializedName("deadline")
    public long deadline;

    @NonNull
    @SerializedName("priority")
    public String priority; // 'Thấp', 'Trung bình', 'Cao'

    @ColumnInfo(name = "estimated_duration")
    @SerializedName("duration_minutes")
    public int estimatedDuration; // in minutes

    @NonNull
    @SerializedName("status")
    public String status; // 'Chưa thực hiện', 'Đang thực hiện', 'Đã hoàn thành'

    @ColumnInfo(name = "completed_time")
    @Nullable
    @SerializedName("actual_completed_time")
    public Long completedTime; // timestamp in ms when finished

    public boolean deleted;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    @SerializedName("is_deleted")
    public int isDeleted = 0;

    public TaskEntity(@NonNull String id, @NonNull String userEmail, @NonNull String name,
                      long deadline, @NonNull String priority, int estimatedDuration,
                      @NonNull String status, @Nullable Long completedTime, boolean deleted) {
        this.id = id;
        this.userEmail = userEmail;
        this.name = name;
        this.deadline = deadline;
        this.priority = priority;
        this.estimatedDuration = estimatedDuration;
        this.status = status;
        this.completedTime = completedTime;
        this.deleted = deleted;
        this.isDeleted = deleted ? 1 : 0;
    }
}
