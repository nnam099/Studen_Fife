package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.google.gson.annotations.SerializedName;

@Entity(
    tableName = "categories",
    primaryKeys = {"id", "user_id"},
    indices = {@androidx.room.Index("user_id")},
    foreignKeys = @ForeignKey(
        entity = UserEntity.class,
        parentColumns = "id",
        childColumns = "user_id",
        onDelete = ForeignKey.CASCADE
    )
)
public class CategoryEntity {
    @NonNull
    @SerializedName("id")
    public String id;

    @ColumnInfo(name = "user_id")
    @NonNull
    @SerializedName("user_id")
    public String userId;

    @NonNull
    @SerializedName("name")
    public String name;

    @ColumnInfo(name = "icon_name")
    @NonNull
    @SerializedName("icon_name")
    public String iconName;

    @ColumnInfo(name = "is_default")
    @SerializedName("is_default")
    public boolean isDefault;

    @SerializedName("visible")
    public boolean visible;

    @ColumnInfo(name = "updated_at", defaultValue = "0")
    @SerializedName("updated_at")
    public long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "is_synced", defaultValue = "0")
    public int isSynced = 0;

    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    @SerializedName("is_deleted")
    public int isDeleted = 0;

    public CategoryEntity(@NonNull String id, @NonNull String userId, @NonNull String name,
                          @NonNull String iconName, boolean isDefault, boolean visible) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.iconName = iconName;
        this.isDefault = isDefault;
        this.visible = visible;
    }
}
