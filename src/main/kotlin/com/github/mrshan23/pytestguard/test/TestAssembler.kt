package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.display.custom.CustomProgressIndicator
import com.github.mrshan23.pytestguard.monitor.ErrorMonitor
import com.github.mrshan23.pytestguard.utils.FileUtils
import com.github.mrshan23.pytestguard.utils.isProcessStopped
import com.intellij.openapi.project.Project
import java.io.File

class TestAssembler(
    private val errorMonitor: ErrorMonitor,
    private val indicator: CustomProgressIndicator,
    val project: Project,
) {
    private var rawText = ""

    fun consume(text: String) {
        if (text.isEmpty()) return

        rawText = rawText.plus(text)
    }

    fun getContent(): String {
        return rawText
    }

    fun assembleTestSuite(methodName: String): List<TestCase>? {
        if (isProcessStopped(errorMonitor, indicator)) return null

        val testCases = ArrayList<TestCase>()

        val directoryPath = FileUtils.getMethodTestFilesDirectoryPath(methodName, project)
        val directory = File(directoryPath)

        val testFiles: List<File> = directory.listFiles()?.filter { it.isFile } ?: emptyList()

        for (testFile in testFiles) {
            val content = testFile.readText()
            val fileName = testFile.nameWithoutExtension
            val testCase = TestCase(null, fileName.substringBeforeLast("_"), fileName, content, methodName)
            testCases.add(testCase)
        }

        return testCases
    }
}