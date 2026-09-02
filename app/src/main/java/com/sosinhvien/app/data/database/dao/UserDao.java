package com.sosinhvien.app.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.sosinhvien.app.data.database.entity.UserConfigEntity;
import com.sosinhvien.app.data.database.entity.UserEntity;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    @Update
    void updateUser(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertConfig(UserConfigEntity config);

    @Update
    void updateConfig(UserConfigEntity config);

    @Query("SELECT * FROM user_configs WHERE user_id = :userId LIMIT 1")
    UserConfigEntity getConfigByUserId(String userId);
}
