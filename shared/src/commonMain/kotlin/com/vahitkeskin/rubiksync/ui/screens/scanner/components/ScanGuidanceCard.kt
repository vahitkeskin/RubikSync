package com.vahitkeskin.rubiksync.ui.screens.scanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun ScanGuidanceCard(
    appState: RubikAppState,
    guidanceMessage: String
) {
    val displayMessage = appState.infoMessage ?: guidanceMessage
    val isSuccess = appState.infoMessage != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    if (isSuccess) {
                        if (RubikTheme.colors.isDark) {
                            listOf(AccentGreenShadow, AccentGreenVeryDark)
                        } else {
                            listOf(AccentGreenFaintBg, AccentGreenSoftBg)
                        }
                    } else {
                        if (RubikTheme.colors.isDark) {
                            listOf(DarkBgSecondary, DarkBgTertiary)
                        } else {
                            listOf(LightBgPrimary, LightBgTertiary)
                        }
                    }
                )
            )
            .border(
                width = 0.5.dp,
                color = if (isSuccess) {
                    if (RubikTheme.colors.isDark) AccentGreenAlpha13 else AccentGreenAlpha20
                } else {
                    RubikTheme.colors.borderSubtle
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isSuccess) { appState.updateInfoMessage(null) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSuccess) "✅ $displayMessage" else "📸 $displayMessage",
            color = if (isSuccess) {
                if (RubikTheme.colors.isDark) AccentGreenBright else AccentGreenDark
            } else {
                RubikTheme.colors.textPrimary
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}
