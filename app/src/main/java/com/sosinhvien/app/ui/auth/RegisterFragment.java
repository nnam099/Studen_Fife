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

            if (email.isEmpty() || name.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 8) {
                Toast.makeText(requireContext(), "Mật khẩu phải chứa ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
                Toast.makeText(requireContext(), "Mật khẩu phải gồm cả chữ và số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(requireContext(), "Xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);

            // 1. Try Online Registration First
            com.sosinhvien.app.data.network.ApiClient.getApiService(requireContext())
                    .register(new com.sosinhvien.app.data.network.model.AuthModels.RegisterRequest(email, password, name))
                    .enqueue(new retrofit2.Callback<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse> call,
                                               retrofit2.Response<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse> response) {
                            if (!isAdded()) return;
                            btnRegister.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null) {
                                com.sosinhvien.app.data.network.model.AuthModels.AuthResponse authRes = response.body();
                                SessionManager session = new SessionManager(requireContext());
                                session.login(authRes.userId, authRes.email, authRes.displayName, authRes.token);

                                // Save user profile locally
                                com.sosinhvien.app.data.MockDataRepository.getInstance().executeAsync(() -> {
                                    com.sosinhvien.app.data.MockDataRepository.getInstance().saveUserLocally(authRes.email, authRes.displayName);
                                });

                                // Trigger initial background sync
                                com.sosinhvien.app.data.sync.SyncManager.getInstance(requireContext()).triggerSync(null);

                                Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(requireContext(), OnboardingActivity.class));
                                requireActivity().finish();
                            } else {
                                try {
                                    String errorBodyStr = response.errorBody() != null ? response.errorBody().string() : "";
                                    String message = "Đăng ký thất bại.";
                                    if (errorBodyStr.contains("message")) {
                                        message = new org.json.JSONObject(errorBodyStr).optString("message", message);
                                    }
                                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(requireContext(), "Đăng ký không thành công.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.sosinhvien.app.data.network.model.AuthModels.AuthResponse> call, Throwable t) {
                            if (!isAdded()) return;
                            btnRegister.setEnabled(true);

                            // 2. Fallback to Local Registration
                            com.sosinhvien.app.data.MockDataRepository.getInstance().executeAsync(() -> {
                                boolean isRegistered = com.sosinhvien.app.data.MockDataRepository.getInstance().registerUser(email, password, name);
                                String userId = com.sosinhvien.app.data.MockDataRepository.getInstance().getUserIdByEmail(email);
                                com.sosinhvien.app.data.MockDataRepository.getInstance().runOnMainThread(() -> {
                                    if (isRegistered) {
                                        SessionManager session = new SessionManager(requireContext());
                                        session.login(userId, email, name, ""); // local token is empty

                                        Toast.makeText(requireContext(), "Đăng ký ngoại tuyến thành công!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(requireContext(), OnboardingActivity.class));
                                        requireActivity().finish();
                                    } else {
                                        Toast.makeText(requireContext(), "Đăng ký thất bại: Không kết nối máy chủ hoặc email đã tồn tại ngoại tuyến.", Toast.LENGTH_LONG).show();
                                    }
                                });
                            });
                        }
                    });
        });

        return view;
    }
}
