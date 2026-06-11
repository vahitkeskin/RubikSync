package com.vahitkeskin.rubiksync.ui.screens.scanner.components

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
internal fun ScannerShowcaseOverlay(
    appState: RubikAppState,
    scannerTargetBounds: Rect?,
    scannerTargetCornerRadius: Dp
) {
    val isShowcaseActive = appState.scannerShowcaseStep != 0 && !appState.isScannerShowcaseCompleted
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
                    .let { modifier ->
                        if (isShowcaseActive) {
                            modifier.clickable(
                                onClick = { appState.advanceScannerShowcase() },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                        } else {
                            modifier
                        }
                    }
            ) {
                drawRect(color = Slate900.copy(alpha = overlayAlpha))
                scannerTargetBounds?.let { rect ->
                    val localLeft = rect.left - canvasPositionInRoot.x
                    val localTop = rect.top - canvasPositionInRoot.y
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(localLeft, localTop),
                        size = Size(rect.width, rect.height),
                        cornerRadius = CornerRadius(
                            scannerTargetCornerRadius.toPx(),
                            scannerTargetCornerRadius.toPx()
                        ),
                        blendMode = BlendMode.Clear
                    )
                }
            }

            // Skip Showcase/Tutorial Button (styled as a premium, slate button aligned to top-right corner)
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
                    .background(Slate800) // Solid Slate 800
                    .border(1.dp, Slate600, RoundedCornerShape(20.dp)) // Solid Slate 600 border
                    .let { modifier ->
                        if (isShowcaseActive) {
                            modifier.clickable {
                                appState.updateScannerShowcaseStep(0)
                                appState.updateScannerShowcaseCompleted(true)
                            }
                        } else {
                            modifier
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appState.strings.skipShowcase,
                    color = Slate100, // Slate 100
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
