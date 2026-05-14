package com.example.deepseekchat.data.local

import android.content.Context
import com.example.deepseekchat.data.api.Message
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// 对话历史持久化 — 按 sessionId 隔离存储
// 首次升级时自动迁移旧版单一 key 的消息到默认会话

object ChatHistoryStore {
    private const val PREFS_FILE = "deepseek_chat_history"
    private const val KEY_MESSAGES_OLD = "messages_json"  // 旧版（v1）单一 key
    private const val MAX_MESSAGES = 100

    private lateinit var prefs: android.content.SharedPreferences
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val messageListType = Types.newParameterizedType(List::class.java, Message::class.java)
    private val messageListAdapter = moshi.adapter<List<Message>>(messageListType)

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
    }

    private fun keyForSession(sessionId: String) = "messages_$sessionId"

    fun saveMessages(sessionId: String, messages: List<Message>) {
        val toSave = if (messages.size > MAX_MESSAGES) messages.takeLast(MAX_MESSAGES) else messages
        prefs.edit().putString(keyForSession(sessionId), messageListAdapter.toJson(toSave)).apply()
    }

    fun loadMessages(sessionId: String): List<Message> {
        val json = prefs.getString(keyForSession(sessionId), null) ?: return emptyList()
        return try { messageListAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    fun clearMessages(sessionId: String) {
        prefs.edit().remove(keyForSession(sessionId)).apply()
    }

    fun deleteSession(sessionId: String) {
        prefs.edit().remove(keyForSession(sessionId)).apply()
    }

    // 将旧版单一 key 的消息迁移到指定会话（仅调用一次）
    fun migrateOldMessages(sessionId: String): Boolean {
        val oldJson = prefs.getString(KEY_MESSAGES_OLD, null) ?: return false
        val messages = try { messageListAdapter.fromJson(oldJson) } catch (_: Exception) { null } ?: return false
        if (messages.isEmpty()) {
            prefs.edit().remove(KEY_MESSAGES_OLD).apply()
            return false
        }
        saveMessages(sessionId, messages)
        prefs.edit().remove(KEY_MESSAGES_OLD).apply()
        return true
    }
}
