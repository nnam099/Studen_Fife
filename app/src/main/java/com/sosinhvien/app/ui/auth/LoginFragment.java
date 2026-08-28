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

            if (email.isEmpty() || password.isEmpty()) {
                textError.setText("Vui lòng nhập đầy đủ email và mật khẩu.");
                textError.setVisibility(View.VISIBLE);
                return;
            }

            textError.setVisibility(View.GONE);
            btnSubmit.setEnabled(false);

            // 1. Try Online Login First
            com.sosinhvien.app.data.network.ApiClient.getApiService(requireContext())
                    .login(new com.sosinhvien.app.data.network.model.AuthModels.LoginRequest(email, password))
                    .enqueue(new retrofit2.Callback<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse> call,
                                               retrofit2.Response<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse> response) {
                            if (!isAdded()) return;
                            btnSubmit.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                com.sosinhvien.app.data.network.model.AuthModels.AuthResponse authRes = response.body();
                                SessionManager session = new SessionManager(requireContext());
                                session.login(authRes.email, authRes.displayName, authRes.token);

                                // Save user profile locally
                                MockDataRepository.getInstance().saveUserLocally(authRes.email, authRes.displayName);

                                // Trigger initial background sync
                                com.sosinhvien.app.data.sync.SyncManager.getInstance(requireContext()).triggerSync(null);

                                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(requireContext(), OnboardingActivity.class));
                                requireActivity().finish();
                            } else {
                                try {
                                    String errorBodyStr = response.errorBody() != null ? response.errorBody().string() : "";
                                    String message = "Email hoặc mật khẩu không chính xác.";
                                    if (errorBodyStr.contains("message")) {
                                        message = new org.json.JSONObject(errorBodyStr).optString("message", message);
                                    }
                                    textError.setText(message);
                                } catch (Exception e) {
                                    textError.setText("Email hoặc mật khẩu không chính xác.");
                                }
                                textError.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse> call, Throwable t) {
                            if (!isAdded()) return;
                            btnSubmit.setEnabled(true);

                            // 2. Fallback to Local Authentication
                            if (MockDataRepository.getInstance().validateLogin(email, password)) {
                                SessionManager session = new SessionManager(requireContext());
                                String name = MockDataRepository.getInstance().getUserDisplayName(email);
                                session.login(email, name, ""); // local sync token is empty

                                Toast.makeText(requireContext(), "Đăng nhập ngoại tuyến thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(requireContext(), OnboardingActivity.class));
                                requireActivity().finish();
                            } else {
                                textError.setText("Không thể kết nối máy chủ. Tài khoản ngoại tuyến không chính xác.");
                                textError.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        });

        return view;
    }
}
