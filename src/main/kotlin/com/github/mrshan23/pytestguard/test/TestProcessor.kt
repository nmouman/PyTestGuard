package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.data.TestCase
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.createDirectories

private val log = KotlinLogging.logger {}

class TestProcessor(
    val project: Project,
) {

    fun runTest(testCase: TestCase, testFramework: TestFramework) {
        val testFilePath = createTemporaryTestFile(testCase)



        log.info { "Removing test file $testFilePath" }
        cleanFolder(testFilePath)

        log.info { "Removing results directory ${getPyTestGuardResultsDirectoryPath()}$" }
        cleanFolder(getPyTestGuardResultsDirectoryPath())
    }

    private fun createTemporaryTestFile(testCase: TestCase): String {

        val testResultDirectory = getPyTestGuardResultsDirectoryPath()

        val tmpDir = File(testResultDirectory)

        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        val id = UUID.randomUUID().toString()

        val testFilePath = "$testResultDirectory${sanitizeFileName(testCase.testName)}_$id.py"

        val testFile = File(testFilePath)
        testFile.createNewFile()

        log.info { "Save test in file " + testFile.absolutePath }

        testFile.writeText(testCase.testCode)

        return testFilePath
    }

    private fun getPyTestGuardResultsDirectoryPath(): String {
        val sep = File.separatorChar
        val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}pyTestGuardResults${sep}"
        return testResultDirectory
    }

    /**
     * Remove all forbidden characters depending on platform.
     */
    private fun sanitizeFileName(name: String): String {
        if (isWindows()) {
            val forbiddenChars = arrayOf("<", ">", ":", "\"", "/", "\\", "|", "?", "*")
            return forbiddenChars.fold(name) { acc, c -> acc.replace(c, "_") }
        }
        return name
    }

    private fun isWindows(): Boolean {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        return (os.indexOf("win") >= 0)
    }

    private fun cleanFolder(path: String) {
        val folder = File(path)

        if (!folder.exists()) return

        if (folder.isDirectory) {
            val files = folder.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        cleanFolder(file.absolutePath)
                    } else {
                        file.delete()
                    }
                }
            }
        }
        folder.delete()
    }
}