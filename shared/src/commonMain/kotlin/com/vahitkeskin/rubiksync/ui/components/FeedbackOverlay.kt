package com.vahitkeskin.rubiksync.ui.components

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.delay

@Composable
fun BoxScope.FeedbackOverlay(
    appState: RubikAppState
) {
    // Error message
    FeedbackBanner(
        message = appState.errorMessage,
        emoji = "❌",
        gradientColors = listOf(AccentRedCrimson, AccentRedDark),
        borderColor = AccentRedVibrant,
        onDismiss = { appState.updateErrorMessage(null) },
        autoDismissMs = 4000
    )

    // Success message
    FeedbackBanner(
        message = appState.successMessage,
        emoji = "✅",
        gradientColors = listOf(AccentGreenMedium, AccentGreenPine),
        borderColor = AccentGreen,
        onDismiss = { appState.updateSuccessMessage(null) },
        autoDismissMs = 3000
    )

    // Info message (only show if no error or success)
    if (appState.errorMessage == null && appState.successMessage == null) {
        FeedbackBanner(
            message = appState.infoMessage,
            emoji = "ℹ️",
            gradientColors = listOf(DarkBorderSubtle, DarkGradientBg3),
            borderColor = AccentBlue,
            onDismiss = { appState.updateInfoMessage(null) },
            autoDismissMs = 3000
        )
    }
}

@Composable
private fun BoxScope.FeedbackBanner(
    message: String?,
    emoji: String,
    gradientColors: List<Color>,
    borderColor: Color,
    onDismiss: () -> Unit,
    autoDismissMs: Long = 3000
) {
    // Auto-dismiss timer
    LaunchedEffect(message) {
        if (message != null) {
            delay(autoDismissMs)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 350)
        ) + fadeIn(animationSpec = tween(durationMillis = 350)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 250)
        ) + fadeOut(animationSpec = tween(durationMillis = 250)),
        modifier = Modifier.align(Alignment.TopCenter)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp, start = 20.dp, end = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(gradientColors)
                )
                .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .clickable { onDismiss() }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = emoji,
                        fontSize = 14.sp
                    )
                    Text(
                        text = message ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✕",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
fun FeedbackOverlaySuccessDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val appState = rememberPreviewRubikAppState()
        appState.updateSuccessMessage("Tebrikler! Küp başarıyla çözüldü.")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFF0F172A))
        ) {
            FeedbackOverlay(appState = appState)
        }
    }
}

@Preview
@Composable
fun FeedbackOverlayErrorLightPreview() {
    PreviewRubikTheme(isDark = false) {
        val appState = rememberPreviewRubikAppState()
        appState.updateErrorMessage("Hata: Geçersiz küp tasarımı!")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.White)
        ) {
            FeedbackOverlay(appState = appState)
        }
    }
}
