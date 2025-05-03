package com.github.mrshan23.pytestguard.utils

import com.intellij.openapi.project.Project
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.util.Locale

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

    fun removeFile(path: String) {
        val file = File(path)

        if (!file.exists()) return

        log.debug { "Removing file at path: $path" }

        file.delete()
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

    fun sanitizeFileName(name: String): String {
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