package com.sosinhvien.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.sosinhvien.app.ui.auth.AuthActivity;
import com.sosinhvien.app.ui.main.MainActivity;
import com.sosinhvien.app.ui.onboarding.OnboardingActivity;
import com.sosinhvien.app.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        Intent intent;
        if (!session.isLoggedIn()) {
            intent = new Intent(this, AuthActivity.class);
        } else if (!session.isOnboarded()) {
            intent = new Intent(this, OnboardingActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
