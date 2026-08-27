package com.sosinhvien.app.ui.calendar;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sosinhvien.app.databinding.ActivityAiScheduleBinding;

public class AiScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAiScheduleBinding binding = ActivityAiScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đề xuất lịch AI");
        }

        binding.btnAccept.setOnClickListener(v ->
                Toast.makeText(this, "Đã chấp nhận đề xuất (demo mock)", Toast.LENGTH_SHORT).show());
        binding.btnReject.setOnClickListener(v ->
                Toast.makeText(this, "Đã từ chối đề xuất (demo mock)", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
