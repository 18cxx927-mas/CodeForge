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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.EditorSettings
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun SettingsPanel(
    settings: EditorSettings,
    onSettingsChanged: (EditorSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var themeDropdownExpanded by remember { mutableStateOf(false) }
    val themes = listOf(
        "vs-dark" to "Dark+ (default dark)",
        "vs" to "Light (Visual Studio)",
        "hc-black" to "Dark High Contrast",
        "monokai" to "Monokai"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSidebar)
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "PREFERENCES: SETTINGS",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Theme Selector
        SettingSectionTitle("Color Theme")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeBg)
                .clickable { themeDropdownExpanded = true }
                .padding(10.dp)
                .testTag("theme_selector_dropdown")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = themes.firstOrNull { it.first == settings.theme }?.second ?: "Dark+ (default dark)",
                    color = VsCodeCyan,
                    fontSize = 12.sp
                )
                Text(text = "▼", color = VsCodeTextSecondary, fontSize = 10.sp)
            }

            DropdownMenu(
                expanded = themeDropdownExpanded,
                onDismissRequest = { themeDropdownExpanded = false },
                modifier = Modifier.background(VsCodeBg)
            ) {
                themes.forEach { (id, label) ->
                    DropdownMenuItem(
                        text = { Text(label, color = VsCodeTextPrimary, fontSize = 12.sp) },
                        onClick = {
                            themeDropdownExpanded = false
                            onSettingsChanged(settings.copy(theme = id))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Font Size Slider
        SettingSectionTitle("Font Size (${settings.fontSize}px)")
        Slider(
            value = settings.fontSize.toFloat(),
            onValueChange = { onSettingsChanged(settings.copy(fontSize = it.toInt())) },
            valueRange = 10f..24f,
            steps = 14,
            colors = SliderDefaults.colors(
                thumbColor = VsCodePrimary,
                activeTrackColor = VsCodePrimary,
                inactiveTrackColor = VsCodeSecondary
            ),
            modifier = Modifier.testTag("font_size_slider")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Size
        SettingSectionTitle("Tab Size")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 4, 8).forEach { size ->
                val isSelected = settings.tabSize == size
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) VsCodePrimary else VsCodeSecondary)
                        .clickable { onSettingsChanged(settings.copy(tabSize = size)) }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$size spaces",
                        color = if (isSelected) Color.White else VsCodeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggles
        SettingSectionTitle("Editor Features")

        SettingSwitchRow(
            title = "Word Wrap",
            subtitle = "Lines will wrap at the edge of the editor view",
            checked = settings.wordWrap,
            onCheckedChange = { onSettingsChanged(settings.copy(wordWrap = it)) }
        )

        SettingSwitchRow(
            title = "Minimap",
            subtitle = "Controls whether the code minimap is shown",
            checked = settings.minimap,
            onCheckedChange = { onSettingsChanged(settings.copy(minimap = it)) }
        )

        SettingSwitchRow(
            title = "Bracket Pair Colorization",
            subtitle = "Colorize matching brackets for easier readability",
            checked = settings.bracketPairColorization,
            onCheckedChange = { onSettingsChanged(settings.copy(bracketPairColorization = it)) }
        )

        SettingSwitchRow(
            title = "Mobile Accessory Bar",
            subtitle = "Quick coding symbols bar above keyboard",
            checked = settings.showAccessoryBar,
            onCheckedChange = { onSettingsChanged(settings.copy(showAccessoryBar = it)) }
        )

        SettingSwitchRow(
            title = "Smooth Scrolling",
            subtitle = "Animate scrolling using smooth physics",
            checked = settings.smoothScrolling,
            onCheckedChange = { onSettingsChanged(settings.copy(smoothScrolling = it)) }
        )

        SettingSwitchRow(
            title = "Show Hidden Files",
            subtitle = "Show dotfiles like .gitignore in explorer",
            checked = settings.showHiddenFiles,
            onCheckedChange = { onSettingsChanged(settings.copy(showHiddenFiles = it)) }
        )
    }
}

@Composable
private fun SettingSectionTitle(title: String) {
    Text(
        text = title,
        color = VsCodeTextPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = VsCodeTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = VsCodeTextSecondary, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = VsCodePrimary,
                uncheckedThumbColor = VsCodeTextSecondary,
                uncheckedTrackColor = VsCodeSecondary
            )
        )
    }
}
