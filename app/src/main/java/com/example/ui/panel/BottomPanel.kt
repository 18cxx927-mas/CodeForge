package com.example.ui.panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.model.ProblemItem
import com.example.model.ProblemSeverity
import com.example.model.TerminalLine
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeError
import com.example.ui.theme.VsCodeGreen
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTerminalBg
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import com.example.ui.theme.VsCodeWarning
import com.example.viewmodel.BottomPanelTab

@Composable
fun BottomPanel(
    activeTab: BottomPanelTab,
    onSelectTab: (BottomPanelTab) -> Unit,
    onClose: () -> Unit,
    // Terminal params
    terminalLines: List<TerminalLine>,
    terminalInput: String,
    onTerminalInputChange: (String) -> Unit,
    onExecuteCommand: () -> Unit,
    onClearTerminal: () -> Unit,
    // Problems params
    problems: List<ProblemItem>,
    onProblemClick: (ProblemItem) -> Unit,
    // Output logs
    outputLogs: List<String>,
    // Debug console
    debugLogs: List<String>,
    debugInput: String,
    onDebugInputChange: (String) -> Unit,
    onExecuteDebug: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(VsCodeTerminalBg)
    ) {
        // Tab Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(VsCodeSidebar)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PanelTabItem(
                    title = "TERMINAL",
                    isSelected = activeTab == BottomPanelTab.TERMINAL,
                    onClick = { onSelectTab(BottomPanelTab.TERMINAL) }
                )
                PanelTabItem(
                    title = "PROBLEMS",
                    isSelected = activeTab == BottomPanelTab.PROBLEMS,
                    badgeCount = problems.size,
                    onClick = { onSelectTab(BottomPanelTab.PROBLEMS) }
                )
                PanelTabItem(
                    title = "OUTPUT",
                    isSelected = activeTab == BottomPanelTab.OUTPUT,
                    onClick = { onSelectTab(BottomPanelTab.OUTPUT) }
                )
                PanelTabItem(
                    title = "DEBUG CONSOLE",
                    isSelected = activeTab == BottomPanelTab.DEBUG_CONSOLE,
                    onClick = { onSelectTab(BottomPanelTab.DEBUG_CONSOLE) }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (activeTab == BottomPanelTab.TERMINAL) {
                    IconButton(
                        onClick = onClearTerminal,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Clear terminal",
                            tint = VsCodeTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close panel",
                        tint = VsCodeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (activeTab) {
                BottomPanelTab.TERMINAL -> {
                    TerminalView(
                        lines = terminalLines,
                        input = terminalInput,
                        onInputChange = onTerminalInputChange,
                        onExecute = onExecuteCommand
                    )
                }
                BottomPanelTab.PROBLEMS -> {
                    ProblemsView(
                        problems = problems,
                        onProblemClick = onProblemClick
                    )
                }
                BottomPanelTab.OUTPUT -> {
                    OutputView(logs = outputLogs)
                }
                BottomPanelTab.DEBUG_CONSOLE -> {
                    DebugConsoleView(
                        logs = debugLogs,
                        input = debugInput,
                        onInputChange = onDebugInputChange,
                        onExecute = onExecuteDebug
                    )
                }
            }
        }
    }
}

@Composable
private fun PanelTabItem(
    title: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = if (isSelected) VsCodeTextPrimary else VsCodeTextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 0.5.sp
            )

            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(VsCodePrimary)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(text = badgeCount.toString(), color = Color.White, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun TerminalView(
    lines: List<TerminalLine>,
    input: String,
    onInputChange: (String) -> Unit,
    onExecute: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(lines) { line ->
                val color = when {
                    line.isError -> VsCodeError
                    line.isSuccess -> VsCodeGreen
                    line.isPrompt -> VsCodeCyan
                    else -> VsCodeTextPrimary
                }
                Text(
                    text = line.text,
                    color = color,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }

        // Terminal Command Prompt Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VsCodeSidebar)
                .clip(RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$ ",
                color = VsCodeGreen,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            BasicTextField(
                value = input,
                onValueChange = onInputChange,
                textStyle = TextStyle(
                    color = VsCodeTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                ),
                cursorBrush = SolidColor(VsCodeGreen),
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input_field"),
                decorationBox = { innerTextField ->
                    if (input.isEmpty()) {
                        Text(
                            "Type command (e.g. ls, help, run)...",
                            color = VsCodeTextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    innerTextField()
                }
            )

            IconButton(
                onClick = onExecute,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("terminal_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute",
                    tint = VsCodePrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun ProblemsView(
    problems: List<ProblemItem>,
    onProblemClick: (ProblemItem) -> Unit
) {
    if (problems.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓ No problems have been detected in the workspace.",
                color = VsCodeGreen,
                fontSize = 12.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(problems) { problem ->
                val (icon, color) = when (problem.severity) {
                    ProblemSeverity.ERROR -> "❌" to VsCodeError
                    ProblemSeverity.WARNING -> "⚠️" to VsCodeWarning
                    ProblemSeverity.INFO -> "ℹ️" to VsCodeCyan
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProblemClick(problem) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = icon, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = problem.message,
                            color = color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${problem.fileName} [${problem.line}, ${problem.column}]",
                            color = VsCodeTextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutputView(logs: List<String>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(logs) { log ->
            Text(
                text = log,
                color = VsCodeTextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun DebugConsoleView(
    logs: List<String>,
    input: String,
    onInputChange: (String) -> Unit,
    onExecute: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(logs) { log ->
                Text(
                    text = log,
                    color = VsCodeCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            if (logs.isEmpty()) {
                item {
                    Text(
                        text = "Debug console active. Expressions evaluated here.",
                        color = VsCodeTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VsCodeSidebar)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "> ", color = VsCodeCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            BasicTextField(
                value = input,
                onValueChange = onInputChange,
                textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                cursorBrush = SolidColor(VsCodePrimary),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (input.isEmpty()) {
                        Text("Evaluate in debug console...", color = VsCodeTextSecondary, fontSize = 11.sp)
                    }
                    innerTextField()
                }
            )
            IconButton(onClick = onExecute, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = VsCodePrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
