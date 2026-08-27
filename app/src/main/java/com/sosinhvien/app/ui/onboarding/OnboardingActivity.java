package com.sosinhvien.app.ui.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sosinhvien.app.R;
import com.sosinhvien.app.databinding.ActivityOnboardingBinding;
import com.sosinhvien.app.ui.main.MainActivity;
import com.sosinhvien.app.util.SessionManager;

public class OnboardingActivity extends AppCompatActivity {

    private ActivityOnboardingBinding binding;
    private int currentStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showStep(1);

        binding.btnSkip.setOnClickListener(v -> finishOnboarding());
        binding.btnContinue.setOnClickListener(v -> {
            if (currentStep < 3) {
                showStep(currentStep + 1);
            } else {
                finishOnboarding();
            }
        });
    }

    private void showStep(int step) {
        currentStep = step;
        binding.progressOnboarding.setProgress(step, true);

        TextView content = new TextView(this);
        content.setPadding(0, 16, 0, 0);
        content.setTextColor(getColor(R.color.on_surface_subtle));

        switch (step) {
            case 1:
                binding.textStepTitle.setText(R.string.onboarding_step1);
                binding.textStepDesc.setText("Khung giờ ngủ: 23:00 – 07:00\nKhung không khả dụng: 12:00 – 13:00");
                content.setText("• Giờ bắt đầu ngủ: 23:00\n• Giờ thức dậy: 07:00\n• Thời gian nghỉ trưa");
                break;
            case 2:
                binding.textStepTitle.setText(R.string.onboarding_step2);
                binding.textStepDesc.setText("Tùy chỉnh khung giờ học/làm việc hiệu quả");
                content.setText("• Đệm giữa hoạt động: 15 phút\n• Khoảng tối thiểu: 30 phút\n• Ngưỡng tập trung: 90 phút");
                break;
            case 3:
                binding.textStepTitle.setText(R.string.onboarding_step3);
                binding.textStepDesc.setText("Chọn sở thích và nhập số dư đầu kỳ");
                content.setText("• Sở thích: Học nhóm, Gym, Part-time\n• Số dư đầu kỳ: 2.000.000 đ");
                binding.btnContinue.setText(R.string.save);
                break;
            default:
                break;
        }

        binding.onboardingContent.removeAllViews();
        binding.onboardingContent.addView(content);
    }

    private void finishOnboarding() {
        SessionManager session = new SessionManager(this);
        session.completeOnboarding();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
