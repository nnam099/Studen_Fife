package com.sosinhvien.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "sosinhvien_session";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_ONBOARDED = "onboarded";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public boolean isOnboarded() {
        return prefs.getBoolean(KEY_ONBOARDED, false);
    }

    public void login(String email, String displayName) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_EMAIL, email)
                .putString(KEY_DISPLAY_NAME, displayName)
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
}
