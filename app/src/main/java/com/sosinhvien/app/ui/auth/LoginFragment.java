package com.sosinhvien.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sosinhvien.app.R;
import com.sosinhvien.app.data.MockDataRepository;
import com.sosinhvien.app.ui.onboarding.OnboardingActivity;
import com.sosinhvien.app.util.SessionManager;

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        TextInputEditText editEmail = view.findViewById(R.id.edit_email);
        TextInputEditText editPassword = view.findViewById(R.id.edit_password);
        TextView textError = view.findViewById(R.id.text_error);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(v -> {
            String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
            String password = editPassword.getText() != null ? editPassword.getText().toString() : "";

            if (MockDataRepository.getInstance().validateLogin(email, password)) {
                SessionManager session = new SessionManager(requireContext());
                session.login(email, "Minh");
                startActivity(new Intent(requireContext(), OnboardingActivity.class));
                requireActivity().finish();
            } else {
                textError.setText(R.string.login_error);
                textError.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }
}
