package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "category_budgets",
    primaryKeys = {"month_label", "category_id", "user_email"},
    foreignKeys = {
        @ForeignKey(
            entity = BudgetEntity.class,
            parentColumns = {"month_label", "user_email"},
            childColumns = {"month_label", "user_email"},
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = CategoryEntity.class,
            parentColumns = {"id", "user_email"},
            childColumns = {"category_id", "user_email"},
            onDelete = ForeignKey.CASCADE
        )
    }
)
public class CategoryBudgetEntity {
    @ColumnInfo(name = "month_label")
    @NonNull
    @SerializedName("month_label")
    public String monthLabel;

    @ColumnInfo(name = "category_id")
    @NonNull
    @SerializedName("category_id")
    public String categoryId;

    @ColumnInfo(name = "user_email")
    @NonNull
    @SerializedName("user_email")
    public String userEmail;

    @SerializedName("amount")
    public long amount;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    @SerializedName("is_deleted")
    public int isDeleted = 0;

    public CategoryBudgetEntity(@NonNull String monthLabel, @NonNull String categoryId, @NonNull String userEmail, long amount) {
        this.monthLabel = monthLabel;
        this.categoryId = categoryId;
        this.userEmail = userEmail;
        this.amount = amount;
    }
}
