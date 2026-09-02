package com.sosinhvien.app.data.auth

import com.sosinhvien.app.data.network.compose.AuthApi
import com.sosinhvien.app.data.network.compose.AuthApiFactory
import com.sosinhvien.app.data.network.compose.AuthResponse
import com.sosinhvien.app.data.network.compose.LoginRequest
import com.sosinhvien.app.data.network.compose.RefreshRequest
import com.sosinhvien.app.data.network.compose.RegisterRequest

class AuthRepository(
    private val tokenStore: TokenStore,
    private val api: AuthApi = AuthApiFactory.create(),
) {
    suspend fun register(email: String, password: String, fullName: String): AuthResponse {
        return api.register(RegisterRequest(email, password, fullName)).also(::save)
    }

    suspend fun login(email: String, password: String): AuthResponse {
        return api.login(LoginRequest(email, password)).also(::save)
    }

    suspend fun refresh(): AuthResponse {
        val refreshToken = tokenStore.refreshToken() ?: error("No refresh token saved")
        return api.refresh(RefreshRequest(refreshToken)).also(::save)
    }

    private fun save(response: AuthResponse) {
        tokenStore.save(response.accessToken, response.refreshToken)
    }
}
