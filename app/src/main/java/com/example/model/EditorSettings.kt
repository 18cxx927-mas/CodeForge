package com.example.model

data class EditorSettings(
    val theme: String = "vs-dark", // "vs-dark", "vs", "hc-black", "monokai"
    val fontSize: Int = 14,
    val lineHeight: Int = 21,
    val tabSize: Int = 4,
    val wordWrap: Boolean = true,
    val minimap: Boolean = true,
    val lineNumbers: String = "on", // "on", "off", "relative"
    val autoSave: String = "afterDelay", // "off", "afterDelay", "onFocusChange"
    val formatOnSave: Boolean = false,
    val formatOnPaste: Boolean = true,
    val breadcrumbs: Boolean = true,
    val cursorBlinking: String = "smooth", // "smooth", "blink", "solid"
    val smoothScrolling: Boolean = true,
    val bracketPairColorization: Boolean = true,
    val showHiddenFiles: Boolean = false,
    val showAccessoryBar: Boolean = true
)
