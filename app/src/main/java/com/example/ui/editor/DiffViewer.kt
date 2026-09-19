package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DiffLine
import com.example.model.DiffLineType
import com.example.model.FileDiff
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun DiffViewer(
    diff: FileDiff,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeBg)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF252526))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diff: ${diff.fileName} (Working Tree)",
                color = VsCodeCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close diff",
                    tint = VsCodeTextSecondary
                )
            }
        }

        // Diff lines
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(diff.lines) { line ->
                DiffLineRow(line = line)
            }
        }
    }
}

@Composable
private fun DiffLineRow(line: DiffLine) {
    val (bgColor, textColor, prefix) = when (line.type) {
        DiffLineType.ADDED -> Triple(Color(0x332EA043), Color(0xFF7EE787), "+")
        DiffLineType.REMOVED -> Triple(Color(0x33F85149), Color(0xFFFFA198), "-")
        DiffLineType.UNCHANGED -> Triple(Color.Transparent, VsCodeTextPrimary, " ")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Old line number
        Text(
            text = line.oldLineNumber?.toString() ?: "",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .width(36.dp)
                .padding(start = 6.dp)
        )

        // New line number
        Text(
            text = line.newLineNumber?.toString() ?: "",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .width(36.dp)
                .padding(start = 2.dp)
        )

        // Prefix (+ / - / space)
        Text(
            text = prefix,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(16.dp)
        )

        // Text
        Text(
            text = line.text,
            color = textColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
