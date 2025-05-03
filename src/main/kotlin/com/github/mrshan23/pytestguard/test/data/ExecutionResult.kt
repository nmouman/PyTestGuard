package com.github.mrshan23.pytestguard.test.data

data class ExecutionResult(
    val exitCode: Int,
    val executionMessage: String,
    var statementCoverage: String?,
    var statementCoverageChange: String?,
) {
    fun isSuccessful(): Boolean = exitCode == 0
}
