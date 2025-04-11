package com.github.mrshan23.pytestguard.data

/**
 * Test case storage, implemented based on the org.evosuite.utils.CompactTestCase structure.
 */
open class TestCase(
    val id: Int,
    var testCode: String,
)
