package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "budgets",
    primaryKeys = {"month_label", "user_id"},
    indices = {@androidx.room.Index("user_id")},
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class BudgetEntity {
    @ColumnInfo(name = "month_label")
    @NonNull
    @SerializedName("month_label")
    public String monthLabel; // Format: YYYY-MM

    @ColumnInfo(name = "user_id")
    @NonNull
    @SerializedName("user_id")
    public String userId;

    @ColumnInfo(name = "total_budget")
    @SerializedName("total_budget")
    public long totalBudget;

    @ColumnInfo(name = "opening_balance")
    @SerializedName("opening_balance")
    public long openingBalance;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    @SerializedName("is_deleted")
    public int isDeleted = 0;

    public BudgetEntity(@NonNull String monthLabel, @NonNull String userId, long totalBudget, long openingBalance) {
        this.monthLabel = monthLabel;
        this.userId = userId;
        this.totalBudget = totalBudget;
        this.openingBalance = openingBalance;
    }
}
