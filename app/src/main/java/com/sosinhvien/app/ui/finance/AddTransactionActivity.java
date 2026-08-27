package com.sosinhvien.app.ui.finance;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sosinhvien.app.data.model.Transaction;
import com.sosinhvien.app.databinding.ActivityAddTransactionBinding;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "extra_type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAddTransactionBinding binding = ActivityAddTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm giao dịch");
        }

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        if (Transaction.TYPE_INCOME.equals(type)) {
            binding.btnIncome.setChecked(true);
        } else {
            binding.btnExpense.setChecked(true);
        }

        binding.btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu giao dịch (demo mock)", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
