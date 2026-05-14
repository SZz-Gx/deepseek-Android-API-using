package com.example.deepseekchat.data.local

import android.content.Context
import android.net.Uri

object BackgroundStore {
    private const val PREFS_FILE = "deepseek_background"
    private const val KEY_URI = "background_uri"
    private const val KEY_ENABLED = "background_enabled"

    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }

    var backgroundUri: String
        get() = prefs.getString(KEY_URI, "") ?: ""
        set(value) = prefs.edit().putString(KEY_URI, value).apply()

    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    fun setBackground(uri: Uri) {
        backgroundUri = uri.toString()
        isEnabled = true
    }

    fun clearBackground() {
        backgroundUri = ""
        isEnabled = false
    }
}
