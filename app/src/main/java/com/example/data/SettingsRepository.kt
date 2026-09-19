package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.EditorSettings

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("codeforge_settings", Context.MODE_PRIVATE)

    fun loadSettings(): EditorSettings {
        return EditorSettings(
            theme = prefs.getString("theme", "vs-dark") ?: "vs-dark",
            fontSize = prefs.getInt("fontSize", 14),
            lineHeight = prefs.getInt("lineHeight", 21),
            tabSize = prefs.getInt("tabSize", 4),
            wordWrap = prefs.getBoolean("wordWrap", true),
            minimap = prefs.getBoolean("minimap", true),
            lineNumbers = prefs.getString("lineNumbers", "on") ?: "on",
            autoSave = prefs.getString("autoSave", "afterDelay") ?: "afterDelay",
            formatOnSave = prefs.getBoolean("formatOnSave", false),
            formatOnPaste = prefs.getBoolean("formatOnPaste", true),
            breadcrumbs = prefs.getBoolean("breadcrumbs", true),
            cursorBlinking = prefs.getString("cursorBlinking", "smooth") ?: "smooth",
            smoothScrolling = prefs.getBoolean("smoothScrolling", true),
            bracketPairColorization = prefs.getBoolean("bracketPairColorization", true),
            showHiddenFiles = prefs.getBoolean("showHiddenFiles", false),
            showAccessoryBar = prefs.getBoolean("showAccessoryBar", true)
        )
    }

    fun saveSettings(settings: EditorSettings) {
        prefs.edit()
            .putString("theme", settings.theme)
            .putInt("fontSize", settings.fontSize)
            .putInt("lineHeight", settings.lineHeight)
            .putInt("tabSize", settings.tabSize)
            .putBoolean("wordWrap", settings.wordWrap)
            .putBoolean("minimap", settings.minimap)
            .putString("lineNumbers", settings.lineNumbers)
            .putString("autoSave", settings.autoSave)
            .putBoolean("formatOnSave", settings.formatOnSave)
            .putBoolean("formatOnPaste", settings.formatOnPaste)
            .putBoolean("breadcrumbs", settings.breadcrumbs)
            .putString("cursorBlinking", settings.cursorBlinking)
            .putBoolean("smoothScrolling", settings.smoothScrolling)
            .putBoolean("bracketPairColorization", settings.bracketPairColorization)
            .putBoolean("showHiddenFiles", settings.showHiddenFiles)
            .putBoolean("showAccessoryBar", settings.showAccessoryBar)
            .apply()
    }
}
