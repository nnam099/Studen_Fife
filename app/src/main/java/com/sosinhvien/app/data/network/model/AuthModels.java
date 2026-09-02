package com.sosinhvien.app.data.network.model;

public class AuthModels {
    public static class RegisterRequest {
        public String email;
        public String password;
        public String displayName;

        public RegisterRequest(String email, String password, String displayName) {
            this.email = email;
            this.password = password;
            this.displayName = displayName;
        }
    }

    public static class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    public static class AuthResponse {
        public String userId;
        public String token;
        public String email;
        public String displayName;
    }
}
