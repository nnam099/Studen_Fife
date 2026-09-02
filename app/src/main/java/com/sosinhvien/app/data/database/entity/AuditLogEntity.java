package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "audit_logs",
    indices = {
        @androidx.room.Index("user_id"),
        @androidx.room.Index("transaction_id")
    },
    foreignKeys = {
        @ForeignKey(
            entity = UserEntity.class,
            parentColumns = "id",
            childColumns = "user_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = TransactionEntity.class,
            parentColumns = "id",
            childColumns = "transaction_id",
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class AuditLogEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    public String id;

    @ColumnInfo(name = "transaction_id")
    @NonNull
    @SerializedName("transaction_id")
    public String transactionId;

    @ColumnInfo(name = "user_id")
    @NonNull
    @SerializedName("user_id")
    public String userId;

    @NonNull
    @SerializedName("action")
    public String action; // 'INSERT', 'UPDATE', 'DELETE'

    @ColumnInfo(name = "amount_change")
    @SerializedName("amount_change")
    public long amountChange;

    @SerializedName("timestamp")
    public long timestamp;

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    public AuditLogEntity(@NonNull String id, @NonNull String transactionId, @NonNull String userId,
                          @NonNull String action, long amountChange, long timestamp) {
        this.id = id;
        this.transactionId = transactionId;
        this.userId = userId;
        this.action = action;
        this.amountChange = amountChange;
        this.timestamp = timestamp;
    }
}
