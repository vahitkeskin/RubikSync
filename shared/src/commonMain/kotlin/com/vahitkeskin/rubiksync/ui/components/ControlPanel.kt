package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.RubikSolver
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ControlPanel(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    val cubeState = appState.cubeState
    val coroutineScope = appState.coroutineScope
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Tab Selector — compact pill style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF161D2A))
                .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(10.dp))
                .padding(2.dp)
        ) {
            listOf("Eylemler", "Yapay Zeka").forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == index) Color(0xFF252E3E) else Color.Transparent)
                        .clickable { selectedTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) Color.White else Color(0xFF6B7A8D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Tab Content
        if (selectedTab == 0) {
            // ACTIONS TAB — 3 equal-width buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch { cubeState.scramble() }
                    },
                    enabled = !cubeState.isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8A00),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "Karıştır",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch { cubeState.undo() }
                    },
                    enabled = !cubeState.isAnimating && cubeState.moveHistory.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2633),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "Geri Al",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = {
                        cubeState.reset()
                        appState.yaw = -0.55f
                        appState.pitch = 0.40f
                        appState.cameraDistance = 10.0f
                        appState.panX = 0f
                        appState.panY = 0f
                    },
                    enabled = !cubeState.isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A1519),
                        contentColor = Color(0xFFFF453A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "Sıfırla",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            // AI & TOOLS TAB — 2 equal-width buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { appState.showEditorDialog = true },
                    enabled = !cubeState.isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF172238),
                        contentColor = Color(0xFF448AFF)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "Tasarla",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                                        appState.errorMessage = "Küp zaten çözülmüş!"
                                    } else {
                                        appState.errorMessage = "Çözüm bulunamadı!"
                                    }
                                    appState.isRecalculating = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    appState.errorMessage = "Hata: ${e.message}"
                                    appState.isRecalculating = false
                                }
                            }
                        }
                    },
                    enabled = !cubeState.isAnimating && !appState.isRecalculating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF132619),
                        contentColor = Color(0xFF30D158)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    if (appState.isRecalculating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF30D158),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Çöz",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Speed Control — single compact row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hız",
                color = Color(0xFF6B7A8D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )

            Slider(
                value = 400f - cubeState.rotationSpeedMs,
                onValueChange = { speed ->
                    cubeState.rotationSpeedMs = 400f - speed
                },
                valueRange = 100f..350f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFFFF8A00),
                    inactiveTrackColor = Color(0xFF1E2633),
                    thumbColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${cubeState.rotationSpeedMs.toInt()}ms",
                color = Color(0xFF8A99AD),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End
            )
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header Row — step counter + close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF132619))
                .border(1.dp, Color(0x3330D158), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentStepDisplay = (appState.currentSolutionStep + 1).coerceAtMost(solution.size)
            Text(
                text = "Çözüm: $currentStepDisplay / ${solution.size}",
                color = Color(0xFF30D158),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x22FFFFFF))
                    .clickable {
                        appState.activeSolution = null
                        appState.isPlaybackRunning = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = Color(0xFF8A99AD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Horizontally Scrollable Steps List
        val lazyListState = rememberLazyListState()
        LaunchedEffect(appState.currentSolutionStep) {
            val step = appState.currentSolutionStep
            if (step >= 0 && step < solution.size) {
                val layoutInfo = lazyListState.layoutInfo
                val viewportWidth = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                val itemSize = layoutInfo.visibleItemsInfo.find { it.index == step }?.size
                    ?: layoutInfo.visibleItemsInfo.firstOrNull()?.size
                    ?: 80
                val offset = -(viewportWidth / 2) + (itemSize / 2)
                lazyListState.animateScrollToItem(step, offset)
            }
        }

        LazyRow(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF161D2A))
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(solution.size) { index ->
                val move = solution[index]
                val isCurrent = index == appState.currentSolutionStep
                val isPast = index < appState.currentSolutionStep

                val bgModifier = if (isCurrent) {
                    Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF30D158), Color(0xFF34C759))))
                } else if (isPast) {
                    Modifier.background(Color(0xFF1A2E1F))
                } else {
                    Modifier.background(Color(0xFF1E2633))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .then(bgModifier)
                        .border(
                            width = if (isCurrent) 1.dp else 0.dp,
                            color = if (isCurrent) Color(0xFFE5FFEA) else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = move.label,
                        color = when {
                            isCurrent -> Color.Black
                            isPast -> Color(0xFF5A8A62)
                            else -> Color(0xFFAABBCC)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Media Controls — 3 compact buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restart
            Button(
                onClick = {
                    cubeState.setCustomState(appState.editorFaces)
                    appState.currentSolutionStep = 0
                    appState.isPlaybackRunning = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E2633),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Text(
                    text = "⏮ Başa",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play / Pause — wider, accent color
            Button(
                onClick = { appState.isPlaybackRunning = !appState.isPlaybackRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (appState.isPlaybackRunning) Color(0xFF2A1519) else Color(0xFF132619),
                    contentColor = if (appState.isPlaybackRunning) Color(0xFFFF453A) else Color(0xFF30D158)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (appState.isPlaybackRunning) Color(0x33FF453A) else Color(0x3330D158)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(1.3f).height(36.dp)
            ) {
                Text(
                    text = if (appState.isPlaybackRunning) "⏸ Durdur" else "▶ Oynat",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Step Forward
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E2633),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Text(
                    text = "İleri ⏭",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Speed Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hız",
                color = Color(0xFF6B7A8D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(28.dp)
            )

            Slider(
                value = 400f - cubeState.rotationSpeedMs,
                onValueChange = { speed ->
                    cubeState.rotationSpeedMs = 400f - speed
                },
                valueRange = 100f..350f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color(0xFF30D158),
                    inactiveTrackColor = Color(0xFF1E2633),
                    thumbColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "${cubeState.rotationSpeedMs.toInt()}ms",
                color = Color(0xFF8A99AD),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.End
            )
        }
    }
}
