package com.example.deepseekchat.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.deepseekchat.data.api.Message
import com.example.deepseekchat.data.api.StreamChunk
import com.example.deepseekchat.data.api.Usage
import com.example.deepseekchat.data.local.BalanceManager
import com.example.deepseekchat.data.local.ChatHistoryStore
import com.example.deepseekchat.data.local.ChatSession
import com.example.deepseekchat.data.local.SecurePreferences
import com.example.deepseekchat.data.local.SessionManager
import com.example.deepseekchat.data.repository.ChatRepository
import com.example.deepseekchat.utils.CostCalculator
import com.example.deepseekchat.utils.NetworkUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _lastUsage = MutableStateFlow<Usage?>(null)
    val lastUsage: StateFlow<Usage?> = _lastUsage.asStateFlow()
    private val _lastCost = MutableStateFlow(BigDecimal.ZERO)
    val lastCost: StateFlow<BigDecimal> = _lastCost.asStateFlow()
    private val _snackbar = MutableSharedFlow<String>()
    val snackbar: SharedFlow<String> = _snackbar.asSharedFlow()

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()
    private var currentSessionId: String = SessionManager.getCurrentSessionId()

    var model: String = "deepseek-chat"
    var systemPrompt: String = ""
    var maxContextRounds: Int = 20
    var inputPriceMiss: BigDecimal = BigDecimal("1")
    var inputPriceHit: BigDecimal = BigDecimal("0.02")
    var outputPrice: BigDecimal = BigDecimal("2")

    init {
        _messages.value = ChatHistoryStore.loadMessages(currentSessionId)
        _sessions.value = SessionManager.getAllSessions()
    }

    val currentSessionIdProp: String get() = currentSessionId

    val currentSessionTitle: String
        get() = SessionManager.getSession(currentSessionId)?.title ?: "DeepSeek Chat"

    // ── 会话管理 ───────────────────────────────────────

    fun newSession() {
        val session = SessionManager.createSession()
        switchToSession(session.id)
    }

    fun switchToSession(sessionId: String) {
        if (sessionId == currentSessionId) return
        // 保存当前会话状态
        ChatHistoryStore.saveMessages(currentSessionId, _messages.value)
        currentSessionId = sessionId
        SessionManager.switchToSession(sessionId)
        _messages.value = ChatHistoryStore.loadMessages(sessionId)
        _lastUsage.value = null
        _lastCost.value = BigDecimal.ZERO
        _sessions.value = SessionManager.getAllSessions()
    }

    fun deleteSession(sessionId: String) {
        SessionManager.deleteSession(sessionId)
        _sessions.value = SessionManager.getAllSessions()
        currentSessionId = SessionManager.getCurrentSessionId()
        _messages.value = ChatHistoryStore.loadMessages(currentSessionId)
        _lastUsage.value = null
        _lastCost.value = BigDecimal.ZERO
    }

    // ── 消息发送 ───────────────────────────────────────

    fun sendMessage(content: String) {
        val trimmed = content.trim()
        if (trimmed.isEmpty()) return
        val context = getApplication<Application>()

        if (!NetworkUtils.isNetworkAvailable(context)) {
            viewModelScope.launch { _snackbar.emit("网络连接不可用，请检查网络后重试") }
            return
        }
        if (!SecurePreferences.hasApiKey) {
            viewModelScope.launch { _snackbar.emit("请先在设置中配置 API Key") }
            return
        }

        val isFirstMessage = _messages.value.none { it.role != "system" }

        viewModelScope.launch {
            val userMessage = Message(role = "user", content = trimmed)
            val historyBeforeSend = _messages.value.toList()
            _messages.value = historyBeforeSend + userMessage + Message(role = "assistant", content = "")
            _isLoading.value = true

            repository.sendMessageStream(model, systemPrompt, historyBeforeSend, trimmed, maxContextRounds)
                .collect { chunk ->
                    when (chunk) {
                        is StreamChunk.Content -> {
                            val msgs = _messages.value.toMutableList()
                            val last = msgs.last()
                            msgs[msgs.lastIndex] = last.copy(content = last.content + chunk.text)
                            _messages.value = msgs
                        }
                        is StreamChunk.Done -> {
                            val usage = chunk.usage
                            _lastUsage.value = usage
                            val cost = CostCalculator.calculate(usage, inputPriceMiss, inputPriceHit, outputPrice)
                            _lastCost.value = cost
                            BalanceManager.addCost(cost)
                            ChatHistoryStore.saveMessages(currentSessionId, _messages.value)
                            updateSessionTitle(isFirstMessage, trimmed)
                            _isLoading.value = false
                        }
                        is StreamChunk.Error -> {
                            _messages.value = historyBeforeSend
                            _snackbar.emit(chunk.message)
                            _isLoading.value = false
                        }
                    }
                }
        }
    }

    private fun updateSessionTitle(isFirstMessage: Boolean, content: String) {
        if (!isFirstMessage) return
        val title = if (content.length > 30) content.take(30) + "…" else content
        val session = SessionManager.getSession(currentSessionId) ?: return
        SessionManager.updateSession(session.copy(title = title, updatedAt = System.currentTimeMillis()))
        _sessions.value = SessionManager.getAllSessions()
    }

    fun clearChat() {
        _messages.value = emptyList()
        _lastUsage.value = null
        _lastCost.value = BigDecimal.ZERO
        ChatHistoryStore.clearMessages(currentSessionId)
    }

    fun deleteRound(index: Int) {
        val cur = _messages.value.toMutableList()
        if (index !in cur.indices) return
        when (cur[index].role) {
            "user" -> { cur.removeAt(index); if (index < cur.size && cur[index].role == "assistant") cur.removeAt(index) }
            "assistant" -> { cur.removeAt(index); if (index - 1 in cur.indices && cur[index - 1].role == "user") cur.removeAt(index - 1) }
            "system" -> cur.removeAt(index)
        }
        _messages.value = cur
        ChatHistoryStore.saveMessages(currentSessionId, cur)
    }

    fun resendLast() {
        val cur = _messages.value.toMutableList()
        if (cur.isEmpty()) return
        val idx = cur.indexOfLast { it.role == "user" }
        if (idx == -1) return
        val content = cur[idx].content
        _messages.value = cur.subList(0, idx)
        sendMessage(content)
    }

    // ── 导出 Markdown ──────────────────────────────────

    fun exportToMarkdown(): String {
        val sb = StringBuilder()
        val title = currentSessionTitle
        sb.appendLine("# $title")
        sb.appendLine()
        _messages.value.forEach { msg ->
            when (msg.role) {
                "system" -> { sb.appendLine("> ${msg.content}"); sb.appendLine() }
                "user" -> { sb.appendLine("**你:**"); sb.appendLine(msg.content); sb.appendLine() }
                "assistant" -> { sb.appendLine("**DeepSeek:**"); sb.appendLine(msg.content); sb.appendLine() }
            }
        }
        return sb.toString()
    }
}
