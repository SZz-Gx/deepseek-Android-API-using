package com.example.deepseekchat.data.repository

import com.example.deepseekchat.data.api.CompletionRequest
import com.example.deepseekchat.data.api.Message
import com.example.deepseekchat.data.api.RetrofitClient
import com.example.deepseekchat.data.api.StreamChunk
import com.example.deepseekchat.data.api.StreamResponse
import com.example.deepseekchat.data.local.SecurePreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

// 聊天仓库 —— 构建请求、调用 API、翻译错误
// 支持非流式（Retrofit）和 SSE 流式（OkHttp 直接）两种模式

object ChatRepository {
    private val api = RetrofitClient.apiService

    // ── 非流式（保留兼容，目前未使用） ─────────────────

    suspend fun sendMessage(
        model: String,
        systemPrompt: String,
        history: List<Message>,
        userMessage: String,
        maxContextRounds: Int = 20
    ): Result<com.example.deepseekchat.data.api.CompletionResponse> {
        val messages = buildMessageList(systemPrompt, history, userMessage, maxContextRounds)
        val request = CompletionRequest(model = model, messages = messages, stream = false, temperature = 1.0, maxTokens = 4096)

        val apiKey = SecurePreferences.apiKey
        if (apiKey.isBlank()) return Result.failure(ApiKeyMissingException("API Key 未配置"))
        val authHeader = "Bearer $apiKey"

        return try {
            val response = api.chatCompletion(authHeader, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(IOException("响应体为空"))
            } else {
                val msg = when (response.code()) {
                    401 -> "API Key 无效（401），请检查 Key 是否正确"
                    429 -> "请求过于频繁（429），请稍后重试"
                    500, 502, 503 -> "DeepSeek 服务器错误（${response.code()}），请稍后重试"
                    else -> "请求失败（${response.code()}）: ${response.message()}"
                }
                Result.failure(HttpException(response.code(), msg))
            }
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(TimeoutException("请求超时，AI 回复可能需要较长时间，请重试"))
        } catch (e: java.net.UnknownHostException) {
            Result.failure(NetworkException("无法连接到 api.deepseek.com，请检查网络"))
        } catch (e: IOException) {
            Result.failure(NetworkException("网络请求失败: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── SSE 流式（当前主力） ───────────────────────────

    fun sendMessageStream(
        model: String,
        systemPrompt: String,
        history: List<Message>,
        userMessage: String,
        maxContextRounds: Int = 20
    ): Flow<StreamChunk> = callbackFlow {
        val messages = buildMessageList(systemPrompt, history, userMessage, maxContextRounds)
        val request = CompletionRequest(model = model, messages = messages, stream = true, temperature = 1.0, maxTokens = 4096)

        val apiKey = SecurePreferences.apiKey
        if (apiKey.isBlank()) {
            trySend(StreamChunk.Error("API Key 未配置"))
            close()
            return@callbackFlow
        }

        val jsonBody = RetrofitClient.moshi.adapter(CompletionRequest::class.java).toJson(request)
        val httpRequest = Request.Builder()
            .url("${RetrofitClient.BASE_URL}v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitClient.okHttp.newCall(httpRequest).execute()
            }

            if (!response.isSuccessful) {
                val msg = when (response.code) {
                    401 -> "API Key 无效（401）"
                    429 -> "请求过于频繁（429）"
                    else -> "请求失败（${response.code}）"
                }
                trySend(StreamChunk.Error(msg))
                close()
                return@callbackFlow
            }

            val body = response.body
            if (body == null) {
                trySend(StreamChunk.Error("响应体为空"))
                close()
                return@callbackFlow
            }

            withContext(Dispatchers.IO) {
                val reader = body.charStream().buffered()
                val streamAdapter = RetrofitClient.moshi.adapter(StreamResponse::class.java)

                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) return@forEachLine
                    if (trimmed == "data: [DONE]") return@forEachLine
                    if (!trimmed.startsWith("data: ")) return@forEachLine

                    val json = trimmed.removePrefix("data: ")
                    try {
                        val streamResponse = streamAdapter.fromJson(json) ?: return@forEachLine
                        val delta = streamResponse.choices.firstOrNull()?.delta ?: return@forEachLine
                        val content = delta.content
                        if (!content.isNullOrEmpty()) {
                            trySend(StreamChunk.Content(content))
                        }
                        streamResponse.usage?.let { usage ->
                            trySend(StreamChunk.Done(usage))
                        }
                    } catch (_: Exception) {
                        // 忽略格式异常的 SSE 行
                    }
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            trySend(StreamChunk.Error("请求超时"))
        } catch (e: java.net.UnknownHostException) {
            trySend(StreamChunk.Error("无法连接到 api.deepseek.com"))
        } catch (e: IOException) {
            trySend(StreamChunk.Error("网络请求失败: ${e.message}"))
        } catch (e: Exception) {
            trySend(StreamChunk.Error(e.message ?: "未知错误"))
        }

        close()
    }

    // ── 消息列表构建 ──────────────────────────────────

    private fun buildMessageList(
        systemPrompt: String,
        history: List<Message>,
        userMessage: String,
        maxContextRounds: Int
    ): List<Message> {
        val result = mutableListOf<Message>()
        val trimmedPrompt = systemPrompt.trim()
        if (trimmedPrompt.isNotEmpty()) {
            if (history.isEmpty() || history.first().role != "system")
                result.add(Message(role = "system", content = trimmedPrompt))
        }
        val maxMessages = maxContextRounds * 2
        val recentHistory = if (history.size > maxMessages) history.takeLast(maxMessages) else history
        val filteredHistory = if (recentHistory.isNotEmpty() && recentHistory.first().role == "system")
            recentHistory.drop(1) else recentHistory
        result.addAll(filteredHistory)
        result.add(Message(role = "user", content = userMessage))
        return result
    }
}

class ApiKeyMissingException(message: String) : Exception(message)
class HttpException(val code: Int, message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)
class TimeoutException(message: String) : Exception(message)
