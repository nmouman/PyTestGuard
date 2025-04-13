package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.utils.FileUtils
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

        FileUtils.removeFile(testFilePath)
        FileUtils.removeDirectory(FileUtils.getPyTestGuardResultsDirectoryPath(project))

        return result
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