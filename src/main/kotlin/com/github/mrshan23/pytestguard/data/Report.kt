package com.github.mrshan23.pytestguard.data

/**
 * Stores generated test cases and their coverage.
 * Implemented on the basis of `org.evosuite.utils.CompactReport` structure.
 *
 * `Report`'s member fields were created based on the fields in
 * `org.evosuite.utils.CompactReport` for easier transformation.
 */
open class Report {
    var testCaseList: HashMap<Int, TestCase> = hashMapOf()
}
