package com.example.ui.sidebar

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EditorTab
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodeOrange
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun RunDebugPanel(
    activeTab: EditorTab?,
    onRunActiveFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSidebar)
            .padding(10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "RUN AND DEBUG",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Target File Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(VsCodeBg)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = "TARGET FILE",
                    color = VsCodeTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = activeTab?.title ?: "No file open",
                    color = if (activeTab != null) VsCodeCyan else VsCodeTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Language: ${activeTab?.language ?: "none"}",
                    color = VsCodeTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Big Run Button
        Button(
            onClick = onRunActiveFile,
            enabled = activeTab != null,
            colors = ButtonDefaults.buttonColors(
                containerColor = VsCodeGreen,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .testTag("run_active_file_button")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Run Current File", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Variables Section
        SectionHeader(title = "VARIABLES")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeBg)
                .padding(8.dp)
        ) {
            Column {
                VariableRow(name = "process.env.NODE_ENV", value = "\"development\"")
                VariableRow(name = "WORKSPACE_DIR", value = "\"~/workspace\"")
                VariableRow(name = "RUNTIME", value = "\"Android Native (V8/Monaco)\"")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Call Stack Section
        SectionHeader(title = "CALL STACK")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeBg)
                .padding(8.dp)
        ) {
            Text(
                text = "Thread 1: Main (Running)",
                color = VsCodeCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Breakpoints Section
        SectionHeader(title = "BREAKPOINTS")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeBg)
                .padding(8.dp)
        ) {
            Text(
                text = "Tap any line number in Monaco to toggle breakpoints",
                color = VsCodeTextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = VsCodeTextSecondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun VariableRow(name: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = name, color = VsCodeCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = VsCodeOrange, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}
