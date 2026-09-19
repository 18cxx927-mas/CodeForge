package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeTabsBar
import com.example.ui.theme.VsCodeTextPrimary

@Composable
fun MobileAccessoryBar(
    onInsertText: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onFind: () -> Unit,
    onFormat: () -> Unit,
    onComment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSymbols = listOf(
        "TAB" to "\t",
        "{" to "{",
        "}" to "}",
        "(" to "(",
        ")" to ")",
        "[" to "[",
        "]" to "]",
        ";" to ";",
        ":" to ":",
        "=" to "=",
        "==" to "==",
        "=>" to "=>",
        "->" to "->",
        "\"" to "\"",
        "'" to "'",
        "<" to "<",
        ">" to ">",
        "/" to "/",
        "\\" to "\\",
        "|" to "|",
        "&" to "&",
        "!" to "!",
        "?" to "?",
        "_" to "_",
        "#" to "#",
        "$" to "$"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(VsCodeTabsBar)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Quick Action Buttons
        ActionButton(label = "💾 Save", onClick = onSave, isPrimary = true)
        ActionButton(label = "↩ Undo", onClick = onUndo)
        ActionButton(label = "↪ Redo", onClick = onRedo)
        ActionButton(label = "🔍 Find", onClick = onFind)
        ActionButton(label = "✨ Format", onClick = onFormat)
        ActionButton(label = "💬 //", onClick = onComment)

        // Symbols
        quickSymbols.forEach { (label, symbol) ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeSecondary)
                    .clickable { onInsertText(symbol) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("symbol_$label"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = VsCodeTextPrimary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 3.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isPrimary) VsCodePrimary else VsCodeBorder)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = VsCodeTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
