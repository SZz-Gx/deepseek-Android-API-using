package com.example.deepseekchat.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// DeepSeek API 接口定义（Retrofit）

interface DeepSeekApiService {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: CompletionRequest
    ): Response<CompletionResponse>
}
