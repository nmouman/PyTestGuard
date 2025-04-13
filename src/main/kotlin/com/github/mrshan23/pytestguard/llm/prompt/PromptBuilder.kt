package com.github.mrshan23.pytestguard.llm.prompt

import com.github.mrshan23.pytestguard.test.TestFramework
import java.util.*

class PromptBuilder(private val promptTemplate: String) {

    private val insertedKeywordValues: EnumMap<PromptKeyword, String> = EnumMap(PromptKeyword::class.java)

    // collect all the keywords present in the prompt template
    private val templateKeywords: List<PromptKeyword> = buildList {
        for (keyword in PromptKeyword.entries) {
            if (promptTemplate.contains(keyword.variable)) {
                add(keyword)
            }
        }
    }

    /**
     * Builds the prompt by populating the template with the inserted values
     * and validating that all mandatory keywords were provided.
     *
     * @return The built prompt.
     * @throws IllegalStateException if a mandatory keyword is not present in the template.
     */
    fun build(): String {
        var populatedPrompt = promptTemplate

        // populate the template with the inserted values
        for ((keyword, value) in insertedKeywordValues.entries) {
            populatedPrompt = populatedPrompt.replace(keyword.variable, value, ignoreCase = false)
        }

        // validate that all mandatory keywords were provided
        for (keyword in templateKeywords) {
            if (!insertedKeywordValues.contains(keyword) && keyword.mandatory) {
                throw IllegalStateException("The prompt must contain ${keyword.name} keyword")
            }
        }

        return populatedPrompt
    }

    /**
     * Inserts a keyword and its corresponding value into the prompt template.
     * If the keyword is marked as mandatory and not present in the template, an IllegalArgumentException is thrown.
     *
     * @param keyword The keyword to be inserted.
     * @param value The value corresponding to the keyword.
     * @throws IllegalArgumentException if a mandatory keyword is not present in the template.
     */
    private fun insert(keyword: PromptKeyword, value: String) {
        if (!templateKeywords.contains(keyword) && keyword.mandatory) {
            throw IllegalArgumentException("Prompt template does not contain mandatory ${keyword.name}")
        }
        insertedKeywordValues[keyword] = value
    }

    fun insertMethodName(methodName: String) = apply {
        insert(PromptKeyword.METHOD_NAME, methodName)
    }

    fun insertTestFramework(testFramework: TestFramework) = apply {
        insert(PromptKeyword.TEST_FRAMEWORK, testFramework.frameworkName)
    }

    fun insertImportedModules(importedModules: List<String>?) = apply {
        if (importedModules.isNullOrEmpty()) {
            insert(PromptKeyword.IMPORTED_MODULES, "")
            return@apply
        }

        val fullText = StringBuilder()

        fullText.append("\nThe following modules have been imported:\n")

        for (importedModule in importedModules) {
            fullText.append("$importedModule\n")
        }

        insert(PromptKeyword.IMPORTED_MODULES, fullText.toString())
    }

    fun insertClassName(className: String?, methodName: String) = apply {
        if (className.isNullOrEmpty()) {
            insert(PromptKeyword.CLASS_NAME, "")
            return@apply
        }

        insert(PromptKeyword.CLASS_NAME, "\nThe method '$methodName' is a member of the '$className' class. This class is already defined elsewhere.\n")
    }

    fun insertGlobalFields(globalFields: List<String>, className: String?) = apply {
        if (globalFields.isEmpty()) {
            insert(PromptKeyword.GLOBAL_FIELDS, "")
            return@apply
        }

        val fullText = StringBuilder()

        fullText.append("\nThe following variable are declared in the __init__ of the class '$className':\n")

        for (globalField in globalFields) {
            fullText.append("$globalField\n")
        }

        insert(PromptKeyword.GLOBAL_FIELDS, fullText.toString())
    }

    fun insertClassMethodSignatures(methodSignatures: List<String>, className: String?) = apply {
        if (methodSignatures.isEmpty()) {
            insert(PromptKeyword.CLASS_METHOD_SIGNATURES, "")
            return@apply
        }

        val fullText = StringBuilder()

        fullText.append("\nThe following method signatures are declared in the class '$className':\n")

        for (methodSignature in methodSignatures) {
            fullText.append("$methodSignature\n")
        }

        insert(PromptKeyword.CLASS_METHOD_SIGNATURES, fullText.toString())
    }

    fun insertFileMethodSignatures(methodSignatures: List<String>, methodName: String) = apply {
        if (methodSignatures.isEmpty()) {
            insert(PromptKeyword.FILE_METHOD_SIGNATURES, "")
            return@apply
        }

        val fullText = StringBuilder()

        fullText.append("\nThe following method signatures are declared same file as the method '$methodName':\n")


        for (methodSignature in methodSignatures) {
            fullText.append("$methodSignature\n")
        }

        insert(PromptKeyword.FILE_METHOD_SIGNATURES, fullText.toString())
    }

    fun insertMethodUnderTest(methodUnderTest: String) = apply {
        insert(PromptKeyword.METHOD_UNDER_TEST, "```\n${methodUnderTest}\n```\n")
    }

}