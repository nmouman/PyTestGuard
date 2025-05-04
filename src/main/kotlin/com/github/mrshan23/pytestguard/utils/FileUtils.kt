package com.github.mrshan23.pytestguard.utils

import com.github.mrshan23.pytestguard.data.TestCase
import com.intellij.openapi.project.Project
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.DosFileAttributeView
import java.util.*

private val log = KotlinLogging.logger {}

/**
 * This file uses code from TestSpark (https://github.com/JetBrains-Research/TestSpark)
 */
object FileUtils {

    private val PYTESTGUARD_RESULTS_PATH = "${File.separatorChar}.pyTestGuardResults${File.separatorChar}"

    fun getPyTestGuardResultsDirectoryPath(project: Project): String {
        val testResultDirectory = "${project.basePath!!}$PYTESTGUARD_RESULTS_PATH"
        return testResultDirectory
    }

    fun getCoverageTestCasePath(project: Project): String {
        val testResultDirectory = getPyTestGuardResultsDirectoryPath(project)
        return "${testResultDirectory}.testCaseCoverage"
    }

    fun getCoverageTestSuitePath(project: Project): String {
        val testResultDirectory = getPyTestGuardResultsDirectoryPath(project)
        return "${testResultDirectory}.testSuiteCoverage"
    }

    fun getCoverageCombinedTestsPath(project: Project): String {
        val testResultDirectory = getPyTestGuardResultsDirectoryPath(project)
        return "${testResultDirectory}.combinedCoverage"
    }

    fun getCoverageJsonPath(project: Project): String {
        val testResultDirectory = getPyTestGuardResultsDirectoryPath(project)
        return "${testResultDirectory}coverage.json"
    }

    fun removeDirectory(path: String) {
        val directory = File(path)

        if (!directory.exists()) return

        if (directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        removeDirectory(file.absolutePath)
                    } else {
                        file.delete()
                    }
                }
            }
        }

        log.debug { "Removing directory at path: $path" }

        directory.delete()
    }

    fun getUniqueTestCaseName(testName: String): String {
        val id = UUID.randomUUID().toString()
        return "${sanitizeFileName(testName)}_$id"
    }

    fun getTestCasePath(testCase: TestCase, project: Project): String {
        val testResultDirectory = getPyTestGuardResultsDirectoryPath(project)
        return "$testResultDirectory${testCase.uniqueTestName}.py"
    }

    fun createHiddenPyTestGuardResultsDirectory(project:Project) {
        val testResultDirectoryPath = getPyTestGuardResultsDirectoryPath(project)

        val tmpDir = File(testResultDirectoryPath)
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        // Set hidden attribute for Windows
        if (isWindows()) {
            try {
                val path = tmpDir.toPath()
                Files.getFileAttributeView(path, DosFileAttributeView::class.java)?.setHidden(true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sanitizeFileName(name: String): String {
        if (isWindows()) {
            val forbiddenChars = arrayOf("<", ">", ":", "\"", "/", "\\", "|", "?", "*")
            return forbiddenChars.fold(name) { acc, c -> acc.replace(c, "_") }
        }
        return name
    }

    fun isWindows(): Boolean {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        return (os.indexOf("win") >= 0)
    }

}