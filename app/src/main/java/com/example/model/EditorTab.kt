package com.example.model

import java.io.File
import java.util.UUID

data class EditorTab(
    val id: String = UUID.randomUUID().toString(),
    val file: File,
    val title: String = file.name,
    val path: String = file.absolutePath,
    val language: String,
    val content: String = "",
    val isDirty: Boolean = false,
    val isPinned: Boolean = false,
    val cursorLine: Int = 1,
    val cursorColumn: Int = 1
)
