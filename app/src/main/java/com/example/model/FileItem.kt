package com.example.model

import java.io.File

enum class GitFileStatus {
    UNMODIFIED,
    MODIFIED,
    STAGED,
    UNTRACKED,
    DELETED
}

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val extension: String = file.extension.lowercase(),
    val isExpanded: Boolean = false,
    val children: List<FileItem> = emptyList(),
    val gitStatus: GitFileStatus = GitFileStatus.UNMODIFIED,
    val size: Long = if (file.isFile) file.length() else 0L
) {
    val language: String
        get() = when (extension) {
            "js", "jsx", "mjs" -> "javascript"
            "ts", "tsx" -> "typescript"
            "py", "pyw" -> "python"
            "kt", "kts" -> "kotlin"
            "java" -> "java"
            "html", "htm" -> "html"
            "css" -> "css"
            "scss", "sass" -> "scss"
            "json" -> "json"
            "md", "markdown" -> "markdown"
            "xml", "svg" -> "xml"
            "yaml", "yml" -> "yaml"
            "sql" -> "sql"
            "c", "h" -> "c"
            "cpp", "hpp", "cc" -> "cpp"
            "cs" -> "csharp"
            "go" -> "go"
            "rs" -> "rust"
            "php" -> "php"
            "rb" -> "ruby"
            "swift" -> "swift"
            "sh", "bash", "zsh" -> "shell"
            "dockerfile" -> "dockerfile"
            else -> if (name.lowercase() == "dockerfile") "dockerfile" else "plaintext"
        }
}
