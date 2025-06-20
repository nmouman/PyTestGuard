package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.display.custom.CustomProgressIndicator
import com.github.mrshan23.pytestguard.settings.PluginSettingsService
import com.github.mrshan23.pytestguard.test.data.ExecutionResult
import com.github.mrshan23.pytestguard.utils.FileUtils
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class TestProcessor(
    val project: Project,
    private val customProgressIndicator: CustomProgressIndicator
) {

    /*
     * Executes a test case and returns the result.
     *
     * @param testCase The test case to execute.
     * @param testFramework The test framework to use (e.g., pytest, unittest).
     * @return The execution result.
     */
    fun runTest(testCase: TestCase, testFramework: TestFramework): ExecutionResult? {

        val testFilePath = FileUtils.getTestCasePath(testCase, project)

        if (customProgressIndicator.isCanceled()) return null

        val testExecutor = TestExecutor(project)
        val result = testExecutor.executeTest(testFilePath, testFramework) ?: return null

        if (customProgressIndicator.isCanceled()) return null

        // Get the coverage data for the test case
        val testCaseStatementCoverage = testExecutor.getCoverageData(
            FileUtils.getCoverageTestCasePath(project)
        ) ?: return null
        result.statementCoverage = testCaseStatementCoverage.totals.percent_covered_display

        val settingsState = project.service<PluginSettingsService>().state
        val enableCoverageChangeMode = settingsState.enableCoverageChangeMode

        // Check if coverage change mode is enabled
        if (enableCoverageChangeMode) {
            log.debug { "Calculating coverage of test suite" }

            if (customProgressIndicator.isCanceled()) return null

            val testSuiteResult = testExecutor.executeTestSuite(settingsState.testSuitePath, testFramework)
            if (testSuiteResult != null) {
                // Get the coverage data for the test suite
                val testSuiteStatementCoverage = testExecutor.getCoverageData(
                    FileUtils.getCoverageTestSuitePath(project)
                ) ?: return null

                if (customProgressIndicator.isCanceled()) return null

                // Combine the test suite and test case coverage data
                testExecutor.combineCoverageResults()

                // Get the coverage data for the combined tests
                val combinedTestsStatementCoverage = testExecutor.getCoverageData(
                    FileUtils.getCoverageCombinedTestsPath(project)
                ) ?: return null

                result.statementCoverageChange = getStatementCoverageChange(
                    testSuiteStatementCoverage.totals.percent_covered_display,
                    combinedTestsStatementCoverage.totals.percent_covered_display
                )
            }
        }

        return result
    }

    /*
     * Calculates the change in statement coverage between the original test suite
     * and the test suite combined with the test case.
     *
     * @param testSuiteStatementCoverage The statement coverage of the test suite.
     * @param combinedTestsStatementCoverage The statement coverage of the combination of the test suite and the test case.
     * @return The change in statement coverage as a string.
     */
    private fun getStatementCoverageChange(
        testSuiteStatementCoverage: String,
        combinedTestsStatementCoverage: String
    ): String {
        val combinedTestsCoverage = combinedTestsStatementCoverage.toInt()
        val testSuiteCoverage = testSuiteStatementCoverage.toInt()

        if (combinedTestsCoverage > testSuiteCoverage) {
            return "${combinedTestsCoverage - testSuiteCoverage}"
        }

        return "0"
    }
}