package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.display.custom.CustomProgressIndicator
import com.github.mrshan23.pytestguard.monitor.ErrorMonitor
import com.github.mrshan23.pytestguard.utils.isProcessStopped

class TestAssembler(
    val errorMonitor: ErrorMonitor,
    val indicator: CustomProgressIndicator,
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
        if (isProcessStopped(errorMonitor, indicator)) return null

        val testParser = TestParser()
        if (rawText.isEmpty()) {
            return null
        }
        val generatedTestSuite = testParser.parseTestSuiteFile(rawText) ?: return null

        if (isProcessStopped(errorMonitor, indicator)) return null

        return generatedTestSuite.assembleTestCasesForDisplay(testFramework == TestFramework.UNITTEST)
    }
}