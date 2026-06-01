package com.vahitkeskin.rubiksync

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.*
import kotlinx.coroutines.launch
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val cubeState = remember { RubikCubeState() }

    // Camera State
    var yaw by remember { mutableStateOf(-0.55f) }       // Initial yaw - viewing top-left-front
    var pitch by remember { mutableStateOf(0.40f) }      // Initial pitch
    var cameraDistance by remember { mutableStateOf(6.5f) }
    var panX by remember { mutableStateOf(0f) }
    var panY by remember { mutableStateOf(0f) }

    // Gesture Handler
    val gestureHandler = remember(cubeState) {
        GestureHandler(
            state = cubeState,
            onCameraOrbit = { dy, dp ->
                yaw = (yaw + dy) % (2f * PI.toFloat())
                pitch = (pitch + dp).coerceIn(-1.4f, 1.4f)
            },
            onCameraZoom = { dz ->
                cameraDistance = (cameraDistance + dz).coerceIn(4f, 12f)
            },
            onCameraPan = { dx, dy ->
                panX += dx
                panY += dy
            },
            onLayerRotate = { move ->
                coroutineScope.launch {
                    cubeState.executeMove(move)
                }
            }
        )
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF8A00),
            background = Color(0xFF0F1520),
            surface = Color(0xFF1E2633)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A2639), // Dark steel blue center
                            Color(0xFF0A0D14)  // Absolute deep space outer edges
                        )
                    )
                )
                .safeContentPadding()
        ) {
            // Main 3D Canvas
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
                                        cameraDistance = (cameraDistance + scrollDelta.y * 0.15f).coerceIn(4f, 12f)
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
                                                yaw,
                                                pitch,
                                                cameraDistance,
                                                panX,
                                                panY
                                            )
                                            change.consume()
                                        } else if (change.previousPressed) {
                                            val dragAmount = change.position - change.previousPosition
                                            gestureHandler.handleTouchDrag(
                                                change.position,
                                                dragAmount,
                                                width,
                                                height,
                                                yaw,
                                                pitch,
                                                cameraDistance,
                                                panX,
                                                panY
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
                                        cameraDistance = (cameraDistance + zoomDelta).coerceIn(4f, 12f)

                                        // Pan (average movement of two fingers)
                                        val panDelta = ((pos1 - prev1) + (pos2 - prev2)) * 0.5f
                                        // Adjust pan sensitivity relative to zoom distance
                                        panX += panDelta.x * 0.005f * cameraDistance
                                        panY += -panDelta.y * 0.005f * cameraDistance

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
                    yaw = yaw,
                    pitch = pitch,
                    cameraDistance = cameraDistance,
                    panX = panX,
                    panY = panY
                )
                renderer.draw(this, size.width, size.height)
            }

            // Top Dashboard (Title & History)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RUBIK SYNC",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3D Interactive Simulation",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                // Move History Card
                if (cubeState.moveHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Geçmiş: " + cubeState.moveHistory.takeLast(8).joinToString(" ") { it.label },
                            color = Color(0xFFFFBD59),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Dashboard (Controls Card)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glassmorphic control panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x1A1E2633))
                        .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        cubeState.scramble()
                                    }
                                },
                                enabled = !cubeState.isAnimating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF8A00),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Karıştır", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        cubeState.undo()
                                    }
                                },
                                enabled = !cubeState.isAnimating && cubeState.moveHistory.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x33FFFFFF),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Geri Al", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    cubeState.reset()
                                    yaw = -0.55f
                                    pitch = 0.40f
                                    cameraDistance = 6.5f
                                    panX = 0f
                                    panY = 0f
                                },
                                enabled = !cubeState.isAnimating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x22FF3B30),
                                    contentColor = Color(0xFFFF3B30)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Sıfırla", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Rotation Speed Control Slider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hız:",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.width(42.dp)
                            )

                            Slider(
                                value = 400f - cubeState.rotationSpeedMs, // Inverse value: higher ms is slower speed
                                onValueChange = { speed ->
                                    cubeState.rotationSpeedMs = 400f - speed
                                },
                                valueRange = 100f..350f, // Map to speedMs: 50ms (fast) to 300ms (slow)
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFFFF8A00),
                                    inactiveTrackColor = Color(0x22FFFFFF),
                                    thumbColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${cubeState.rotationSpeedMs.toInt()} ms",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(54.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}