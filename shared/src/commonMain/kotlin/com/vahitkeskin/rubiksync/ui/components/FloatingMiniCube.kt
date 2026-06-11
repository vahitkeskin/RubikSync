package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeRenderer
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.state.AccentOrange
import com.vahitkeskin.rubiksync.ui.state.AccentBlue
import kotlin.math.roundToInt

@Composable
fun FloatingMiniCube(
    appState: RubikAppState,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    // A solve is active if there is an unresolved active solution
    val isSolving =
        appState.activeSolution != null && appState.currentSolutionStep < appState.activeSolution!!.size
    val isTargetActive = currentRoute == "settings" && isSolving

    SideEffect {
        println("FloatingMiniCube debug: currentRoute='$currentRoute', isSolving=$isSolving, activeSolutionSize=${appState.activeSolution?.size}, step=${appState.currentSolutionStep}, isTargetActive=$isTargetActive, mainBounds=${appState.mainCubeBounds}")
    }

    val transitionProgress = remember { Animatable(0f) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isTargetActive) {
        if (isTargetActive) {
            isVisible = true
            transitionProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        } else {
            transitionProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
            isVisible = false
        }
    }

    val isCurrentlyVisible = isVisible || transitionProgress.value > 0f
    if (!isCurrentlyVisible) return

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()

        val density = LocalDensity.current
        val pipSizeDp = 150.dp
        val pipSizePx = with(density) { pipSizeDp.toPx() }
        val marginDp = 24.dp
        val marginPx = with(density) { marginDp.toPx() }

        // System insets for navigation bar and status bar to keep PIP in the safe area
        val navBarHeightPx =
            androidx.compose.foundation.layout.WindowInsets.navigationBars.getBottom(density)
                .toFloat()
        val statusBarHeightPx =
            androidx.compose.foundation.layout.WindowInsets.statusBars.getTop(density).toFloat()

        val topLimitPx = statusBarHeightPx + marginPx
        val bottomLimitPx = navBarHeightPx + marginPx

        // Default resting position for PIP: bottom-right corner, resting above the navigation bar
        val xEndDefault = screenWidthPx - pipSizePx - marginPx
        val yEndDefault = screenHeightPx - pipSizePx - bottomLimitPx

        // Draggable state offsets
        var dragOffsetX by remember { mutableStateOf(0f) }
        var dragOffsetY by remember { mutableStateOf(0f) }

        // Reset offsets when fully collapsed to start fresh next time
        LaunchedEffect(transitionProgress.value) {
            if (transitionProgress.value == 0f) {
                dragOffsetX = 0f
                dragOffsetY = 0f
            }
        }

        // Target resting position considering manual drag offsets (clamped inside safe area)
        val xEnd =
            (xEndDefault + dragOffsetX).coerceIn(marginPx, screenWidthPx - pipSizePx - marginPx)
        val yEnd = (yEndDefault + dragOffsetY).coerceIn(
            topLimitPx,
            screenHeightPx - pipSizePx - bottomLimitPx
        )

        // Start coordinates: Main home cube's coordinates (or center of screen fallback)
        val mainBounds = appState.mainCubeBounds
        val xStart = mainBounds?.left ?: (screenWidthPx / 2f - pipSizePx / 2f)
        val yStart = mainBounds?.top ?: (screenHeightPx / 2f - pipSizePx / 2f)
        val wStart = mainBounds?.width ?: pipSizePx
        val hStart = mainBounds?.height ?: pipSizePx

        // Interpolate size and coordinates using the transition progress
        val t = transitionProgress.value
        val currentWidthPx = wStart + (pipSizePx - wStart) * t
        val currentHeightPx = hStart + (pipSizePx - hStart) * t
        val currentXPx = xStart + (xEnd - xStart) * t
        val currentYPx = yStart + (yEnd - yStart) * t

        val currentWidthDp = with(density) { currentWidthPx.toDp() }
        val currentHeightDp = with(density) { currentHeightPx.toDp() }

        val currentX = currentXPx.roundToInt()
        val currentY = currentYPx.roundToInt()

        // Enable dragging only when the PIP is fully opened (to avoid jank during transition)
        val dragModifier = if (t >= 0.95f) {
            Modifier.pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragOffsetX += dragAmount.x
                    dragOffsetY += dragAmount.y
                }
            }
        } else Modifier

        val isDarkTheme = RubikTheme.colors.isDark
        val cornerRadius = with(density) { (16.dp.toPx() * t).toDp() }
        val bgAlpha = 0.85f * t
        val cardBgColor = RubikTheme.colors.backgroundSecondary.copy(alpha = bgAlpha)
        val borderColor = RubikTheme.colors.cardBorder.copy(alpha = t)
        val shadowElevation = (8 * t).dp

        Box(
            modifier = Modifier
                .offset { IntOffset(currentX, currentY) }
                .size(currentWidthDp, currentHeightDp)
                .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius))
                .clip(RoundedCornerShape(cornerRadius))
                .background(cardBgColor)
                .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
                .then(dragModifier)
        ) {
            // 3D Rubik's Cube Canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val renderer = CubeRenderer(
                        state = appState.cubeState,
                        yaw = appState.yaw,
                        pitch = appState.pitch,
                        cameraDistance = appState.cameraDistance + 2f, // Zoom out slightly to sit nicely inside bounds
                        panX = appState.panX,
                        panY = appState.panY,
                        isDark = isDarkTheme
                    )
                    renderer.draw(this, size.width, size.height)
                }
            }

            // Sleek information and close overlays (fades in as transition reaches completion)
            if (t > 0.6f) {
                val alpha = (t - 0.6f) / 0.4f // Normalize opacity from 0.0 to 1.0

                val totalSteps = appState.activeSolution?.size ?: 1
                val currentStep = appState.currentSolutionStep
                val progress = currentStep.toFloat() / totalSteps.toFloat()

                // Step count indicator badge in top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f * alpha),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "$currentStep/$totalSteps",
                        color = Color.White.copy(alpha = alpha),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Close button in top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f * alpha),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            appState.updatePlaybackRunning(false)
                            appState.updateActiveSolution(null) // Reset solution and close PIP
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = Color.White.copy(alpha = alpha),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Glowing progress bar at the bottom boundary
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.2f * alpha))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(AccentOrange, AccentBlue)
                                )
                            )
                    )
                }
            }
        }
    }
}
