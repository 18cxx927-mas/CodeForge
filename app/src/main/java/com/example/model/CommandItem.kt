package com.example.model

data class CommandItem(
    val id: String,
    val title: String,
    val category: String,
    val shortcut: String = "",
    val action: () -> Unit
)
