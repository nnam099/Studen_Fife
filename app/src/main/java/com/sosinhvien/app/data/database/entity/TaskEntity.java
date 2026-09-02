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
    indices = {@androidx.room.Index("user_id")},
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class TaskEntity {
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

    public TaskEntity(@NonNull String id, @NonNull String userId, @NonNull String name,
                      long deadline, @NonNull String priority, int estimatedDuration,
                      @NonNull String status, @Nullable Long completedTime, boolean deleted) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.deadline = deadline;
        this.priority = priority;
        this.estimatedDuration = estimatedDuration;
        this.status = status;
        this.completedTime = completedTime;
        this.deleted = deleted;
        this.isDeleted = deleted ? 1 : 0;
    }

    public boolean isOverdue() {
        return deadline < System.currentTimeMillis() && !"Đã hoàn thành".equals(status);
    }
}
