package com.sosinhvien.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sosinhvien.app.R;
import com.sosinhvien.app.ui.onboarding.OnboardingActivity;
import com.sosinhvien.app.util.SessionManager;

public class RegisterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        TextInputEditText editName = view.findViewById(R.id.edit_display_name);
        TextInputEditText editEmail = view.findViewById(R.id.edit_email);
        TextInputEditText editPassword = view.findViewById(R.id.edit_password);
        TextInputEditText editConfirm = view.findViewById(R.id.edit_confirm_password);
        MaterialButton btnRegister = view.findViewById(R.id.btn_register);

        btnRegister.setOnClickListener(v -> {
            String name = editName.getText() != null ? editName.getText().toString().trim() : "Sinh viên";
            String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
            String password = editPassword.getText() != null ? editPassword.getText().toString() : "";
            String confirm = editConfirm.getText() != null ? editConfirm.getText().toString() : "";

            if (email.isEmpty() || password.length() < 8 || !password.equals(confirm)) {
                Toast.makeText(requireContext(), "Vui lòng kiểm tra thông tin đăng ký", Toast.LENGTH_SHORT).show();
                return;
            }

            SessionManager session = new SessionManager(requireContext());
            session.login(email, name);
            startActivity(new Intent(requireContext(), OnboardingActivity.class));
            requireActivity().finish();
        });

        return view;
    }
}
