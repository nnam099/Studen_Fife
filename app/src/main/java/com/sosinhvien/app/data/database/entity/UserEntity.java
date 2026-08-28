package com.sosinhvien.app.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    public String email;

    @ColumnInfo(name = "password_hash")
    @NonNull
    public String passwordHash;

    @ColumnInfo(name = "display_name")
    @NonNull
    public String displayName;

    @ColumnInfo(name = "failed_login_attempts")
    public int failedLoginAttempts;

    @ColumnInfo(name = "locked_until")
    public long lockedUntil; // timestamp in ms

    public UserEntity(@NonNull String email, @NonNull String passwordHash, @NonNull String displayName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.failedLoginAttempts = 0;
        this.lockedUntil = 0;
    }
}
