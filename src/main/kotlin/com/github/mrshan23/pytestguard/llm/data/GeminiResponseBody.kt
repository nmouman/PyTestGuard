package com.github.mrshan23.pytestguard.llm.data

data class GeminiResponseBody(
    val content: GeminiResponseContent,
    val finishReason: String,
    val avgLogprobs: Double,
)

data class GeminiResponseContent(
    val parts: List<GeminiResponsePart>,
    val role: String?,
)

data class GeminiResponsePart(
    val text: String,
)