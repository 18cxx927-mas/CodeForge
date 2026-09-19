package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun BreadcrumbsBar(
    filePath: String?,
    modifier: Modifier = Modifier
) {
    if (filePath.isNullOrEmpty()) return

    val parts = filePath.split("/").filter { it.isNotEmpty() }
    val relevantParts = if (parts.size > 3) parts.takeLast(3) else parts

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(VsCodeBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        relevantParts.forEachIndexed { index, part ->
            val isLast = index == relevantParts.size - 1
            if (index == 0) {
                Text(
                    text = "📁",
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = part,
                color = if (isLast) VsCodeTextPrimary else VsCodeTextSecondary,
                fontSize = 11.sp
            )

            if (!isLast) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = VsCodeTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
