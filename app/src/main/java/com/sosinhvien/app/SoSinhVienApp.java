package com.sosinhvien.app;

import android.app.Application;
import com.sosinhvien.app.data.MockDataRepository;

public class SoSinhVienApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MockDataRepository.initialize(this);
    }
}
