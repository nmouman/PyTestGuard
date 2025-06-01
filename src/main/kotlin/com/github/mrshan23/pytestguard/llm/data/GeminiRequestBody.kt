package com.github.mrshan23.pytestguard.llm.data


data class GeminiRequestBody(
    val system_instruction: SystemInstruction,
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)

data class SystemInstruction(
    val parts: List<InstructionPart>
)

data class InstructionPart(
    val text: String
)

data class Content(
    val parts: List<ContentPart>
)

data class ContentPart(
    val text: String
)

data class GenerationConfig(
    val temperature: Double
)


