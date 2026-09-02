package com.sosinhvien.app.ui.finance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sosinhvien.app.databinding.ActivityOcrConfirmBinding;

public class OcrConfirmActivity extends AppCompatActivity {

    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> pickImageLauncher;
    private com.sosinhvien.app.databinding.ActivityOcrConfirmBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOcrConfirmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chụp hóa đơn (OCR demo)");
        }

        pickImageLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.net.Uri imageUri = result.getData().getData();
                        binding.imgReceipt.setImageURI(imageUri);
                        
                        // Simulate OCR process
                        Toast.makeText(this, "Đang trích xuất dữ liệu...", Toast.LENGTH_SHORT).show();
                        binding.editOcrAmount.setText("45000");
                        binding.editOcrName.setText("Highlands Coffee");
                    }
                });

        binding.btnPickImage.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> {
            String name = binding.editOcrName.getText() != null ? binding.editOcrName.getText().toString().trim() : "";
            String amountStr = binding.editOcrAmount.getText() != null ? binding.editOcrAmount.getText().toString().trim() : "0";
            long amount = 0;
            try {
                amount = Long.parseLong(amountStr);
            } catch (NumberFormatException ignored) {}

            if (name.isEmpty() || amount <= 0) {
                Toast.makeText(this, "Vui lòng chọn ảnh và kiểm tra dữ liệu", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save actual transaction
            final long finalAmount = amount;
            com.sosinhvien.app.data.MockDataRepository.getInstance().executeAsync(() -> {
                com.sosinhvien.app.data.MockDataRepository.getInstance().addTransaction(
                        name, "food", finalAmount, com.sosinhvien.app.data.model.Transaction.TYPE_EXPENSE, 
                        com.sosinhvien.app.data.model.Transaction.SOURCE_MANUAL);
                com.sosinhvien.app.data.MockDataRepository.getInstance().runOnMainThread(() -> {
                    Toast.makeText(this, "Đã lưu giao dịch", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
