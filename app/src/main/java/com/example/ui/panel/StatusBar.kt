package com.example.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeStatusBar

@Composable
fun StatusBar(
    currentBranch: String,
    errorCount: Int,
    warningCount: Int,
    activeLine: Int,
    activeColumn: Int,
    language: String,
    tabSize: Int,
    isZenMode: Boolean,
    onBranchClick: () -> Unit,
    onProblemsClick: () -> Unit,
    onToggleTerminal: () -> Unit,
    onToggleZenMode: () -> Unit,
    onToggleMarkdownPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(VsCodeStatusBar)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Section: Git branch & Errors / Warnings
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Git Branch
            StatusItem(
                text = "🌿 $currentBranch",
                testTag = "status_bar_branch",
                onClick = onBranchClick
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Errors & Warnings
            StatusItem(
                text = "⊗ $errorCount  ⚠ $warningCount",
                testTag = "status_bar_problems",
                onClick = onProblemsClick
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Terminal Toggle
            StatusItem(
                text = "💻 Terminal",
                testTag = "status_bar_terminal",
                onClick = onToggleTerminal
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Right Section: Cursor, Encoding, Language
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusItem(
                text = "Ln $activeLine, Col $activeColumn",
                testTag = "status_bar_cursor"
            )

            Spacer(modifier = Modifier.width(10.dp))

            StatusItem(
                text = "Spaces: $tabSize",
                testTag = "status_bar_tab_size"
            )

            Spacer(modifier = Modifier.width(10.dp))

            StatusItem(
                text = "UTF-8",
                testTag = "status_bar_encoding"
            )

            Spacer(modifier = Modifier.width(10.dp))

            StatusItem(
                text = language.replaceFirstChar { it.uppercase() },
                testTag = "status_bar_language"
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Markdown preview toggle
            if (language.lowercase() in listOf("markdown", "md")) {
                StatusItem(
                    text = "📖 Preview",
                    testTag = "status_bar_markdown_preview",
                    onClick = onToggleMarkdownPreview
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            // Zen mode toggle
            StatusItem(
                text = if (isZenMode) "🔲 Exit Zen" else "🔲 Zen",
                testTag = "status_bar_zen_mode",
                onClick = onToggleZenMode
            )
        }
    }
}

@Composable
private fun StatusItem(
    text: String,
    testTag: String,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .testTag(testTag)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
