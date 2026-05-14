package com.example.deepseekchat.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

// API Key 加密存储（EncryptedSharedPreferences + AES256-GCM）
// 密钥由 Android Keystore 硬件保管

object SecurePreferences {
    private const val PREFS_FILE = "deepseek_secure_prefs"
    private const val KEY_API_KEY = "api_key"

    private lateinit var encryptedPrefs: android.content.SharedPreferences

    fun init(context: Context) {
        if (::encryptedPrefs.isInitialized) return
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedPrefs = EncryptedSharedPreferences.create(
            PREFS_FILE, masterKeyAlias, context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var apiKey: String
        get() {
            if (!::encryptedPrefs.isInitialized) return ""
            return encryptedPrefs.getString(KEY_API_KEY, "") ?: ""
        }
        set(value) {
            if (!::encryptedPrefs.isInitialized) return
            encryptedPrefs.edit().putString(KEY_API_KEY, value).apply()
        }

    val hasApiKey: Boolean get() = apiKey.isNotBlank()
}
