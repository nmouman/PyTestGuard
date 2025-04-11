package com.github.mrshan23.pytestguard.utils

import com.github.mrshan23.pytestguard.data.Report
import com.github.mrshan23.pytestguard.data.TestCase

object ReportUpdater {
    fun updateTestCase(
        report: Report,
        testCase: TestCase,
    ) {
        report.testCaseList.remove(testCase.id)
        report.testCaseList[testCase.id] = testCase
    }

    fun removeTestCase(
        report: Report,
        testCase: TestCase,
    ) {
        report.testCaseList.remove(testCase.id)
    }
}