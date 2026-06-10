package com.vahitkeskin.rubiksync.ui.screens.editor.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun BottomActionButtons(
    appState: RubikAppState,
    onDismiss: () -> Unit,
    onPositioned: (Rect) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = RubikTheme.colors.backgroundSecondary,
                contentColor = RubikTheme.colors.textPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
        ) {
            Text(
                text = appState.strings.cancelButton,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Button(
            onClick = { appState.resetEditorFaces() },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (RubikTheme.colors.isDark) SelectionDarkMagenta else AccentRedFaintBg,
                contentColor = RubikTheme.colors.accentRed
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            border = BorderStroke(1.dp, if (RubikTheme.colors.isDark) SelectionMediumMagenta else AccentRedSoftBg),
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
        ) {
            Text(
                text = appState.strings.clearButton,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .weight(1.1f)
                .height(42.dp)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                }
        ) {
            Button(
                onClick = {
                    appState.applyEditorState(onSuccess = onDismiss)
                },
                enabled = !appState.cubeState.isAnimating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RubikTheme.colors.accentOrange,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = appState.strings.applyButton,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
            AuraBalloon(
                text = appState.strings.showcaseEditorApply,
                isVisible = appState.editorShowcaseStep == 4 && !appState.isEditorShowcaseCompleted,
                isBelow = false,
                onDismiss = { appState.advanceEditorShowcase() }
            )
        }
    }
}
