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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExtensionItem
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun ExtensionsPanel(
    extensions: List<ExtensionItem>,
    onToggleExtension: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSidebar)
            .padding(10.dp)
    ) {
        Text(
            text = "EXTENSIONS",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(extensions) { ext ->
                ExtensionCard(
                    extension = ext,
                    onToggle = { onToggleExtension(ext.id) }
                )
            }
        }
    }
}

@Composable
private fun ExtensionCard(
    extension: ExtensionItem,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(VsCodeBg)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = extension.iconEmoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 10.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = extension.name,
                        color = VsCodeTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = extension.version,
                        color = VsCodeTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = extension.publisher,
                    color = VsCodeCyan,
                    fontSize = 11.sp
                )

                Text(
                    text = extension.description,
                    color = VsCodeTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "★ ${extension.rating}  •  ${extension.downloads} installs",
                        color = VsCodeTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = extension.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = VsCodePrimary,
                    uncheckedThumbColor = VsCodeTextSecondary,
                    uncheckedTrackColor = VsCodeSecondary
                )
            )
        }
    }
}
