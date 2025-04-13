package com.github.mrshan23.pytestguard.llm.prompt

import com.github.mrshan23.pytestguard.bundles.llm.LLMSettingsBundle
import com.github.mrshan23.pytestguard.psi.PsiFunctionContext
import com.github.mrshan23.pytestguard.psi.PsiHelper
import com.github.mrshan23.pytestguard.test.TestFramework
import com.intellij.openapi.application.ApplicationManager

class PromptGenerator(
    private val psiHelper: PsiHelper,
    private val caretOffset: Int,
    private val testFramework: TestFramework,
) {

    fun generateUserPrompt(): String {
        val userPromptTemplate = LLMSettingsBundle.get("userPrompt")

        if (userPromptTemplate.isEmpty()) {
            throw RuntimeException("User prompt template is empty")
        }

        val promptBuilder = PromptBuilder(userPromptTemplate)

        val userPrompt = StringBuilder()

        ApplicationManager.getApplication().runReadAction {
            val method = psiHelper.getFunctionForGeneration(caretOffset)
                ?: throw IllegalStateException("Method for generation not found at caret offset: $caretOffset")

            userPrompt.append(promptBuilder
                .insertMethodName(method.name!!)
                .insertTestFramework(testFramework)
                .insertImportedModules(method.containingFile.importStatements)
                .insertFileMethodSignatures(getFileMethodSignatures(method), method.name!!)
                .insertClassName(method.containingClass?.name, method.name!!)
                .insertGlobalFields(getGlobalFields(method), method.containingClass?.name)
                .insertClassMethodSignatures(getClassMethodSignatures(method), method.containingClass?.name)
                .insertMethodUnderTest(method.text!!)
                .build()
            )
        }

        return userPrompt.toString()
    }

    fun getSystemPrompt(): String {
        val systemPrompt = LLMSettingsBundle.get("systemPrompt")

        if (systemPrompt.isEmpty()) {
            throw RuntimeException("System prompt is empty")
        }

        return systemPrompt
    }

    private fun getClassMethodSignatures(psiFunction: PsiFunctionContext): List<String> {
        val methodSignatures = mutableListOf<String>()

        if (psiFunction.containingClass != null) {
            methodSignatures.addAll(psiFunction.containingClass.methodSignatures)
        }

        return methodSignatures
    }

    private fun getFileMethodSignatures(psiFunction: PsiFunctionContext): List<String> {
        val methodSignatures = mutableListOf<String>()

        methodSignatures.addAll(psiFunction.containingFile.methodSignatures)

        return methodSignatures
    }

    private fun getGlobalFields(psiFunction: PsiFunctionContext): List<String> {
        val globalFields = mutableListOf<String>()

        if (psiFunction.containingClass != null) {
            globalFields.addAll(psiFunction.containingClass.variablesInInit)
        }

        return globalFields
    }
}