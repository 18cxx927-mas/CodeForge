package com.example.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun CodeForgeTopBar(
    activeFileName: String?,
    isSidebarVisible: Boolean,
    onToggleSidebar: () -> Unit,
    onOpenCommandPalette: () -> Unit,
    onOpenQuickOpen: () -> Unit,
    onRunActiveFile: () -> Unit,
    onToggleTerminal: () -> Unit,
    onToggleZenMode: () -> Unit,
    onToggleMarkdownPreview: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(VsCodeActivityBar)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Sidebar Toggle & App Brand
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleSidebar,
                modifier = Modifier
                    .size(34.dp)
                    .testTag("toggle_sidebar_button")
            ) {
                Icon(
                    imageVector = if (isSidebarVisible) Icons.AutoMirrored.Filled.MenuOpen else Icons.Default.Menu,
                    contentDescription = "Toggle Sidebar",
                    tint = VsCodeTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "⚡ CodeForge",
                color = VsCodeCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Center: Global Command Search Pill
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(VsCodeSecondary)
                .clickable { onOpenCommandPalette() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("top_bar_search_pill"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = VsCodeTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (activeFileName != null) "$activeFileName — CodeForge" else "Type > for commands",
                    color = VsCodeTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        // Right: Run, Terminal, Overflow Menu
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRunActiveFile,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_bar_run_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Run",
                    tint = VsCodeGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onToggleTerminal,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("top_bar_terminal_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal",
                    tint = VsCodeTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("top_bar_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = VsCodeTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(VsCodeActivityBar)
                ) {
                    DropdownMenuItem(
                        text = { Text("Command Palette...", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showMenu = false
                            onOpenCommandPalette()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Go to File...", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showMenu = false
                            onOpenQuickOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Toggle Markdown Preview", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showMenu = false
                            onToggleMarkdownPreview()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Toggle Zen Mode", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showMenu = false
                            onToggleZenMode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            showMenu = false
                            onOpenSettings()
                        }
                    )
                }
            }
        }
    }
}
