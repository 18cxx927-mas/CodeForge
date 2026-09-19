package com.example.model

data class GitCommit(
    val id: String,
    val hash: String,
    val message: String,
    val author: String,
    val date: String
)

data class GitBranch(
    val name: String,
    val isCurrent: Boolean = false
)

enum class DiffLineType {
    ADDED,
    REMOVED,
    UNCHANGED
}

data class DiffLine(
    val type: DiffLineType,
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
    val text: String
)

data class FileDiff(
    val filePath: String,
    val fileName: String,
    val status: GitFileStatus,
    val lines: List<DiffLine>
)
