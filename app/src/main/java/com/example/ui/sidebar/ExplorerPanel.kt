package com.example.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.model.GitFileStatus
import com.example.ui.editor.getFileEmoji
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeGitModified
import com.example.ui.theme.VsCodeGitUntracked
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import java.io.File

@Composable
fun ExplorerPanel(
    fileTree: List<FileItem>,
    onFileClick: (File) -> Unit,
    onFolderToggle: (FileItem) -> Unit,
    onNewFile: (File?) -> Unit,
    onNewFolder: (File?) -> Unit,
    onRefresh: () -> Unit,
    onRename: (File) -> Unit,
    onDelete: (File) -> Unit,
    onDuplicate: (File) -> Unit,
    onShowDiff: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSidebar)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "EXPLORER",
                color = VsCodeTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onNewFile(null) },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("explorer_new_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.NoteAdd,
                        contentDescription = "New File",
                        tint = VsCodeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = { onNewFolder(null) },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("explorer_new_folder")
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "New Folder",
                        tint = VsCodeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("explorer_refresh")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = VsCodeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Section header: WORKSPACE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VsCodeBg)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📁 CODEFORGE-WORKSPACE",
                color = VsCodeTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Tree
        if (fileTree.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Workspace is empty", color = VsCodeTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VsCodeBorder)
                            .clickable { onNewFile(null) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Create File", color = VsCodeTextPrimary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(fileTree) { item ->
                    FileTreeItemRow(
                        item = item,
                        depth = 0,
                        onFileClick = onFileClick,
                        onFolderToggle = onFolderToggle,
                        onNewFile = onNewFile,
                        onNewFolder = onNewFolder,
                        onRename = onRename,
                        onDelete = onDelete,
                        onDuplicate = onDuplicate,
                        onShowDiff = onShowDiff
                    )
                }
            }
        }
    }
}

@Composable
private fun FileTreeItemRow(
    item: FileItem,
    depth: Int,
    onFileClick: (File) -> Unit,
    onFolderToggle: (FileItem) -> Unit,
    onNewFile: (File?) -> Unit,
    onNewFolder: (File?) -> Unit,
    onRename: (File) -> Unit,
    onDelete: (File) -> Unit,
    onDuplicate: (File) -> Unit,
    onShowDiff: (File) -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Column {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clickable {
                        if (item.isDirectory) {
                            onFolderToggle(item)
                        } else {
                            onFileClick(item.file)
                        }
                    }
                    .padding(start = (depth * 14 + 8).dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isDirectory) {
                    Icon(
                        imageVector = if (item.isExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = VsCodeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (item.isExpanded) "📂" else "📁",
                        fontSize = 12.sp
                    )
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = getFileEmoji(item.name),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = item.name,
                    color = if (item.gitStatus == GitFileStatus.MODIFIED) VsCodeGitModified
                    else if (item.gitStatus == GitFileStatus.UNTRACKED) VsCodeGitUntracked
                    else VsCodeTextPrimary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Git status tag (M / U / D)
                if (item.gitStatus != GitFileStatus.UNMODIFIED) {
                    val statusText = when (item.gitStatus) {
                        GitFileStatus.MODIFIED -> "M"
                        GitFileStatus.UNTRACKED -> "U"
                        GitFileStatus.STAGED -> "A"
                        GitFileStatus.DELETED -> "D"
                        else -> ""
                    }
                    val statusColor = when (item.gitStatus) {
                        GitFileStatus.MODIFIED -> VsCodeGitModified
                        GitFileStatus.UNTRACKED -> VsCodeGitUntracked
                        else -> VsCodeCyan
                    }
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Context trigger
                Text(
                    text = "⋮",
                    color = VsCodeTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { showContextMenu = true }
                        .padding(horizontal = 4.dp)
                )
            }

            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                modifier = Modifier.background(VsCodeBg)
            ) {
                if (item.isDirectory) {
                    DropdownMenuItem(
                        text = { Text("New File in folder", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showContextMenu = false
                            onNewFile(item.file)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New Folder in folder", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showContextMenu = false
                            onNewFolder(item.file)
                        }
                    )
                } else {
                    if (item.gitStatus == GitFileStatus.MODIFIED) {
                        DropdownMenuItem(
                            text = { Text("Open Changes (Diff)", color = VsCodeCyan, fontSize = 12.sp) },
                            onClick = {
                                showContextMenu = false
                                onShowDiff(item.file)
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Duplicate", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showContextMenu = false
                            onDuplicate(item.file)
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Rename", color = VsCodeTextPrimary, fontSize = 12.sp) },
                    onClick = {
                        showContextMenu = false
                        onRename(item.file)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFF14C4C), fontSize = 12.sp) },
                    onClick = {
                        showContextMenu = false
                        onDelete(item.file)
                    }
                )
            }
        }

        // Render children recursively if expanded
        if (item.isDirectory && item.isExpanded) {
            item.children.forEach { child ->
                FileTreeItemRow(
                    item = child,
                    depth = depth + 1,
                    onFileClick = onFileClick,
                    onFolderToggle = onFolderToggle,
                    onNewFile = onNewFile,
                    onNewFolder = onNewFolder,
                    onRename = onRename,
                    onDelete = onDelete,
                    onDuplicate = onDuplicate,
                    onShowDiff = onShowDiff
                )
            }
        }
    }
}
