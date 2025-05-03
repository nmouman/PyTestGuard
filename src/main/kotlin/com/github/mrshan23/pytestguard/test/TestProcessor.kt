package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.settings.PluginSettingsService
import com.github.mrshan23.pytestguard.test.data.ExecutionResult
import com.github.mrshan23.pytestguard.utils.FileUtils
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.UUID

private val log = KotlinLogging.logger {}

class TestProcessor(
    val project: Project,
) {

    //TODO: pass indicator here to check if the process has been cancelled
    fun runTest(testCase: TestCase, testFramework: TestFramework): ExecutionResult {
        val testFilePath = createTemporaryTestFile(testCase)

        val testExecutor = TestExecutor(project)
        val result = testExecutor.executeTest(testFilePath, testFramework)

        // Get coverage data
        val testCaseStatementCoverage = testExecutor.getCoverageData(
            FileUtils.getCoverageTestCasePath(project)
        )
        result.statementCoverage = testCaseStatementCoverage.totals.percent_covered_display

        val settingsState = project.service<PluginSettingsService>().state
        val enableCoverageChangeMode = settingsState.enableCoverageChangeMode

        if (enableCoverageChangeMode) {
            val testSuiteResult = testExecutor.executeTestSuite(settingsState.testSuitePath, testFramework)
            if (testSuiteResult != null) {
                val testSuiteStatementCoverage = testExecutor.getCoverageData(
                    FileUtils.getCoverageTestSuitePath(project)
                )

                testExecutor.combineCoverageResults()

                val combinedTestsStatementCoverage = testExecutor.getCoverageData(
                    FileUtils.getCoverageCombinedTestsPath(project)
                )

                result.statementCoverageChange = getStatementCoverageChange(
                    testSuiteStatementCoverage.totals.percent_covered_display,
                    combinedTestsStatementCoverage.totals.percent_covered_display
                )
            }
        }

        FileUtils.removeFile(testFilePath)
        FileUtils.removeDirectory(FileUtils.getPyTestGuardResultsDirectoryPath(project))

        return result
    }

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

    private fun createTemporaryTestFile(testCase: TestCase): String {
        val testResultDirectory = FileUtils.getPyTestGuardResultsDirectoryPath(project)

        val tmpDir = File(testResultDirectory)
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        // Create a unique file name to avoid collisions
        val id = UUID.randomUUID().toString()
        val testFilePath = "$testResultDirectory${FileUtils.sanitizeFileName(testCase.testName)}_$id.py"

        val testFile = File(testFilePath)
        testFile.createNewFile()

        log.debug { "Save test in file " + testFile.absolutePath }

        testFile.writeText(testCase.testCode)

        return testFilePath
    }
}