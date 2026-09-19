package com.example.ui.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeActivityBar
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.viewmodel.SidebarPanel

@Composable
fun ActivityBar(
    activePanel: SidebarPanel,
    isSidebarVisible: Boolean,
    onSelectPanel: (SidebarPanel) -> Unit,
    gitChangesCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .background(VsCodeActivityBar),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Activity Icons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActivityIconItem(
                icon = Icons.Default.Folder,
                contentDescription = "Explorer",
                isSelected = isSidebarVisible && activePanel == SidebarPanel.EXPLORER,
                testTag = "activity_explorer",
                onClick = { onSelectPanel(SidebarPanel.EXPLORER) }
            )
            ActivityIconItem(
                icon = Icons.Default.Search,
                contentDescription = "Search",
                isSelected = isSidebarVisible && activePanel == SidebarPanel.SEARCH,
                testTag = "activity_search",
                onClick = { onSelectPanel(SidebarPanel.SEARCH) }
            )
            ActivityIconItem(
                icon = Icons.Default.AccountTree,
                contentDescription = "Source Control",
                isSelected = isSidebarVisible && activePanel == SidebarPanel.SOURCE_CONTROL,
                badgeCount = gitChangesCount,
                testTag = "activity_source_control",
                onClick = { onSelectPanel(SidebarPanel.SOURCE_CONTROL) }
            )
            ActivityIconItem(
                icon = Icons.Default.BugReport,
                contentDescription = "Run and Debug",
                isSelected = isSidebarVisible && activePanel == SidebarPanel.RUN_DEBUG,
                testTag = "activity_run_debug",
                onClick = { onSelectPanel(SidebarPanel.RUN_DEBUG) }
            )
            ActivityIconItem(
                icon = Icons.Default.Extension,
                contentDescription = "Extensions",
                isSelected = isSidebarVisible && activePanel == SidebarPanel.EXTENSIONS,
                testTag = "activity_extensions",
                onClick = { onSelectPanel(SidebarPanel.EXTENSIONS) }
            )
        }

        // Bottom Settings Icon
        ActivityIconItem(
            icon = Icons.Default.Settings,
            contentDescription = "Settings",
            isSelected = isSidebarVisible && activePanel == SidebarPanel.SETTINGS,
            testTag = "activity_settings",
            onClick = { onSelectPanel(SidebarPanel.SETTINGS) }
        )
    }
}

@Composable
private fun ActivityIconItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    testTag: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Left active white bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .align(Alignment.CenterStart)
                    .background(Color.White)
            )
        }

        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = VsCodePrimary,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            fontSize = 9.sp
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = if (isSelected) Color.White else VsCodeTextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isSelected) Color.White else VsCodeTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
