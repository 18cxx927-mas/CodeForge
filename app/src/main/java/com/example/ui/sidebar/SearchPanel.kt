package com.example.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WorkspaceManager
import com.example.ui.editor.getFileEmoji
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import java.io.File

@Composable
fun SearchPanel(
    searchQuery: String,
    replaceText: String,
    isRegex: Boolean,
    isCaseSensitive: Boolean,
    isWholeWord: Boolean,
    isSearching: Boolean,
    results: List<WorkspaceManager.SearchResult>,
    onSearchQueryChange: (String) -> Unit,
    onReplaceTextChange: (String) -> Unit,
    onToggleRegex: () -> Unit,
    onToggleCaseSensitive: () -> Unit,
    onToggleWholeWord: () -> Unit,
    onReplaceAll: () -> Unit,
    onResultClick: (File, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSidebar)
            .padding(10.dp)
    ) {
        Text(
            text = "SEARCH",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Search Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeBg)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 12.sp),
                    cursorBrush = SolidColor(VsCodePrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input_field"),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Search", color = VsCodeTextSecondary, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )

                // Aa toggle
                SearchOptionButton(
                    label = "Aa",
                    isSelected = isCaseSensitive,
                    onClick = onToggleCaseSensitive
                )
                Spacer(modifier = Modifier.width(4.dp))

                // \b toggle
                SearchOptionButton(
                    label = "\\b",
                    isSelected = isWholeWord,
                    onClick = onToggleWholeWord
                )
                Spacer(modifier = Modifier.width(4.dp))

                // .* toggle
                SearchOptionButton(
                    label = ".*",
                    isSelected = isRegex,
                    onClick = onToggleRegex
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Replace Input Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeBg)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = replaceText,
                    onValueChange = onReplaceTextChange,
                    textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 12.sp),
                    cursorBrush = SolidColor(VsCodePrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("replace_input_field"),
                    decorationBox = { innerTextField ->
                        if (replaceText.isEmpty()) {
                            Text("Replace", color = VsCodeTextSecondary, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )

                IconButton(
                    onClick = onReplaceAll,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FindReplace,
                        contentDescription = "Replace All",
                        tint = VsCodePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSearching) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = VsCodePrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Searching...", color = VsCodeTextSecondary, fontSize = 11.sp)
                }
            } else if (searchQuery.isNotEmpty()) {
                val fileCount = results.map { it.file.absolutePath }.distinct().size
                Text(
                    text = "${results.size} results in $fileCount files",
                    color = VsCodeTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Search Results List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(results) { res ->
                SearchResultRow(result = res, onClick = { onResultClick(res.file, res.lineNumber) })
            }
        }
    }
}

@Composable
private fun SearchOptionButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (isSelected) VsCodePrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else VsCodeTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SearchResultRow(
    result: WorkspaceManager.SearchResult,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = getFileEmoji(result.file.name), fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = result.file.name,
                color = VsCodeCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = ":${result.lineNumber}",
                color = VsCodeTextSecondary,
                fontSize = 11.sp
            )
        }
        Text(
            text = result.lineContent,
            color = VsCodeTextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            modifier = Modifier.padding(start = 18.dp, top = 2.dp)
        )
    }
}
