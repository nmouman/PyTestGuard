package com.github.mrshan23.pytestguard.test.data

import com.github.mrshan23.pytestguard.data.TestCase

data class TestSuiteGenerated(
    val imports: Set<String>,
    val className: String?,
    val fixtures: List<String>,
    val setupMethods: List<String>,
    val testCasesGenerated: List<TestCaseGenerated>
) {

    private val indent: String = " ".repeat(4)

    fun assembleTestCasesForDisplay(isUnittest: Boolean): List<TestCase> {
        return testCasesGenerated.map { testCaseGenerated ->
            TestCase(null, testCaseGenerated.name, buildString {
                if (imports.isNotEmpty()) {
                    appendLine(imports.joinToString("\n"))
                    appendLine()
                }

                className?.let { className ->
                    if (isUnittest) {
                        appendLine("class $className(unittest.TestCase):")
                    } else {
                        // For pytest
                        appendLine("class $className:")
                    }
                    appendLine()

                    // Add fixtures (indented)
                    if (!isUnittest) {
                        fixtures.forEach { fixture ->
                            fixture.lines().forEach { line ->
                                appendLine("${indent}$line")
                            }
                            appendLine()
                        }
                    }

                    // Add setup methods (indented)
                    setupMethods.forEach { setupMethod ->
                        if (isUnittest) {
                            appendLine("${indent}$setupMethod") // Keeping it simple for unittest
                        } else {
                            setupMethod.lines().forEach { line ->
                                appendLine("${indent}$line")
                            }
                        }
                        appendLine()
                    }

                    testCaseGenerated.body.lines().forEach { line ->
                        appendLine("${indent}$line")
                    }

                } ?: run {
                    // No class -> just test case body (for pytest/unittest)
                    testCaseGenerated.body.lines().forEach { line ->
                        appendLine(line)
                    }
                }

                if (isUnittest) {
                    appendLine()
                    appendLine("if __name__ == '__main__':")
                    appendLine("${indent}unittest.main()")
                }

            })
        }
    }
}

data class TestCaseGenerated(
    var name: String = "",
    var body: String = "",
)