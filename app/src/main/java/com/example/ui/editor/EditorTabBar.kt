package com.example.ui.editor

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Splitscreen
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
import com.example.model.EditorTab
import com.example.ui.theme.VsCodeActiveTab
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeInactiveTab
import com.example.ui.theme.VsCodeOrange
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeTabsBar
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.ui.theme.VsCodeYellow

@Composable
fun EditorTabBar(
    tabs: List<EditorTab>,
    activeTabIndex: Int,
    isSplitEditor: Boolean,
    onTabSelected: (Int) -> Unit,
    onTabClosed: (Int) -> Unit,
    onCloseOtherTabs: (Int) -> Unit,
    onCloseAllTabs: () -> Unit,
    onTogglePinTab: (Int) -> Unit,
    onToggleSplitEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(VsCodeTabsBar),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isActive = index == activeTabIndex
                TabItemView(
                    tab = tab,
                    isActive = isActive,
                    onClick = { onTabSelected(index) },
                    onClose = { onTabClosed(index) },
                    onCloseOthers = { onCloseOtherTabs(index) },
                    onCloseAll = onCloseAllTabs,
                    onTogglePin = { onTogglePinTab(index) }
                )
            }
        }

        // Split Editor Button
        IconButton(
            onClick = onToggleSplitEditor,
            modifier = Modifier
                .size(38.dp)
                .testTag("split_editor_button")
        ) {
            Icon(
                imageVector = Icons.Default.Splitscreen,
                contentDescription = "Split Editor",
                tint = if (isSplitEditor) VsCodePrimary else VsCodeTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TabItemView(
    tab: EditorTab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit,
    onCloseOthers: () -> Unit,
    onCloseAll: () -> Unit,
    onTogglePin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val fileEmoji = getFileEmoji(tab.title)
    val bgColor = if (isActive) VsCodeActiveTab else VsCodeInactiveTab
    val textColor = if (isActive) VsCodeTextPrimary else VsCodeTextSecondary

    Box {
        Row(
            modifier = Modifier
                .height(38.dp)
                .background(bgColor)
                .clickable { onClick() }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 16.dp)
                        .background(VsCodePrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(text = fileEmoji, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = tab.title,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (tab.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = VsCodeTextSecondary,
                    modifier = Modifier
                        .size(12.dp)
                        .clickable { onTogglePin() }
                )
            } else if (tab.isDirty) {
                // Dirty indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(VsCodeTextPrimary)
                        .clickable { onClose() }
                )
            } else {
                // Close tab X
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    tint = VsCodeTextSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onClose() }
                )
            }

            // Context menu toggle
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tab options",
                    tint = VsCodeTextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(VsCodeActiveTab)
        ) {
            DropdownMenuItem(
                text = { Text("Close", color = VsCodeTextPrimary, fontSize = 13.sp) },
                onClick = {
                    showMenu = false
                    onClose()
                }
            )
            DropdownMenuItem(
                text = { Text("Close Others", color = VsCodeTextPrimary, fontSize = 13.sp) },
                onClick = {
                    showMenu = false
                    onCloseOthers()
                }
            )
            DropdownMenuItem(
                text = { Text("Close All", color = VsCodeTextPrimary, fontSize = 13.sp) },
                onClick = {
                    showMenu = false
                    onCloseAll()
                }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        if (tab.isPinned) "Unpin Tab" else "Pin Tab",
                        color = VsCodeTextPrimary,
                        fontSize = 13.sp
                    )
                },
                onClick = {
                    showMenu = false
                    onTogglePin()
                }
            )
        }
    }
}

fun getFileEmoji(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "js", "jsx" -> "🟨"
        "ts", "tsx" -> "🔷"
        "py" -> "🐍"
        "kt", "kts" -> "🟣"
        "java" -> "☕"
        "html", "htm" -> "🌐"
        "css", "scss" -> "🎨"
        "json" -> "⚙️"
        "md", "markdown" -> "📝"
        "sql" -> "🗄️"
        "c", "cpp", "h" -> "🔵"
        "rs" -> "🦀"
        "go" -> "🐹"
        "sh", "bash" -> "🐚"
        "yml", "yaml" -> "📋"
        "xml" -> "📑"
        "dockerfile" -> "🐳"
        else -> if (fileName.lowercase() == "dockerfile") "🐳" else "📄"
    }
}
