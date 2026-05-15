package com.example.deepseekchat.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// DeepSeek API 请求/响应数据类
// 完全匹配 https://api.deepseek.com/v1/chat/completions 的 JSON 结构

// ── 非流式请求/响应 ──────────────────────────────────

@JsonClass(generateAdapter = true)
data class CompletionRequest(
    val model: String,
    val messages: List<Message>,
    @Json(name = "stream") val stream: Boolean = false,
    val temperature: Double = 1.0,
    @Json(name = "max_tokens") val maxTokens: Int = 8192
)

@JsonClass(generateAdapter = true)
data class Message(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class CompletionResponse(
    val id: String? = null,
    @Json(name = "object") val obj: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice> = emptyList(),
    val usage: Usage = Usage()
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int = 0,
    val message: Message? = null,
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class Usage(
    @Json(name = "prompt_tokens") val promptTokens: Int = 0,
    @Json(name = "completion_tokens") val completionTokens: Int = 0,
    @Json(name = "total_tokens") val totalTokens: Int = 0,
    @Json(name = "prompt_cache_hit_tokens") val promptCacheHitTokens: Int = 0,
    @Json(name = "prompt_cache_miss_tokens") val promptCacheMissTokens: Int = 0
)

// ── SSE 流式响应解析 ──────────────────────────────────

@JsonClass(generateAdapter = true)
data class StreamDelta(
    val role: String? = null,
    val content: String? = null,
    @Json(name = "reasoning_content") val reasoningContent: String? = null
)

@JsonClass(generateAdapter = true)
data class StreamChoice(
    val index: Int = 0,
    val delta: StreamDelta = StreamDelta(),
    @Json(name = "finish_reason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class StreamResponse(
    val id: String? = null,
    val choices: List<StreamChoice> = emptyList(),
    val usage: Usage? = null
)

// ── 流式事件封装 ──────────────────────────────────────

sealed class StreamChunk {
    data class Reasoning(val text: String) : StreamChunk()
    data class Content(val text: String) : StreamChunk()
    data class Done(val usage: Usage) : StreamChunk()
    data class Error(val message: String) : StreamChunk()
}
