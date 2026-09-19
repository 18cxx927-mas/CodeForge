package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FileItem
import com.example.ui.editor.getFileEmoji
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import java.io.File

@Composable
fun QuickOpenDialog(
    query: String,
    files: List<FileItem>,
    onQueryChange: (String) -> Unit,
    onFileSelect: (File) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun flattenFiles(items: List<FileItem>): List<FileItem> {
        val result = mutableListOf<FileItem>()
        for (item in items) {
            if (!item.isDirectory) {
                result.add(item)
            }
            if (item.children.isNotEmpty()) {
                result.addAll(flattenFiles(item.children))
            }
        }
        return result
    }

    val allFiles = flattenFiles(files)
    val filteredFiles = if (query.isEmpty()) {
        allFiles
    } else {
        allFiles.filter { it.name.lowercase().contains(query.lowercase()) || it.path.lowercase().contains(query.lowercase()) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(VsCodeBg)
                .padding(8.dp)
        ) {
            // Search Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeSecondary)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(VsCodePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("quick_open_input"),
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text("Go to file (type name)...", color = VsCodeTextSecondary, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.padding(vertical = 4.dp))

            // File list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
            ) {
                items(filteredFiles) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFileSelect(item.file)
                                onDismiss()
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = getFileEmoji(item.name), fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = item.name,
                                color = VsCodeTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = item.path,
                                color = VsCodeTextSecondary,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
