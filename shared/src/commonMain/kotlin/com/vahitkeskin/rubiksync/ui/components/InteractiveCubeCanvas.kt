package com.vahitkeskin.rubiksync.ui.components

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
import kotlinx.coroutines.launch
import kotlin.math.PI

@Composable
fun InteractiveCubeCanvas(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    val cubeState = appState.cubeState
    val coroutineScope = rememberCoroutineScope()

    // Gesture Handler
    val gestureHandler = remember(cubeState) {
        GestureHandler(
            state = cubeState,
            onCameraOrbit = { dy, dp ->
                appState.yaw = (appState.yaw + dy) % (2f * PI.toFloat())
                appState.pitch = (appState.pitch + dp).coerceIn(-1.4f, 1.4f)
            },
            onCameraZoom = { dz ->
                appState.cameraDistance = (appState.cameraDistance + dz).coerceIn(4f, 12f)
            },
            onCameraPan = { dx, dy ->
                appState.panX += dx
                appState.panY += dy
            },
            onLayerRotate = { move ->
                coroutineScope.launch {
                    cubeState.executeMove(move)
                    appState.totalMoveCount++
                }
            }
        )
    }

    Box(modifier = modifier) {
        // Ambient glow background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(0))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x08FF8A00),
                            Color(0x04448AFF),
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
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val changes = event.changes
                            val width = size.width.toFloat()
                            val height = size.height.toFloat()

                            if (event.type == PointerEventType.Scroll) {
                                val scrollDelta = event.changes.firstOrNull()?.scrollDelta
                                if (scrollDelta != null) {
                                    appState.cameraDistance = (appState.cameraDistance + scrollDelta.y * 0.15f).coerceIn(4f, 12f)
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
                                            appState.panY
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
                                            appState.panY
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
                                    appState.cameraDistance = (appState.cameraDistance + zoomDelta).coerceIn(4f, 12f)

                                    // Pan (average movement of two fingers)
                                    val panDelta = ((pos1 - prev1) + (pos2 - prev2)) * 0.5f
                                    // Adjust pan sensitivity relative to zoom distance
                                    appState.panX += panDelta.x * 0.005f * appState.cameraDistance
                                    appState.panY += -panDelta.y * 0.005f * appState.cameraDistance

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
                panY = appState.panY
            )
            renderer.draw(this, size.width, size.height)
        }
    }
}
