package com.github.mrshan23.pytestguard.data

import com.github.mrshan23.pytestguard.test.TestFramework

/**
 * Class representing the generated test suite that is shown to the user.
 */
open class Report {
    var testCaseList: HashMap<Int, TestCase> = hashMapOf()
    var testFramework: TestFramework? = null
}
