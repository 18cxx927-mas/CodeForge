package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodePrimary
import com.example.ui.theme.VsCodeSecondary
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun CreateFileDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(VsCodeBg)
                .padding(16.dp)
        ) {
            Text(
                text = "📄 New File",
                color = VsCodeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeSecondary)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(VsCodePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("new_file_name_input"),
                    decorationBox = { innerTextField ->
                        if (fileName.isEmpty()) {
                            Text("e.g. script.py, styles.css", color = VsCodeTextSecondary, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (fileName.isNotBlank()) onConfirm(fileName.trim())
                    },
                    enabled = fileName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodePrimary),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("create_file_confirm_button")
                ) {
                    Text("Create", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CreateFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(VsCodeBg)
                .padding(16.dp)
        ) {
            Text(
                text = "📁 New Folder",
                color = VsCodeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeSecondary)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(VsCodePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("new_folder_name_input"),
                    decorationBox = { innerTextField ->
                        if (folderName.isEmpty()) {
                            Text("e.g. components, utils", color = VsCodeTextSecondary, fontSize = 12.sp)
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (folderName.isNotBlank()) onConfirm(folderName.trim())
                    },
                    enabled = folderName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodePrimary),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("create_folder_confirm_button")
                ) {
                    Text("Create", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RenameDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(VsCodeBg)
                .padding(16.dp)
        ) {
            Text(
                text = "✏️ Rename",
                color = VsCodeTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeSecondary)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                BasicTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    textStyle = TextStyle(color = VsCodeTextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(VsCodePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("rename_input")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = VsCodeTextSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newName.isNotBlank()) onConfirm(newName.trim())
                    },
                    enabled = newName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = VsCodePrimary),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.testTag("rename_confirm_button")
                ) {
                    Text("Rename", color = Color.White)
                }
            }
        }
    }
}
