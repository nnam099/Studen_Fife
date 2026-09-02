package com.sosinhvien.app.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenStore(context: Context) {
    private val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        "student_finance_tokens",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun save(accessToken: String, refreshToken: String) {
        preferences.edit()
            .putString(ACCESS_TOKEN, accessToken)
            .putString(REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun accessToken(): String? = preferences.getString(ACCESS_TOKEN, null)

    fun refreshToken(): String? = preferences.getString(REFRESH_TOKEN, null)

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
    }
}
