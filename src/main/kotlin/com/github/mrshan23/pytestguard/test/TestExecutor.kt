package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.bundles.plugin.PluginMessagesBundle
import com.github.mrshan23.pytestguard.test.data.CoverageResult
import com.github.mrshan23.pytestguard.test.data.ExecutionResult
import com.github.mrshan23.pytestguard.utils.CommandLineRunner
import com.github.mrshan23.pytestguard.utils.FileUtils
import com.github.mrshan23.pytestguard.utils.createWarningNotification
import com.google.gson.Gson
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger {}

class TestExecutor(
    val project: Project,
) {

    /*
     * Executes a test case and returns the result.
     *
     * @param testFilePath The path to the test file.
     * @param testFramework The test framework to use (e.g., pytest, unittest).
     * @return The execution result.
     */
    fun executeTest(testFilePath: String, testFramework: TestFramework): ExecutionResult? {
        log.info { "Executing test at path: $testFilePath" }

        val pythonSdk = findPythonSDKHomePath() ?: return null

        val resultsPath = FileUtils.getPyTestGuardResultsDirectoryPath(project)
        val coverageFilePath = FileUtils.getCoverageTestCasePath(project)

        val baseCommand = arrayListOf(
            pythonSdk, "-m", "coverage", "run",
            "--source=.", "--omit=${resultsPath}*",
            "--omit=C:${File.separatorChar}gitHub${File.separatorChar}beancount${File.separatorChar}tests${File.separatorChar}*", // Hardcode test suite path for experiment
            "--data-file=$coverageFilePath"
        )

        val command = when(testFramework) {
            TestFramework.PYTEST -> {
                baseCommand + arrayListOf("-m", "pytest", testFilePath)
            }
            TestFramework.UNITTEST -> {
                baseCommand + arrayListOf(testFilePath)
            }
        }

        val result = CommandLineRunner.run(command, project)

        log.debug { "Test execution finished with exit code: ${result.exitCode}" }
        log.debug { "Test execution message: ${result.executionMessage}" }

        return result
    }

    /**
     * Executes a test suite and returns the result.
     *
     * @param testSuitePath The path to the test suite.
     * @param testFramework The test framework to use (e.g., pytest, unittest).
     * @return The execution result, or null if the test suite path is empty.
     */
    fun executeTestSuite(testSuitePath: String, testFramework: TestFramework): ExecutionResult? {

        if (testSuitePath.isEmpty()) {
            log.error { "Test suite path is empty" }

            createWarningNotification(
                PluginMessagesBundle.get("missingTestSuitePathTitle"),
                PluginMessagesBundle.get("missingTestSuitePathMessage"),
                project
            )

            return null
        }

        val pythonSdk = findPythonSDKHomePath() ?: return null

        val resultsPath = FileUtils.getPyTestGuardResultsDirectoryPath(project)
        val coverageFilePath = FileUtils.getCoverageTestSuitePath(project)

        val baseCommand = arrayListOf(
            pythonSdk, "-m", "coverage", "run",
            "--source=.", "--omit=${resultsPath}*",
            "--data-file=$coverageFilePath", "-m",
        )

        val command = when(testFramework) {
            TestFramework.PYTEST -> {
                baseCommand + arrayListOf("pytest", testSuitePath)
            }
            TestFramework.UNITTEST -> {
                baseCommand + arrayListOf("unittest", "discover", "-s", testSuitePath)
            }
        }

        val result = CommandLineRunner.run(command, project)

        log.debug { "Test execution finished with exit code: ${result.exitCode}" }
        log.debug { "Test execution message: ${result.executionMessage}" }

        return result
    }

    /**
     * Retrieves coverage data from the specified coverage file path.
     *
     * @param coverageFilePath The path to the coverage file.
     * @return The coverage result.
     */
    fun getCoverageData(coverageFilePath: String): CoverageResult? {
        val pythonSdk = findPythonSDKHomePath() ?: return null

        val coverageJsonPath = FileUtils.getCoverageJsonPath(project)

        val command = arrayListOf(
            pythonSdk, "-m", "coverage", "json",
            "--data-file=$coverageFilePath",
            "-o", coverageJsonPath
        )

        CommandLineRunner.run(command, project)

        val gson = Gson()
        val file = File(coverageJsonPath)
        val jsonString = file.readText()

        val coverageResult = gson.fromJson(jsonString, CoverageResult::class.java)
        return coverageResult
    }

    /**
     * Combines coverage results from the test case and test suite.
     */
    fun combineCoverageResults() {
        val pythonSdk = findPythonSDKHomePath() ?: return

        val coverageTestCasePath = FileUtils.getCoverageTestCasePath(project)
        val coverageTestSuitePath = FileUtils.getCoverageTestSuitePath(project)
        val coverageCombinedTestsPath = FileUtils.getCoverageCombinedTestsPath(project)

        val command = arrayListOf(
            pythonSdk, "-m", "coverage", "combine", "--keep",
            "--data-file=$coverageCombinedTestsPath",
            coverageTestCasePath,
            coverageTestSuitePath
        )

        CommandLineRunner.run(command, project)
    }


    /**
     * Finds the Python SDK home path for the project.
     *
     * @return The Python SDK home path.
     * @throws IllegalStateException if the SDK home path is not found.
     */
    private fun findPythonSDKHomePath(): String? {
        return ProjectRootManager
            .getInstance(project)
            .projectSdk
            ?.homeDirectory
            ?.path
            ?: run {
                createWarningNotification(
                    PluginMessagesBundle.get("missingPythonInterpreterTitle"),
                    PluginMessagesBundle.get("missingPythonInterpreterMessage"),
                    project
                )
                return null
            }
    }

}