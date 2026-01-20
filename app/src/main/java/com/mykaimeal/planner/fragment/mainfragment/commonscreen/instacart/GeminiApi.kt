package com.mykaimeal.planner.fragment.mainfragment.commonscreen.instacart

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun analyzeImage(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): Response<GeminiResponse>

    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun analyzeHtml(
        @Query("key") apiKey: String,
        @Body body: GeminiRequest
    ): Response<GeminiResponse>
}
