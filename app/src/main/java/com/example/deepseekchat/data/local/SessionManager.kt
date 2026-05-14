package com.example.deepseekchat.data.local

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

data class ChatSession(
    val id: String,
    val title: String,
    val messageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 会话列表持久化（SharedPreferences + Moshi JSON）
// 每个会话的消息由 ChatHistoryStore 按 sessionId 分别存储

object SessionManager {
    private const val PREFS_FILE = "deepseek_sessions"
    private const val KEY_SESSIONS = "sessions_json"
    private const val KEY_CURRENT = "current_session_id"
    private const val DEFAULT_TITLE = "新对话"

    private lateinit var prefs: android.content.SharedPreferences
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val sessionListType = Types.newParameterizedType(List::class.java, ChatSession::class.java)
    private val sessionListAdapter = moshi.adapter<List<ChatSession>>(sessionListType)

    private var cachedSessions: List<ChatSession> = emptyList()
    private var currentSessionId: String = ""

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        currentSessionId = prefs.getString(KEY_CURRENT, "") ?: ""
        cachedSessions = loadSessionsFromPrefs()

        // 首次启动：创建默认会话，触发旧消息迁移
        if (cachedSessions.isEmpty()) {
            val defaultSession = createSessionInternal(DEFAULT_TITLE)
            currentSessionId = defaultSession.id
            cachedSessions = listOf(defaultSession)
            saveCurrentSessionId()
            ChatHistoryStore.migrateOldMessages(defaultSession.id)
        }
        // 如果当前会话 ID 无效，回退到第一个会话
        else if (cachedSessions.none { it.id == currentSessionId }) {
            currentSessionId = cachedSessions.first().id
            saveCurrentSessionId()
        }
    }

    fun getAllSessions(): List<ChatSession> = cachedSessions
    fun getCurrentSessionId(): String = currentSessionId

    fun createSession(title: String = DEFAULT_TITLE): ChatSession {
        val session = createSessionInternal(title)
        cachedSessions = listOf(session) + cachedSessions
        saveSessionsToPrefs()
        switchToSession(session.id)
        return session
    }

    fun switchToSession(sessionId: String) {
        if (currentSessionId != sessionId) {
            currentSessionId = sessionId
            saveCurrentSessionId()
        }
    }

    fun updateSession(session: ChatSession) {
        cachedSessions = cachedSessions.map { if (it.id == session.id) session else it }
        saveSessionsToPrefs()
    }

    fun deleteSession(sessionId: String) {
        cachedSessions = cachedSessions.filter { it.id != sessionId }
        ChatHistoryStore.deleteSession(sessionId)
        if (currentSessionId == sessionId) {
            currentSessionId = cachedSessions.firstOrNull()?.id ?: ""
            saveCurrentSessionId()
        }
        saveSessionsToPrefs()
        if (cachedSessions.isEmpty()) {
            val session = createSessionInternal(DEFAULT_TITLE)
            currentSessionId = session.id
            cachedSessions = listOf(session)
            saveCurrentSessionId()
        }
    }

    fun getSession(sessionId: String): ChatSession? = cachedSessions.find { it.id == sessionId }

    private fun createSessionInternal(title: String): ChatSession {
        return ChatSession(id = UUID.randomUUID().toString(), title = title)
    }

    private fun saveCurrentSessionId() {
        prefs.edit().putString(KEY_CURRENT, currentSessionId).apply()
    }

    private fun saveSessionsToPrefs() {
        prefs.edit().putString(KEY_SESSIONS, sessionListAdapter.toJson(cachedSessions)).apply()
    }

    private fun loadSessionsFromPrefs(): List<ChatSession> {
        val json = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return try { sessionListAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
    }
}
