package com.github.mrshan23.pytestguard.llm

import com.github.mrshan23.pytestguard.llm.data.*
import com.github.mrshan23.pytestguard.test.TestAssembler
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.HttpRequests.HttpStatusException
import java.net.HttpURLConnection

class GeminiRequestManager(apiKey: String) {

    private val log = Logger.getInstance(this::class.java)

    private val gson = GsonBuilder().create()

    private val llmModel = "gemini-2.0-flash"

    var url: String = "https://generativelanguage.googleapis.com/v1beta/models/"

    init {
        url = "$url$llmModel:generateContent?key=$apiKey"
    }

    fun sendRequest(
        systemPrompt: String,
        userPrompt: String,
        indicator: ProgressIndicator,
        testAssembler: TestAssembler,
    ): Result<Unit> = try {
        HttpRequests
            .post(url, "application/json")
            .connect { request ->
                request.write(createGeminiRequestBodyJson(systemPrompt, userPrompt))
                val connection = request.connection as HttpURLConnection
                when (val responseCode = connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        Result.success(processGeminiResponse(request, indicator, testAssembler))
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
                contents = listOf(content)
            )
        )
    }

    private fun processGeminiResponse(
        httpRequest: HttpRequests.Request,
        indicator: ProgressIndicator,
        testAssembler: TestAssembler,
    ) {
        while (true) {
//            if (ToolUtils.isProcessCanceled(errorMonitor, indicator)) return

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

        log.debug(testAssembler.getContent())
    }


}