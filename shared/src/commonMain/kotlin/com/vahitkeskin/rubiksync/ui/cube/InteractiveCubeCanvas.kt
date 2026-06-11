package com.vahitkeskin.rubiksync.ui.cube

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import com.vahitkeskin.rubiksync.cube.CubeRenderer
import com.vahitkeskin.rubiksync.cube.GestureHandler
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.state.PreviewRubikTheme
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.rememberPreviewRubikAppState
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp

@Composable
fun InteractiveCubeCanvas(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    val cubeState = appState.cubeState
    val coroutineScope = rememberCoroutineScope()
    val isDarkTheme = RubikTheme.colors.isDark
    val animationFrameTick = rememberCubeAnimationFrameTick(cubeState)
    val layerTurnsEnabled = appState.isCubeEditable

    val gestureHandler = remember(cubeState) {
        GestureHandler(
            cubeState = cubeState,
            onCameraOrbit = { dy, dp ->
                appState.updateCameraOrbit(dy, dp)
            },
            onCameraZoom = { dz ->
                appState.updateCameraZoom(dz)
            },
            onCameraPan = { dx, dy ->
                appState.updateCameraPan(dx, dy)
            },
            onLayerRotate = { move ->
                coroutineScope.launch {
                    cubeState.executeMove(move)
                    appState.addManualMove(move)
                    appState.incrementTotalMoveCount()
                    appState.saveCurrentState()
                }
            }
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val pos = coords.positionInRoot()
                val size = coords.size
                val rect = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                appState.updateMainCubeBounds(rect)

                if (appState.showcaseStep == 5 && !appState.isShowcaseCompleted) {
                    appState.updateTargetVisuals(rect, 16.dp)
                }
            }
    ) {
        AuraBalloon(
            text = appState.strings.showcaseInteractiveCubeText,
            isVisible = appState.showcaseStep == 5 && !appState.isShowcaseCompleted,
            isBelow = true,
            onDismiss = {
                appState.advanceShowcase()
            }
        )

        // Ambient glow background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RubikTheme.colors.glowOrange,
                            RubikTheme.colors.glowBlue,
                            Color.Transparent,
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(layerTurnsEnabled) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            if (event.type == PointerEventType.Scroll) {
                                val scrollDelta = event.changes.firstOrNull()?.scrollDelta
                                if (scrollDelta != null) {
                                    appState.updateCameraZoom(scrollDelta.y * 0.15f)
                                    event.changes.forEach { it.consume() }
                                }
                                continue
                            }

                            if (changes.isEmpty()) continue

                            if (changes.size == 1) {
                                val change = changes.first()
                                if (change.pressed) {
                                    if (change.changedToDown()) {
                                        gestureHandler.handleTouchDown(
                                            change.position,
                                            width,
                                            height,
                                            appState.yaw,
                                            appState.pitch,
                                            appState.cameraDistance,
                                            appState.panX,
                                            appState.panY,
                                            layerTurnsEnabled,
                                        )
                                        change.consume()
                                    } else if (change.previousPressed) {
                                        val dragAmount = change.position - change.previousPosition
                                        gestureHandler.handleTouchDrag(
                                            change.position,
                                            dragAmount,
                                            width,
                                            height,
                                            appState.yaw,
                                            appState.pitch,
                                            appState.cameraDistance,
                                            appState.panX,
                                            appState.panY,
                                            layerTurnsEnabled,
                                        )
                                        change.consume()
                                    }
                                } else if (change.changedToUp()) {
                                    gestureHandler.handleTouchUp()
                                    change.consume()
                                }
                            } else if (changes.size >= 2) {
                                // 2-Finger Zoom and Pan
                                val p1 = changes[0]
                                val p2 = changes[1]
                                if (p1.pressed && p2.pressed) {
                                    val pos1 = p1.position
                                    val pos2 = p2.position
                                    val prev1 = p1.previousPosition
                                    val prev2 = p2.previousPosition

                                    val currentDist = (pos1 - pos2).getDistance()
                                    val prevDist = (prev1 - prev2).getDistance()

                                    // Zoom
                                    val zoomDelta = (prevDist - currentDist) * 0.015f
                                    appState.updateCameraZoom(zoomDelta)

                                    // Orbit/Rotate the entire cube (average movement of two fingers)
                                    val rotateDelta = ((pos1 - prev1) + (pos2 - prev2)) * 0.5f
                                    val dy = -rotateDelta.x * 0.007f
                                    val dp = -rotateDelta.y * 0.007f
                                    appState.updateCameraOrbit(dy, dp)

                                    p1.consume()
                                    p2.consume()
                                }
                            }
                        }
                    }
                }
        ) {
            val renderer = CubeRenderer(
                state = cubeState,
                yaw = appState.yaw,
                pitch = appState.pitch,
                cameraDistance = appState.cameraDistance,
                panX = appState.panX,
                panY = appState.panY,
                isDark = isDarkTheme
            )
            renderer.draw(this, size.width, size.height)
        }
    }
}

@Preview
@Composable
fun InteractiveCubeCanvasDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        InteractiveCubeCanvas(
            appState = rememberPreviewRubikAppState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun InteractiveCubeCanvasLightPreview() {
    PreviewRubikTheme(isDark = false) {
        InteractiveCubeCanvas(
            appState = rememberPreviewRubikAppState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}
