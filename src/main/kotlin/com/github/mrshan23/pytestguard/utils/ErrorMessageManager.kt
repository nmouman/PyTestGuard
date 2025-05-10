package com.github.mrshan23.pytestguard.utils

import com.github.mrshan23.pytestguard.data.TestCase
import com.github.mrshan23.pytestguard.test.TestFramework

object ErrorMessageManager {

    private const val SEPARATOR = "<br/>"

    /***
     * Simplifies the error message by removing unnecessary information and formatting it for better readability.
     *
     * @param errorMessage The original error message to be simplified.
     * @param testCase The test case associated with the error message.
     * @param testFramework The test framework used (e.g., Pytest, Unittest).
     * @return A simplified version of the error message.
     */
    fun simplify(errorMessage: String, testCase: TestCase, testFramework: TestFramework): String {
        val result = when(testFramework) {
            TestFramework.PYTEST -> simplifyForPytestMessage(errorMessage, testCase)
            TestFramework.UNITTEST -> simplifyForUnittestMessage(errorMessage, testCase)
        }

        if (result.isEmpty()) {
            return errorMessage
        }

        return result
    }

    private fun simplifyForPytestMessage(errorMessage: String, testCase: TestCase): String {
        // Regex to match: "E   IndexError: x must be an integer or at least 1-dimensional"
        val errorMessageRegex = Regex("""^E\s+(.*)""", RegexOption.MULTILINE)

        // Regex to match: "python_file.py:4722: IndexError"
        val tracebackRegex = Regex("""^(.+?):(\d+):\s+(\w+Error)$""", RegexOption.MULTILINE)

        val errorMatch = errorMessageRegex.find(errorMessage)
        val tracebackMatch = tracebackRegex.find(errorMessage)

        val result = StringBuilder()

        errorMatch?.let {
            val message = it.groupValues[1]
            result.append("Message: $message").append(SEPARATOR)
        }

        tracebackMatch?.let {
            val (filePath, lineNumber, exception) = it.destructured

            result.append("Exception: $exception").append(SEPARATOR)

            // Check if the file path is the path of the generated test case
            if (!filePath.contains(testCase.uniqueTestName!!)) {
                result.append("File: $filePath").append(SEPARATOR)
            }

            result.append("Line: $lineNumber").append(SEPARATOR)
        }

        return result.toString()
    }

    private fun simplifyForUnittestMessage(errorMessage: String, testCase: TestCase): String {
        // Regex to match: "AssertionError: Lists differ: [] != [1]"
        val errorRegex = Regex("""^(\w+Error):\s+(.+)$""", RegexOption.MULTILINE)

        // Regex to match: 'File "python_file.py", line 18, in function_name'
        val tracebackRegex = Regex("""File "(.+?)", line (\d+), in (.+)""", RegexOption.MULTILINE)

        val errorMatch = errorRegex.find(errorMessage)
        val tracebackMatch = tracebackRegex.findAll(errorMessage).lastOrNull()

        val result = StringBuilder()

        errorMatch?.let {
            val (exception, message) = it.destructured
            result.append("Exception: $exception").append(SEPARATOR)
            result.append("Message: $message").append(SEPARATOR)
        }

        tracebackMatch?.let {
            val (filePath, lineNumber, function) = it.destructured

            // Check if the file path is the path of the generated test case
            if (!filePath.contains(testCase.uniqueTestName!!)) {
                result.append("File: $filePath").append(SEPARATOR)
            }

            result.append("Function: $function").append(SEPARATOR)
            result.append("Line: $lineNumber").append(SEPARATOR)
        }

        return result.toString()
    }
}
