package com.sosinhvien.app.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sosinhvien.app.R;
import com.sosinhvien.app.databinding.ActivityMainBinding;
import com.sosinhvien.app.ui.finance.AddTransactionActivity;
import com.sosinhvien.app.ui.finance.OcrConfirmActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
            binding.bottomNav.setSelectedItemId(R.id.nav_home);
        }

        binding.bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
        binding.fabAdd.setOnClickListener(v -> showQuickAddSheet());
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.sosinhvien.app.data.sync.SyncManager.getInstance(this).triggerSync(new com.sosinhvien.app.data.sync.SyncManager.SyncCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                    if (current != null && current.isAdded()) {
                        current.onResume();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                // Fail silently in background
            }
        });
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_finance) {
            fragment = new FinanceFragment();
        } else if (id == R.id.nav_calendar) {
            fragment = new CalendarFragment();
        } else if (id == R.id.nav_assistant) {
            fragment = new AssistantFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            return false;
        }
        switchFragment(fragment);
        return true;
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void showQuickAddSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);

        com.google.android.material.button.MaterialButton btnExpense =
                new com.google.android.material.button.MaterialButton(this);
        btnExpense.setText(R.string.add_expense);
        btnExpense.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, AddTransactionActivity.class)
                    .putExtra(AddTransactionActivity.EXTRA_TYPE, "expense"));
        });

        com.google.android.material.button.MaterialButton btnIncome =
                new com.google.android.material.button.MaterialButton(this);
        btnIncome.setText(R.string.add_income);
        btnIncome.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, AddTransactionActivity.class)
                    .putExtra(AddTransactionActivity.EXTRA_TYPE, "income"));
        });

        com.google.android.material.button.MaterialButton btnOcr =
                new com.google.android.material.button.MaterialButton(this);
        btnOcr.setText(R.string.scan_receipt);
        btnOcr.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, OcrConfirmActivity.class));
        });

        layout.addView(btnExpense);
        layout.addView(btnIncome);
        layout.addView(btnOcr);
        dialog.setContentView(layout);
        dialog.show();
    }
}
