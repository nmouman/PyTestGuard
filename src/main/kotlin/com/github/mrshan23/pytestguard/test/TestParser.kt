package com.github.mrshan23.pytestguard.test

import com.github.mrshan23.pytestguard.test.data.TestCaseGenerated
import com.github.mrshan23.pytestguard.test.data.TestSuiteGenerated
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class TestParser {

    fun parseTestSuiteFile(rawText: String): TestSuiteGenerated? {
        return try {
            val rawCode = if (rawText.contains("```")) rawText.split("```")[1] else rawText

            // Extract imports
            val importPattern = Regex("""^(import .+|from .+ import .+)""", RegexOption.MULTILINE)
            val imports = importPattern.findAll(rawCode).map { it.value }.toMutableSet()

            val className: String? = Regex("""class\s+(\w+)\s*([(:])""").find(rawCode)
                ?.groupValues
                ?.get(1)
                ?.takeIf { it.contains("test", ignoreCase = true) }

            // Extract setup/teardown code
            val setupMethods = mutableListOf<String>()
            val setupMethodPattern = Regex(
                """(\s*def (setup_method|teardown_method|setUp|tearDown)\(.*?\):(.*?))(?=\n\s+def |\n\s+class |\Z)""",
                RegexOption.DOT_MATCHES_ALL
            )
            setupMethodPattern.findAll(rawCode).forEach { match ->
                val fullSetupCode = match.groupValues[1].trim().trimIndent()
                setupMethods.add(fullSetupCode)
            }

            // Extract test functions (supports both class-based & function-based `pytest`)
            val generatedTestCases = mutableListOf<TestCaseGenerated>()
            val testFunctionPattern = Regex(
                """(\s*def (test_\w+)\(.*?\):(.*?))(?=\n\s+def |\n\s+class |\Z)""",
                RegexOption.DOT_MATCHES_ALL
            )
            testFunctionPattern.findAll(rawCode).forEach { match ->
                val fullTestCode = match.groupValues[1].trim().trimIndent() // Signature + Body
                val methodName = match.groupValues[2]                // Method name only
                generatedTestCases.add(TestCaseGenerated(methodName, fullTestCode))
            }

            // Extract `@pytest.fixture` functions
            val fixtures = mutableListOf<String>()
            val fixtureFunctionPattern = Regex(
                """(\s*@pytest\.fixture\s*\n\s*def \w+\(.*?\):(.*?))(?=\n\s*@pytest\.fixture|\n\s+def |\n\s+class |\Z)""",
                RegexOption.DOT_MATCHES_ALL
            )
            fixtureFunctionPattern.findAll(rawCode).forEach { match ->
                val fullFixtureCode = match.groupValues[1].trimIndent()
                fixtures.add(fullFixtureCode)
            }

            TestSuiteGenerated(
                imports = imports,
                className = className,
                fixtures = fixtures,
                setupMethods = setupMethods,
                testCasesGenerated = generatedTestCases
            )
        } catch (e: Exception) {
            log.error(e) {"Error parsing test suite: ${e.message}"}
            return null
        }
    }

}