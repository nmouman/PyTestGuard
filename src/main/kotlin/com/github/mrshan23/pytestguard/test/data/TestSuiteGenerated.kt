package com.github.mrshan23.pytestguard.test.data

data class TestSuiteGenerated(
    val imports: Set<String>,
    val className: String?,
    val fixtures: List<String>,
    val setupMethods: List<String>,
    val testCases: List<TestCaseGenerated>
) {
    fun assembleTestCasesForDisplay(isUnittest: Boolean): List<String> {
        return testCases.map { testCase ->
            buildString {
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
                                appendLine("    $line")
                            }
                            appendLine()
                        }
                    }

                    // Add setup methods (indented)
                    setupMethods.forEach { setupMethod ->
                        if (isUnittest) {
                            appendLine("    $setupMethod") // Keeping it simple for unittest
                        } else {
                            setupMethod.lines().forEach { line ->
                                appendLine("    $line")
                            }
                        }
                        appendLine()
                    }

                    testCase.body.lines().forEach { line ->
                        appendLine("    $line")
                    }

                } ?: run {
                    // No class -> just test case body (for pytest/unittest)
                    testCase.body.lines().forEach { line ->
                        appendLine(line)
                    }
                }
            }
        }
    }
}

data class TestCaseGenerated(
    var name: String = "",
    var body: String = "",
)