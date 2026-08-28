package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "budgets",
    primaryKeys = {"month_label", "user_email"},
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "email",
        childColumns = "user_email",
        onDelete = ForeignKey.CASCADE
    )
)
public class BudgetEntity {
    @ColumnInfo(name = "month_label")
    @NonNull
    @SerializedName("month_label")
    public String monthLabel; // Format: YYYY-MM

    @ColumnInfo(name = "user_email")
    @NonNull
    @SerializedName("user_email")
    public String userEmail;

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

    public BudgetEntity(@NonNull String monthLabel, @NonNull String userEmail, long totalBudget, long openingBalance) {
        this.monthLabel = monthLabel;
        this.userEmail = userEmail;
        this.totalBudget = totalBudget;
        this.openingBalance = openingBalance;
    }
}
