package com.mykaimeal.planner.fragment.mainfragment.commonscreen.instacart

data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val inline_data: InlineData? = null,
    val text: String? = null
)

data class InlineData(
    val mime_type: String,
    val data: String
)

data class GenerationConfig(
    val temperature: Double,
    val maxOutputTokens: Int
)
