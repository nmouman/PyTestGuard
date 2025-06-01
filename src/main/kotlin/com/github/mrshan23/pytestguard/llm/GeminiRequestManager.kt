package com.github.mrshan23.pytestguard.llm

import com.github.mrshan23.pytestguard.llm.data.*
import com.github.mrshan23.pytestguard.test.TestAssembler
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.HttpRequests.HttpStatusException
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.HttpURLConnection

private val log = KotlinLogging.logger {}

class GeminiRequestManager(apiKey: String) {

    private val gson = GsonBuilder().create()

    private val llmModel = "gemini-2.0-flash"

    var url: String = "https://generativelanguage.googleapis.com/v1beta/models/"

    init {
        url = "$url$llmModel:generateContent?key=$apiKey"
    }

    fun sendRequest(
        systemPrompt: String,
        userPrompt: String,
        testAssembler: TestAssembler,
    ): Result<Unit> = try {
        HttpRequests
            .post(url, "application/json")
            .connect { request ->
                request.write(createGeminiRequestBodyJson(systemPrompt, userPrompt))
                val connection = request.connection as HttpURLConnection
                when (val responseCode = connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        Result.success(processGeminiResponse(request, testAssembler))
                }
                    else -> Result.failure(HttpStatusException(connection.responseMessage, responseCode, url))
                }
            }
    } catch (exception: HttpStatusException) {
        Result.failure(exception)
    }

    private fun createGeminiRequestBodyJson(systemPrompt: String, userPrompt: String): String {
        val systemInstruction = SystemInstruction(listOf(InstructionPart(systemPrompt)))
        val content = Content(listOf(ContentPart(userPrompt)))

        return gson.toJson(
            GeminiRequestBody(
                system_instruction = systemInstruction,
                contents = listOf(content),
                generationConfig = GenerationConfig(0.5)
            )
        )
    }

    private fun processGeminiResponse(
        httpRequest: HttpRequests.Request,
        testAssembler: TestAssembler,
    ) {
        while (true) {

            val text = httpRequest.reader.readText()
            val result =
                gson.fromJson(
                    JsonParser.parseString(text)
                        .asJsonObject["candidates"]
                        .asJsonArray[0].asJsonObject,
                    GeminiResponseBody::class.java,
                )

            testAssembler.consume(result.content.parts[0].text)

            if (result.finishReason == "STOP") break
        }

        log.debug {testAssembler.getContent() }
    }


}