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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GitBranch
import com.example.model.GitCommit
import com.example.model.GitFileStatus
import com.example.ui.editor.getFileEmoji
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeGitModified
import com.example.ui.theme.VsCodeGitUntracked
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeSidebar
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary
import java.io.File

@Composable
fun SourceControlPanel(
    currentBranch: String,
    branches: List<GitBranch>,
    gitStatuses: Map<String, GitFileStatus>,
    commits: List<GitCommit>,
    commitMessage: String,
    onCommitMessageChange: (String) -> Unit,
    onCommit: () -> Unit,
    onStageFile: (File) -> Unit,
    onUnstageFile: (File) -> Unit,
    onStageAll: () -> Unit,
    onUnstageAll: () -> Unit,
    onSwitchBranch: (String) -> Unit,
    onShowDiff: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBranchDropdown by remember { mutableStateOf(false) }
    var showCommitsView by remember { mutableStateOf(false) }

    val stagedFiles = gitStatuses.filter { it.value == GitFileStatus.STAGED }
    val unstagedFiles = gitStatuses.filter { it.value != GitFileStatus.STAGED }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeSidebar)
            .padding(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SOURCE CONTROL",
                color = VsCodeTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Branch selector
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(VsCodeSecondary)
                            .clickable { showBranchDropdown = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🌿 $currentBranch", color = VsCodeCyan, fontSize = 11.sp)
                    }

                    DropdownMenu(
                        expanded = showBranchDropdown,
                        onDismissRequest = { showBranchDropdown = false },
                        modifier = Modifier.background(VsCodeBg)
                    ) {
                        branches.forEach { b ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = (if (b.isCurrent) "✓ " else "  ") + b.name,
                                        color = if (b.isCurrent) VsCodeCyan else VsCodeTextPrimary,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = {
                                    showBranchDropdown = false
                                    onSwitchBranch(b.name)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Toggle commits history
                IconButton(
                    onClick = { showCommitsView = !showCommitsView },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Commit History",
                        tint = if (showCommitsView) VsCodePrimary else VsCodeTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (showCommitsView) {
            // Commit History View
            Text(
                text = "COMMITS (${commits.size})",
                color = VsCodeTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(commits) { commit ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(VsCodeBg)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = commit.hash,
                                color = VsCodeCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = commit.date,
                                color = VsCodeTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = commit.message,
                            color = VsCodeTextPrimary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = commit.author,
                            color = VsCodeTextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        } else {
            // Commit Message Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeBg)
                    .padding(8.dp)
            ) {
                BasicTextField(
                    value = commitMessage,
                    onValueChange = onCommitMessageChange,
                    textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 12.sp),
                    cursorBrush = SolidColor(VsCodePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("commit_message_input"),
                    decorationBox = { innerTextField ->
                        if (commitMessage.isEmpty()) {
                            Text("Message (Ctrl+Enter to commit)", color = VsCodeTextSecondary, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Commit Button
            Button(
                onClick = onCommit,
                enabled = commitMessage.isNotBlank() && gitStatuses.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VsCodePrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("commit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Commit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Staged Changes Section
                if (stagedFiles.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "STAGED CHANGES (${stagedFiles.size})",
                                color = VsCodeTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Unstage all",
                                tint = VsCodeTextSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onUnstageAll() }
                            )
                        }
                    }

                    items(stagedFiles.entries.toList()) { entry ->
                        val file = File(entry.key)
                        GitFileRow(
                            file = file,
                            status = entry.value,
                            isStaged = true,
                            onClick = { onShowDiff(file) },
                            onToggleStage = { onUnstageFile(file) }
                        )
                    }
                }

                // Changes (Unstaged) Section
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CHANGES (${unstagedFiles.size})",
                            color = VsCodeTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (unstagedFiles.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Stage all",
                                tint = VsCodeTextSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onStageAll() }
                            )
                        }
                    }
                }

                if (unstagedFiles.isEmpty()) {
                    item {
                        Text(
                            text = "No changes in working directory",
                            color = VsCodeTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                } else {
                    items(unstagedFiles.entries.toList()) { entry ->
                        val file = File(entry.key)
                        GitFileRow(
                            file = file,
                            status = entry.value,
                            isStaged = false,
                            onClick = { onShowDiff(file) },
                            onToggleStage = { onStageFile(file) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GitFileRow(
    file: File,
    status: GitFileStatus,
    isStaged: Boolean,
    onClick: () -> Unit,
    onToggleStage: () -> Unit
) {
    val statusLetter = when (status) {
        GitFileStatus.MODIFIED -> "M"
        GitFileStatus.UNTRACKED -> "U"
        GitFileStatus.STAGED -> "A"
        GitFileStatus.DELETED -> "D"
        else -> ""
    }
    val statusColor = when (status) {
        GitFileStatus.MODIFIED -> VsCodeGitModified
        GitFileStatus.UNTRACKED -> VsCodeGitUntracked
        else -> VsCodeCyan
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = getFileEmoji(file.name), fontSize = 12.sp)
        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = file.name,
            color = VsCodeTextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = statusLetter,
            color = statusColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp)
        )

        // Stage / Unstage icon
        Icon(
            imageVector = if (isStaged) Icons.Default.Remove else Icons.Default.Add,
            contentDescription = if (isStaged) "Unstage" else "Stage",
            tint = VsCodeTextSecondary,
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(3.dp))
                .clickable { onToggleStage() }
        )
    }
}
