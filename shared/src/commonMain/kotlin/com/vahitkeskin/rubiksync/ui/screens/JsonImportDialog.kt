package com.vahitkeskin.rubiksync.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
internal fun JsonImportDialog(
    appState: RubikAppState,
    onDismissRequest: () -> Unit
) {
    var jsonInputText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                appState.strings.importJsonTitle,
                color = RubikTheme.colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = appState.strings.pasteJsonDesc,
                    color = RubikTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 2
                )
                OutlinedTextField(
                    value = jsonInputText,
                    onValueChange = { jsonInputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RubikTheme.colors.textPrimary,
                        unfocusedTextColor = RubikTheme.colors.textPrimary,
                        focusedBorderColor = RubikTheme.colors.accentOrange,
                        unfocusedBorderColor = RubikTheme.colors.borderPrimary
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val success = appState.importEditorFacesFromJson(jsonInputText)
                    if (success) {
                        onDismissRequest()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = RubikTheme.colors.accentOrange),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = appState.strings.importJsonButton,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest,
                colors = ButtonDefaults.buttonColors(containerColor = RubikTheme.colors.backgroundSecondary),
                border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = appState.strings.cancelAction,
                    color = RubikTheme.colors.textPrimary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        },
        containerColor = RubikTheme.colors.cardBackground,
        shape = RoundedCornerShape(20.dp)
    )
}