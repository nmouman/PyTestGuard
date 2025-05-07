package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.data.TestCase
import com.intellij.openapi.progress.ProgressIndicator

class TestAssembler(
    val indicator: ProgressIndicator,
) {
    private var rawText = ""

    fun consume(text: String) {
        if (text.isEmpty()) return

        rawText = rawText.plus(text)
    }

    fun getContent(): String {
        return rawText
    }

    fun assembleTestSuite(testFramework: TestFramework): List<TestCase>? {
        val testParser = TestParser()
        //TODO: add check for empty generation + notification
        if (rawText.isEmpty()) {
            return null
        }
        val generatedTestSuite = testParser.parseTestSuiteFile(rawText) ?: return null
        return generatedTestSuite.assembleTestCasesForDisplay(testFramework == TestFramework.UNITTEST)
    }
}