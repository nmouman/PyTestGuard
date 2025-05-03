package com.github.mrshan23.pytestguard.test.data

data class CoverageResult(
    val totals: CoverageTotals,
)

data class CoverageTotals(
    val covered_lines: Int,
    val num_statements: Int,
    val percent_covered: Double,
    val percent_covered_display: String,
    val missing_lines: Int,
    val excluded_lines: Int,
)
