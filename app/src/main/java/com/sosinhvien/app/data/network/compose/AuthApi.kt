package com.sosinhvien.app.data.network.compose

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

interface AuthApi {
    @POST("auth/register")
    suspend fun register(request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(request: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(request: RefreshRequest): AuthResponse
}

data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RefreshRequest(
    val refreshToken: String,
)

data class AuthResponse(
    val user: AuthUser? = null,
    val accessToken: String,
    val refreshToken: String,
)

data class AuthUser(
    val id: String,
    val email: String,
    val fullName: String,
)

object AuthApiFactory {
    fun create(): AuthApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
        .baseUrl(com.sosinhvien.app.BuildConfig.BACKEND_BASE_URL)
            .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
            .create(AuthApi::class.java)
    }
}
