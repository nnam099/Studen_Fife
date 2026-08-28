package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "transactions",
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "email",
            childColumns = "user_email",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = CategoryEntity.class,
            parentColumns = {"id", "user_email"},
            childColumns = {"category_id", "user_email"},
            onDelete = ForeignKey.SET_NULL
        )
    }
)
public class TransactionEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    public String id;

    @ColumnInfo(name = "user_email")
    @NonNull
    @SerializedName("user_email")
    public String userEmail;

    @NonNull
    @SerializedName("name")
    public String name;

    @ColumnInfo(name = "category_id")
    @Nullable
    @SerializedName("category_id")
    public String categoryId;

    @SerializedName("amount")
    public long amount;

    @NonNull
    @SerializedName("type")
    public String type; // 'income' or 'expense'

    @NonNull
    @SerializedName("source")
    public String source; // 'Thủ công', 'OCR', 'Trợ lý'

    @SerializedName("timestamp")
    public long timestamp;

    @ColumnInfo(name = "time_label")
    @NonNull
    @SerializedName("time_label")
    public String timeLabel;

    @Nullable
    @SerializedName("note")
    public String notes;

    public boolean deleted;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    @SerializedName("is_deleted")
    public int isDeleted = 0;

    public TransactionEntity(@NonNull String id, @NonNull String userEmail, @NonNull String name,
                             @Nullable String categoryId, long amount, @NonNull String type,
                             @NonNull String source, long timestamp, @NonNull String timeLabel,
                             @Nullable String notes, boolean deleted) {
        this.id = id;
        this.userEmail = userEmail;
        this.name = name;
        this.categoryId = categoryId;
        this.amount = amount;
        this.type = type;
        this.source = source;
        this.timestamp = timestamp;
        this.timeLabel = timeLabel;
        this.notes = notes;
        this.deleted = deleted;
        this.isDeleted = deleted ? 1 : 0;
    }
}
