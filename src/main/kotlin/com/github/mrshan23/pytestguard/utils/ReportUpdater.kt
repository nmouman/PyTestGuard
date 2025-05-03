package com.github.mrshan23.pytestguard.utils

import com.github.mrshan23.pytestguard.data.Report
import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.display.CoverageVisualisationTabBuilder

object ReportUpdater {
    fun updateTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
    ) {
        report.testCaseList.remove(testCase.id)
        report.testCaseList[testCase.id!!] = testCase
        coverageVisualisationTabBuilder.closeToolWindowTab()
    }

    fun removeTestCase(
        report: Report,
        testCase: TestCase,
        coverageVisualisationTabBuilder: CoverageVisualisationTabBuilder,
    ) {
        report.testCaseList.remove(testCase.id)
        coverageVisualisationTabBuilder.closeToolWindowTab()
    }
}