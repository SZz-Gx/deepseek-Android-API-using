package com.example.deepseekchat

import android.app.Application
import com.example.deepseekchat.data.local.ApiConfigStore
import com.example.deepseekchat.data.local.BackgroundStore
import com.example.deepseekchat.data.local.BalanceManager
import com.example.deepseekchat.data.local.ChatHistoryStore
import com.example.deepseekchat.data.local.SecurePreferences
import com.example.deepseekchat.data.local.SessionManager
import com.example.deepseekchat.data.local.VersionHistoryStore

class DeepSeekChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SecurePreferences.init(this)
        BalanceManager.init(this)
        ChatHistoryStore.init(this)
        SessionManager.init(this)
        VersionHistoryStore.init(this)
        ApiConfigStore.init(this)
        BackgroundStore.init(this)
    }
}
