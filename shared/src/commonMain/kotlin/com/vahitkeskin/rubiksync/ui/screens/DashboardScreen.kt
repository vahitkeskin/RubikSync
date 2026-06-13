package com.vahitkeskin.rubiksync.ui.screens

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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.ui.components.FeedbackOverlay
import com.vahitkeskin.rubiksync.ui.cube.InteractiveCubeCanvas
import com.vahitkeskin.rubiksync.rememberShakeDetector
import com.vahitkeskin.rubiksync.BindDashboardBackHandler
import com.vahitkeskin.rubiksync.ui.controlpanel.ControlPanel
import com.vahitkeskin.rubiksync.ui.controlpanel.PlaybackController
import com.vahitkeskin.rubiksync.ui.dashboard.DashboardHeader
import com.vahitkeskin.rubiksync.ui.navigation.Screen
import com.vahitkeskin.rubiksync.ui.state.*
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    appState: RubikAppState,
    cubeState: RubikCubeState,
    navController: NavController
) {
    val isSolving = appState.activeSolution != null && appState.currentSolutionStep < (appState.activeSolution?.size ?: 0)
    BindDashboardBackHandler(enabled = isSolving)

    val overlayAlpha by animateFloatAsState(
        targetValue = if (appState.showcaseStep != 0 && !appState.isShowcaseCompleted) 0.85f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val isShowcaseActive =
        appState.showcaseStep != 0 && !appState.isShowcaseCompleted
    val buttonScaleAndAlpha by animateFloatAsState(
        targetValue = if (isShowcaseActive) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        )
    )

    // Shake to scramble feature detection
    rememberShakeDetector(
        enabled = appState.isShakeToScrambleEnabled && appState.isCubeEditable && !cubeState.isAnimating
    ) {
        appState.clearManualMoves()
        appState.coroutineScope.launch {
            cubeState.scramble()
        }
    }

    LaunchedEffect(appState.isShowcaseCompleted) {
        if (!appState.isShowcaseCompleted && appState.showcaseStep == 0) {
            kotlinx.coroutines.delay(1000)
            appState.updateShowcaseStep(1)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Subtle ambient glow behind the cube area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .align(Alignment.Center)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                RubikTheme.colors.glowOrange,
                                RubikTheme.colors.glowBlue,
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Dashboard (Title & Stats)
                DashboardHeader(
                    cubeState = cubeState,
                    appState = appState,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )

                // 2. Main 3D Canvas (occupies remaining height)
                InteractiveCubeCanvas(
                    appState = appState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                // 3. Playback controller (shown directly below the 3D canvas only when solution is active)
                if (appState.activeSolution != null) {
                    PlaybackController(
                        appState = appState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            )
                    )
                } else {
                    // 4. Control Panel (Shown at the bottom when solver is not active)
                    ControlPanel(
                        appState = appState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 12.dp,
                                vertical = 4.dp
                            )
                    )
                }
            }

            // 7. Global Feedback Overlays
            FeedbackOverlay(appState = appState)
        }

        // 8. Showcase Spotlight Overlay (drawn outside safearea, aligned with root)
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = 0.99f)
                        .let { modifier ->
                            if (isShowcaseActive) {
                                modifier.clickable(
                                    onClick = {
                                        appState.advanceShowcase()
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                            } else {
                                modifier
                            }
                        }
                ) {
                    drawRect(
                        color = Slate900.copy(alpha = overlayAlpha)
                    )
                    appState.targetBounds?.let { rect ->
                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = Offset(rect.left, rect.top),
                            size = Size(rect.width, rect.height),
                            cornerRadius = CornerRadius(
                                appState.targetCornerRadius.toPx(),
                                appState.targetCornerRadius.toPx()
                            ),
                            blendMode = BlendMode.Clear
                        )
                    }
                }

                // Skip Showcase/Tutorial Button (polite, solid background, top-left status bar aware)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(start = 16.dp, top = 12.dp)
                        .graphicsLayer {
                            scaleX = buttonScaleAndAlpha
                            scaleY = buttonScaleAndAlpha
                            alpha = buttonScaleAndAlpha
                        }
                        .clip(RoundedCornerShape(20.dp))
                        .background(Slate800) // Solid Slate 800
                        .border(
                            1.dp,
                            Slate600,
                            RoundedCornerShape(20.dp)
                        ) // Solid Slate 600 border
                        .let { modifier ->
                            if (isShowcaseActive) {
                                modifier.clickable {
                                    appState.updateShowcaseStep(0)
                                    appState.updateShowcaseCompleted(true)
                                    appState.updateEditorShowcaseCompleted(true)
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
}
