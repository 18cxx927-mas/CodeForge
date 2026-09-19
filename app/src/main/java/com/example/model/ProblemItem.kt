package com.example.model

enum class ProblemSeverity {
    ERROR,
    WARNING,
    INFO
}

data class ProblemItem(
    val id: String,
    val message: String,
    val severity: ProblemSeverity,
    val filePath: String,
    val fileName: String,
    val line: Int,
    val column: Int,
    val source: String = "syntax"
)
