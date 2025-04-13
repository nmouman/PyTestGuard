package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.utils.CommandLineRunner
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

data class ExecutionResult(
    val exitCode: Int,
    val executionMessage: String,
) {
    fun isSuccessful(): Boolean = exitCode == 0
}

class TestExecutor(
    val project: Project,
) {

    fun executeTest(testFilePath: String, testFramework: TestFramework): ExecutionResult {

        log.info { "Executing test at path: $testFilePath" }

        val pythonSdk = findPythonSDKHomePath()

        val command = when (testFramework) {
            TestFramework.UNITTEST -> arrayListOf(pythonSdk, "-m", "unittest", testFilePath)
            TestFramework.PYTEST -> arrayListOf(pythonSdk, "-m", "pytest", testFilePath)
        }

        val result = CommandLineRunner.run(command, project)

        log.info { "Test execution finished with exit code: ${result.exitCode}" }
        log.debug { "Test execution message: ${result.executionMessage}" }

        return result
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