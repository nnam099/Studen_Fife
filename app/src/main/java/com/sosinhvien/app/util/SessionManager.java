package com.sosinhvien.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "sosinhvien_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ONBOARDED = "onboarded";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_LAST_SYNC_TIME = "last_sync_time";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        try {
            androidx.security.crypto.MasterKey masterKey = new androidx.security.crypto.MasterKey.Builder(context)
                    .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)
                    .build();

            prefs = androidx.security.crypto.EncryptedSharedPreferences.create(
                    context,
                    PREFS,
                    masterKey,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback for devices where keystore might be corrupted/unsupported (rare, but good practice for demo)
            prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        }
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean isOnboarded() {
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }

    public void login(String userId, String email, String displayName, String token) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email)
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_TOKEN, token)
                .apply();
    }

    public void completeOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply();
    }

    public void logout() {
        prefs.edit().clear().apply();
    }

    public String getDisplayName() {
        return prefs.getString(KEY_DISPLAY_NAME, "Minh");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "demo@truong.edu.vn");
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "default_user_id");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public long getLastSyncTime() {
        return prefs.getLong(KEY_LAST_SYNC_TIME, 0L);
    }

    public void saveLastSyncTime(long timestamp) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply();
    }
}
