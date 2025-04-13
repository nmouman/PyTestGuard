package com.github.mrshan23.pytestguard.data

import com.github.mrshan23.pytestguard.test.TestFramework

open class Report {
    var testCaseList: HashMap<Int, TestCase> = hashMapOf()
    var testFramework: TestFramework? = null
}
