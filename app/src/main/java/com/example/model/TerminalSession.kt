package com.example.model

import java.util.UUID

data class TerminalLine(
    val text: String,
    val isCommand: Boolean = false,
    val isError: Boolean = false,
    val isPrompt: Boolean = false,
    val isSuccess: Boolean = false
)

data class TerminalSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "bash",
    val lines: List<TerminalLine> = emptyList(),
    val workingDirectory: String = "~/workspace",
    val history: List<String> = emptyList()
)
