package com.vahitkeskin.rubiksync.ui.screens.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.components.balloon.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.*
import com.vahitkeskin.rubiksync.utils.safe

@Composable
internal fun ScanCard(
    appState: RubikAppState,
    onStartScanWizard: () -> Unit,
    onPositioned: (Rect) -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        if (RubikTheme.colors.isDark) {
                            listOf(DarkGradientBg2, DarkGradientBg4)
                        } else {
                            listOf(AccentBlueFaintBg, AccentBlueSoftBg)
                        }
                    )
                )
                .border(1.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = appState.strings.cameraScanTitle,
                    color = RubikTheme.colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = appState.strings.cameraScanSubtitle,
                    color = RubikTheme.colors.textSecondary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onStartScanWizard.safe(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RubikTheme.colors.accentBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = appState.strings.scanAction,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
        AuraBalloon(
            text = appState.strings.showcaseEditorScan,
            isVisible = appState.editorShowcaseStep == 5 && !appState.isEditorShowcaseCompleted,
            isBelow = true,
            onDismiss = { appState.advanceEditorShowcase() }
        )
    }
}
