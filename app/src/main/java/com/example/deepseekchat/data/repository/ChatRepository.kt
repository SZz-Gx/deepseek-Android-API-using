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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object ChatRepository {
    private val api = RetrofitClient.apiService

    suspend fun sendMessage(
        model: String, systemPrompt: String, history: List<Message>,
        userMessage: String, maxContextRounds: Int = 20
    ): Result<com.example.deepseekchat.data.api.CompletionResponse> {
        val messages = buildMessageList(systemPrompt, history, userMessage, maxContextRounds)
        val request = CompletionRequest(model = model, messages = messages, stream = false)
        val apiKey = SecurePreferences.apiKey
        if (apiKey.isBlank()) return Result.failure(ApiKeyMissingException("API Key 未配置"))
        return try {
            val response = api.chatCompletion("Bearer $apiKey", request)
            if (response.isSuccessful) response.body()?.let { Result.success(it) } ?: Result.failure(IOException("响应体为空"))
            else Result.failure(HttpException(response.code(), when (response.code()) {
                401 -> "API Key 无效"; 429 -> "请求频繁"; else -> "请求失败（${response.code()}）"
            }))
        } catch (e: java.net.SocketTimeoutException) { Result.failure(TimeoutException("请求超时")) }
        catch (e: java.net.UnknownHostException) { Result.failure(NetworkException("无法连接")) }
        catch (e: IOException) { Result.failure(NetworkException(e.message ?: "网络错误")) }
        catch (e: Exception) { Result.failure(e) }
    }

    fun sendMessageStream(
        model: String, systemPrompt: String, history: List<Message>,
        userMessage: String, maxContextRounds: Int = 20
    ): Flow<StreamChunk> = callbackFlow {
        val messages = buildMessageList(systemPrompt, history, userMessage, maxContextRounds)
        val request = CompletionRequest(model = model, messages = messages, stream = true)
        val apiKey = SecurePreferences.apiKey
        if (apiKey.isBlank()) { send(StreamChunk.Error("API Key 未配置")); close(); return@callbackFlow }

        val jsonBody = RetrofitClient.moshi.adapter(CompletionRequest::class.java).toJson(request)
        val httpRequest = Request.Builder()
            .url("${RetrofitClient.BASE_URL}v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            val response = withContext(Dispatchers.IO) { RetrofitClient.okHttp.newCall(httpRequest).execute() }
            if (!response.isSuccessful) {
                send(StreamChunk.Error(when (response.code) { 401 -> "API Key 无效"; 429 -> "请求频繁"; else -> "请求失败（${response.code}）" }))
                close(); return@callbackFlow
            }
            val body = response.body ?: run { send(StreamChunk.Error("响应为空")); close(); return@callbackFlow }

            withContext(Dispatchers.IO) {
                val reader = BufferedReader(InputStreamReader(body.byteStream()))
                val adapter = RetrofitClient.moshi.adapter(StreamResponse::class.java)
                var usage: com.example.deepseekchat.data.api.Usage? = null

                var line: String? = reader.readLine()
                while (line != null) {
                    val t = line!!.trim()
                    if (t.isNotEmpty() && t.startsWith("data: ")) {
                        val json = t.removePrefix("data: ")
                        if (json == "[DONE]") {
                            usage?.let { send(StreamChunk.Done(it)) }
                            break
                        }
                        try {
                            val sr = adapter.fromJson(json)
                            sr?.usage?.let { usage = it }
                            val delta = sr?.choices?.firstOrNull()?.delta
                            val reasoning = delta?.reasoningContent
                            val content = delta?.content
                            if (!reasoning.isNullOrEmpty()) {
                                send(StreamChunk.Reasoning(reasoning))
                            }
                            if (!content.isNullOrEmpty()) {
                                send(StreamChunk.Content(content))
                            }
                        } catch (_: Exception) {}
                    }
                    line = reader.readLine()
                }
                if (usage != null && line == null) send(StreamChunk.Done(usage))
            }
        } catch (e: java.net.SocketTimeoutException) { send(StreamChunk.Error("请求超时")) }
        catch (e: java.net.UnknownHostException) { send(StreamChunk.Error("无法连接")) }
        catch (e: IOException) { send(StreamChunk.Error(e.message ?: "网络错误")) }
        catch (e: Exception) { send(StreamChunk.Error(e.message ?: "未知错误")) }
        close()
    }

    private fun buildMessageList(
        systemPrompt: String, history: List<Message>, userMessage: String, maxContextRounds: Int
    ): List<Message> {
        val result = mutableListOf<Message>()
        val p = systemPrompt.trim()
        if (p.isNotEmpty() && (history.isEmpty() || history.first().role != "system"))
            result.add(Message("system", p))
        val max = maxContextRounds * 2
        val recent = if (history.size > max) history.takeLast(max) else history
        val filtered = if (recent.isNotEmpty() && recent.first().role == "system") recent.drop(1) else recent
        result.addAll(filtered)
        result.add(Message("user", userMessage))
        return result
    }
}

class ApiKeyMissingException(message: String) : Exception(message)
class HttpException(val code: Int, message: String) : Exception(message)
class NetworkException(message: String) : Exception(message)
class TimeoutException(message: String) : Exception(message)
