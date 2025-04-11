package com.github.mrshan23.pytestguard.test

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

    fun assembleTestSuite(testFramework: String): List<String>? {
        val testParser = TestParser()
        val generatedTestSuite = testParser.parseTestSuiteFile(rawText) ?: return null
        return generatedTestSuite.assembleTestCasesForDisplay(testFramework == TestFramework.UNITTEST.frameworkName)
    }
}