package com.example.model

data class ExtensionItem(
    val id: String,
    val name: String,
    val publisher: String,
    val description: String,
    val version: String,
    val iconEmoji: String,
    val isInstalled: Boolean = true,
    val isEnabled: Boolean = true,
    val category: String,
    val rating: Float,
    val downloads: String
)
