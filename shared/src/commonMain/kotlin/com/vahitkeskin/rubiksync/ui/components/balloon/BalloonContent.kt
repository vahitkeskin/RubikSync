package com.vahitkeskin.rubiksync.ui.components.balloon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.state.AccentOrange
import com.vahitkeskin.rubiksync.ui.state.Slate800

@Composable
fun BalloonContent(
    text: String,
    onDismiss: () -> Unit,
    arrowX: Float,
    isBelow: Boolean,
    animationProgress: Float,
    transformOrigin: TransformOrigin
) {
    val arrowWidthPx = with(LocalDensity.current) { 18.dp.toPx() }
    val arrowHeightPx = with(LocalDensity.current) { 10.dp.toPx() }
    val cornerRadiusPx = with(LocalDensity.current) { 16.dp.toPx() }

    val balloonShape = remember(arrowX, isBelow) {
        BalloonShape(
            arrowWidth = arrowWidthPx,
            arrowHeight = arrowHeightPx,
            arrowX = arrowX,
            isBelow = isBelow,
            cornerRadius = cornerRadiusPx
        )
    }

    Box(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .graphicsLayer {
                alpha = animationProgress
                scaleX = animationProgress
                scaleY = animationProgress
                this.transformOrigin = transformOrigin
            }
            .clickable(indication = null, interactionSource = null) { onDismiss() }
            .shadow(12.dp, balloonShape)
            .clip(balloonShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(AccentOrange.copy(alpha = 0.4f), Color.White)
                ),
                shape = balloonShape
            )
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = if (isBelow) 22.dp else 12.dp,
                bottom = if (!isBelow) 22.dp else 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp
            ),
            color = Slate800,
            textAlign = TextAlign.Center
        )
    }
}