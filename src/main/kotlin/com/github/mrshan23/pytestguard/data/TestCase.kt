package com.github.mrshan23.pytestguard.data

/**
 * Test case storage, implemented based on the org.evosuite.utils.CompactTestCase structure.
 */
open class TestCase(
    var id: Int?,
    val testName: String,
    var testCode: String,
)
