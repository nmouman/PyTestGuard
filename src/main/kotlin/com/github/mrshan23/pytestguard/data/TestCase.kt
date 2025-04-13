package com.github.mrshan23.pytestguard.data

/**
 * Class representing a test case.
 * Used to display generated test cases in the UI.
 */
open class TestCase(
    var id: Int?,
    val testName: String,
    var testCode: String,
)
