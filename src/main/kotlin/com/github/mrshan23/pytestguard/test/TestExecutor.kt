package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.test.data.CoverageResult
import com.github.mrshan23.pytestguard.test.data.ExecutionResult
import com.github.mrshan23.pytestguard.utils.CommandLineRunner
import com.github.mrshan23.pytestguard.utils.FileUtils
import com.google.gson.Gson
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val log = KotlinLogging.logger {}

class TestExecutor(
    val project: Project,
) {

    fun executeTest(testFilePath: String, testFramework: TestFramework): ExecutionResult {
        log.info { "Executing test at path: $testFilePath" }

        val pythonSdk = findPythonSDKHomePath()

        val resultsPath = FileUtils.getPyTestGuardResultsDirectoryPath(project)
        val coverageFilePath = FileUtils.getCoverageTestCasePath(project)

        val baseCommand = arrayListOf(
            pythonSdk, "-m", "coverage", "run",
            "--source=.", "--omit=${resultsPath}*",
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

        log.info { "Test execution finished with exit code: ${result.exitCode}" }
        log.debug { "Test execution message: ${result.executionMessage}" }

        return result
    }

    fun executeTestSuite(testSuitePath: String, testFramework: TestFramework): ExecutionResult? {

        if (testSuitePath.isEmpty()) {
            log.error { "Test suite path is empty" }
            //TODO: send notification to setup test suite path
            return null
        }

        val pythonSdk = findPythonSDKHomePath()
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

        log.info { "Test execution finished with exit code: ${result.exitCode}" }
        log.debug { "Test execution message: ${result.executionMessage}" }

        return result
    }

    fun getCoverageData(coverageFilePath: String): CoverageResult {
        val pythonSdk = findPythonSDKHomePath()

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

    fun combineCoverageResults() {
        val pythonSdk = findPythonSDKHomePath()

        val coverageTestCasePath = FileUtils.getCoverageTestCasePath(project)
        val coverageTestSuitePath = FileUtils.getCoverageTestSuitePath(project)
        val coverageCombinedTestsPath = FileUtils.getCoverageCombinedTestsPath(project)

        val command = arrayListOf(
            pythonSdk, "-m", "coverage", "combine",
            "--data-file=$coverageCombinedTestsPath",
            coverageTestCasePath,
            coverageTestSuitePath
        )

        CommandLineRunner.run(command, project)
    }


    private fun findPythonSDKHomePath(): String {
        return ProjectRootManager
            .getInstance(project)
            .projectSdk
            ?.homeDirectory
            ?.path
            ?: (throw IllegalStateException())
        //TODO: send notification to setup interpreter
    }

}