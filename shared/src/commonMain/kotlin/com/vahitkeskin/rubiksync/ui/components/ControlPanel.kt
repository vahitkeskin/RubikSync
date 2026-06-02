package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.RubikSolver
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ControlPanel(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    val cubeState = appState.cubeState
    val coroutineScope = appState.coroutineScope

    Box(
        modifier = modifier
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
                        appState.yaw = -0.55f
                        appState.pitch = 0.40f
                        appState.cameraDistance = 6.5f
                        appState.panX = 0f
                        appState.panY = 0f
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

            // Tasarla & Çözüm Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        appState.showEditorDialog = true
                    },
                    enabled = !cubeState.isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x33448AFF),
                        contentColor = Color(0xFF448AFF)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    Text("Tasarla", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        appState.isRecalculating = true
                        appState.errorMessage = null
                        coroutineScope.launch(Dispatchers.Default) {
                            try {
                                val solver = RubikSolver()
                                val solution = solver.solve(cubeState)
                                withContext(Dispatchers.Main) {
                                    if (solution != null && solution.isNotEmpty()) {
                                        appState.activeSolution = solution
                                        appState.currentSolutionStep = 0
                                        appState.isPlaybackRunning = false
                                        appState.errorMessage = null
                                    } else if (solution != null && solution.isEmpty()) {
                                        appState.errorMessage = "Küp zaten çözülmüş durumda!"
                                    } else {
                                        appState.errorMessage = "Çözüm bulunamadı! Küp yapısı hatalı olabilir."
                                    }
                                    appState.isRecalculating = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    appState.errorMessage = "Çözücü hatası: ${e.message}"
                                    appState.isRecalculating = false
                                }
                            }
                        }
                    },
                    enabled = !cubeState.isAnimating && !appState.isRecalculating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x334CAF50),
                        contentColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                ) {
                    if (appState.isRecalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFF4CAF50),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Çözücü", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
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
                    value = 400f - cubeState.rotationSpeedMs,
                    onValueChange = { speed ->
                        cubeState.rotationSpeedMs = 400f - speed
                    },
                    valueRange = 100f..350f,
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

@Composable
fun PlaybackController(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    val solution = appState.activeSolution ?: return
    val cubeState = appState.cubeState
    val coroutineScope = appState.coroutineScope

    Box(
        modifier = modifier
            .fillMaxWidth(0.90f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xE61E2633))
            .border(1.5.dp, Color(0x334CAF50), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Çözüm Adımları (${solution.size} Hamle)",
                    color = Color(0xFF4CAF50),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        appState.activeSolution = null
                        appState.isPlaybackRunning = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        text = "✕",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                solution.forEachIndexed { index, move ->
                    val isCurrent = index == appState.currentSolutionStep
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurrent) Color(0xFF4CAF50) else Color(0x11FFFFFF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = move.label,
                            color = if (isCurrent) Color.White else Color.LightGray,
                            fontSize = 14.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        cubeState.setCustomState(appState.editorFaces)
                        appState.currentSolutionStep = 0
                        appState.isPlaybackRunning = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Başa Dön")
                }

                Button(
                    onClick = { appState.isPlaybackRunning = !appState.isPlaybackRunning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (appState.isPlaybackRunning) Color(0xFFFF3B30) else Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (appState.isPlaybackRunning) "Duraklat" else "Oynat")
                }

                Button(
                    onClick = {
                        if (appState.currentSolutionStep < solution.size) {
                            coroutineScope.launch {
                                cubeState.executeMove(solution[appState.currentSolutionStep])
                                appState.currentSolutionStep++
                            }
                        }
                    },
                    enabled = appState.currentSolutionStep < solution.size && !cubeState.isAnimating,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("İleri >")
                }
            }
        }
    }
}
