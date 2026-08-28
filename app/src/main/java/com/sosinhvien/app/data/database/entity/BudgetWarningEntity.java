package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
    tableName = "budget_warning_flags",
    primaryKeys = {"month_label", "category_id", "user_email"},
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "email",
        childColumns = "user_email",
        onDelete = ForeignKey.CASCADE
    )
)
public class BudgetWarningEntity {
    @ColumnInfo(name = "month_label")
    @NonNull
    public String monthLabel;

    @ColumnInfo(name = "category_id")
    @NonNull
    public String categoryId; // 'total' for total budget warning

    @ColumnInfo(name = "user_email")
    @NonNull
    public String userEmail;

    @ColumnInfo(name = "alerted_80")
    public boolean alerted80;

    @ColumnInfo(name = "alerted_100")
    public boolean alerted100;

    public BudgetWarningEntity(@NonNull String monthLabel, @NonNull String categoryId, @NonNull String userEmail,
                               boolean alerted80, boolean alerted100) {
        this.monthLabel = monthLabel;
        this.categoryId = categoryId;
        this.userEmail = userEmail;
        this.alerted80 = alerted80;
        this.alerted100 = alerted100;
    }
}
