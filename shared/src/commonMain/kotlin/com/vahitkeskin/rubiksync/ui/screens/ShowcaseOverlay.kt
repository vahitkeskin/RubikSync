package com.vahitkeskin.rubiksync.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun ShowcaseOverlay(
    appState: RubikAppState,
    editorTargetBounds: Rect?,
    editorTargetCornerRadius: Dp
) {
    val isShowcaseActive = appState.editorShowcaseStep != 0 && !appState.isEditorShowcaseCompleted
    val overlayAlpha by animateFloatAsState(
        targetValue = if (isShowcaseActive) 0.85f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val buttonScaleAndAlpha by animateFloatAsState(
        targetValue = if (isShowcaseActive) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        )
    )

    var canvasPositionInRoot by remember { mutableStateOf(Offset.Zero) }

    if (overlayAlpha > 0f) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coords ->
                        canvasPositionInRoot = coords.positionInRoot()
                    }
                    .graphicsLayer(alpha = 0.99f)
                    .clickable(
                        onClick = { appState.advanceEditorShowcase() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                drawRect(color = Slate900.copy(alpha = overlayAlpha))
                editorTargetBounds?.let { rect ->
                    val localLeft = rect.left - canvasPositionInRoot.x
                    val localTop = rect.top - canvasPositionInRoot.y
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(localLeft, localTop),
                        size = Size(rect.width, rect.height),
                        cornerRadius = CornerRadius(editorTargetCornerRadius.toPx(), editorTargetCornerRadius.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
            }

            // Skip Showcase Button (styled as a premium, slate button aligned to top-right corner)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(end = 16.dp, top = 12.dp)
                    .graphicsLayer {
                        scaleX = buttonScaleAndAlpha
                        scaleY = buttonScaleAndAlpha
                        alpha = buttonScaleAndAlpha
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate800)
                    .border(1.dp, Slate600, RoundedCornerShape(20.dp))
                    .clickable(enabled = isShowcaseActive) {
                        appState.updateEditorShowcaseStep(0)
                        appState.updateEditorShowcaseCompleted(true)
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appState.strings.skipShowcase,
                    color = Slate100,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}