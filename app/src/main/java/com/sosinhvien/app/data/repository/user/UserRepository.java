package com.sosinhvien.app.data.repository.user;

import com.sosinhvien.app.data.database.entity.UserConfigEntity;
import com.sosinhvien.app.data.database.entity.UserEntity;

public interface UserRepository {

    UserEntity getUserByEmail(String email);

    UserEntity getUserById(String userId);

    boolean register(String email, String password, String displayName);

    boolean login(String email, String password);

    void saveSession(String userId, String email, String displayName, String token);

    String getCurrentUserId();

    UserConfigEntity getUserConfig(String userId);

    void updateUserConfig(String userId, UserConfigEntity config);
}
