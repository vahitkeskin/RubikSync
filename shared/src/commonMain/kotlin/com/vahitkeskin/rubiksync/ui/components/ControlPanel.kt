package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.animation.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.LinearProgressIndicator
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
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikSolver
import com.vahitkeskin.rubiksync.cube.toSnapshot
import com.vahitkeskin.rubiksync.cube.AnnotatedMove
import com.vahitkeskin.rubiksync.cube.getMoveMathDetails
import com.vahitkeskin.rubiksync.logMoveDetail
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
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFF111827))
            .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Tab Selector — 3 tabs with animated indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0A0E18))
                .border(1.dp, Color(0x08FFFFFF), RoundedCornerShape(10.dp))
                .padding(2.dp)
        ) {
            listOf("🎮 Hareketler", "⚡ Eylemler", "🧠 Yapay Zeka").forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedTab == index) {
                                Brush.horizontalGradient(
                                    when (index) {
                                        0 -> listOf(Color(0xFF1C2536), Color(0xFF1E2A3E))
                                        1 -> listOf(Color(0xFF1C2536), Color(0xFF1E2A3E))
                                        else -> listOf(Color(0xFF1C2536), Color(0xFF1E2A3E))
                                    }
                                )
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .then(
                            if (selectedTab == index) Modifier.border(0.5.dp, Color(0x15FFFFFF), RoundedCornerShape(8.dp))
                            else Modifier
                        )
                        .clickable { selectedTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) Color.White else Color(0xFF4A5568),
                        fontSize = 11.sp,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // MOVES TAB — 12 move buttons in 2 rows × 6 columns
                MovesGrid(appState = appState)
            }
            1 -> {
                // ACTIONS TAB — 3 equal-width buttons with icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                appState.manualMoves.clear()
                                cubeState.scramble()
                            }
                        },
                        enabled = !cubeState.isAnimating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF0A0E18),
                            disabledContentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2A3548)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎲 Karıştır",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (appState.manualMoves.isNotEmpty()) {
                                    appState.manualMoves.removeAt(appState.manualMoves.size - 1)
                                }
                                cubeState.undo()
                            }
                        },
                        enabled = !cubeState.isAnimating && cubeState.moveHistory.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF0A0E18),
                            disabledContentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2A3548)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "↩️ Geri Al",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val startYaw = appState.yaw
                                val startPitch = appState.pitch
                                val startDist = appState.cameraDistance
                                val startPanX = appState.panX
                                val startPanY = appState.panY

                                cubeState.resetAnimated(durationMs = 500f) { progress ->
                                    appState.yaw = startYaw + (-0.55f - startYaw) * progress
                                    appState.pitch = startPitch + (0.40f - startPitch) * progress
                                    appState.cameraDistance = startDist + (10.0f - startDist) * progress
                                    appState.panX = startPanX + (0f - startPanX) * progress
                                    appState.panY = startPanY + (0f - startPanY) * progress
                                }
                                appState.manualMoves.clear()
                                appState.totalMoveCount = 0
                            }
                        },
                        enabled = !cubeState.isAnimating && !appState.isInitialState,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFFFF453A),
                            disabledContainerColor = Color(0xFF0A0E18),
                            disabledContentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (!cubeState.isAnimating && !appState.isInitialState) Color(0xFF3D1519) else Color(0xFF2A3548)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "🔄 Sıfırla",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            2 -> {
                // AI & TOOLS TAB — 2 equal-width buttons with icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { appState.showEditorDialog = true },
                        enabled = !cubeState.isAnimating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0F1A2E),
                            contentColor = Color(0xFF448AFF),
                            disabledContainerColor = Color(0xFF0A0E18),
                            disabledContentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        border = BorderStroke(1.dp, Color(0xFF1A2D4D)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = "🎨 Tasarla",
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
                                val currentSnapshot = cubeState.toSnapshot()
                                
                                val backtrackMoves = appState.manualMoves.map { move ->
                                    MoveType.values().first {
                                        it.axis == move.axis &&
                                        it.layerValue == move.layerValue &&
                                        it.angleSign == -move.angleSign
                                    }
                                }.reversed()
                                
                                var originalSnapshot = currentSnapshot
                                for (mv in backtrackMoves) {
                                    originalSnapshot = originalSnapshot.applyMove(mv)
                                }
                                
                                val coreSolution = solver.solve(originalSnapshot)
                                val coreDetails = solver.solveAnnotated(originalSnapshot)
                                
                                withContext(Dispatchers.Main) {
                                    if (coreSolution != null && coreDetails != null) {
                                        val combinedSolution = backtrackMoves + coreSolution
                                        val combinedDetails = backtrackMoves.map { move ->
                                            AnnotatedMove(
                                                move = move,
                                                phaseName = "Tersine Mühendislik (Geri Alma)",
                                                phaseDescription = "Kullanıcı tarafından yapılan manuel döndürme hamlesinin tersi olan '${move.label}' oynatılarak geri alınıyor."
                                            )
                                        } + coreDetails
                                        
                                        if (combinedSolution.isNotEmpty()) {
                                            appState.activeSolution = combinedSolution
                                            appState.activeSolutionDetails = combinedDetails
                                            appState.currentSolutionStep = 0
                                            appState.isPlaybackRunning = false
                                            appState.errorMessage = null
                                            appState.successMessage = "${combinedSolution.size} adımda çözüm bulundu!"
                                        } else {
                                            appState.activeSolution = null
                                            appState.activeSolutionDetails = null
                                            appState.successMessage = "Küp zaten çözülmüş! ✅"
                                        }
                                    } else {
                                        appState.activeSolution = null
                                        appState.activeSolutionDetails = null
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
                            containerColor = Color(0xFF0B1F12),
                            contentColor = Color(0xFF30D158),
                            disabledContainerColor = Color(0xFF0A0E18),
                            disabledContentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        border = BorderStroke(1.dp, Color(0xFF1A3D22)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        if (appState.isRecalculating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF30D158),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "🧠 Çöz",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Speed Control — always visible at bottom
        SpeedControl(cubeState = appState.cubeState, accentColor = Color(0xFFFF8A00))
    }
}

@Composable
private fun MovesGrid(
    appState: RubikAppState
) {
    val cubeState = appState.cubeState
    val coroutineScope = appState.coroutineScope

    @Composable
    fun RowScope.MoveBtn(move: MoveType, label: String, c1: Color, c2: Color) {
        val isLight = label.startsWith("R") || label.startsWith("L")
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (!cubeState.isAnimating) Brush.verticalGradient(listOf(c1, c2))
                    else Brush.verticalGradient(listOf(Color(0xFF1C2536), Color(0xFF1C2536)))
                )
                .border(0.5.dp, Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                .clickable(enabled = !cubeState.isAnimating) {
                    coroutineScope.launch {
                        cubeState.executeMove(move)
                        appState.manualMoves.add(move)
                        appState.totalMoveCount++
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (!cubeState.isAnimating) {
                    if (isLight) Color.Black else Color.White
                } else Color(0xFF4A5568),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MoveBtn(MoveType.U, "U", Color(0xFFFF7B00), Color(0xFFFF5F00))
            MoveBtn(MoveType.D, "D", Color(0xFFD6001C), Color(0xFFB5001A))
            MoveBtn(MoveType.R, "R", Color(0xFFCCCCCC), Color(0xFFAAAAAA))
            MoveBtn(MoveType.L, "L", Color(0xFFFFD500), Color(0xFFDDB800))
            MoveBtn(MoveType.F, "F", Color(0xFF009B48), Color(0xFF007A38))
            MoveBtn(MoveType.B, "B", Color(0xFF0046AD), Color(0xFF003890))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MoveBtn(MoveType.U_PRIME, "U'", Color(0xFFFF7B00), Color(0xFFFF5F00))
            MoveBtn(MoveType.D_PRIME, "D'", Color(0xFFD6001C), Color(0xFFB5001A))
            MoveBtn(MoveType.R_PRIME, "R'", Color(0xFFCCCCCC), Color(0xFFAAAAAA))
            MoveBtn(MoveType.L_PRIME, "L'", Color(0xFFFFD500), Color(0xFFDDB800))
            MoveBtn(MoveType.F_PRIME, "F'", Color(0xFF009B48), Color(0xFF007A38))
            MoveBtn(MoveType.B_PRIME, "B'", Color(0xFF0046AD), Color(0xFF003890))
        }
    }
}

@Composable
fun SpeedControl(
    cubeState: com.vahitkeskin.rubiksync.cube.RubikCubeState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⏱ Hız",
            color = Color(0xFF4A5568),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
            maxLines = 1
        )

        Slider(
            value = 400f - cubeState.rotationSpeedMs,
            onValueChange = { speed ->
                cubeState.rotationSpeedMs = 400f - speed
            },
            valueRange = 100f..350f,
            colors = SliderDefaults.colors(
                activeTrackColor = accentColor,
                inactiveTrackColor = Color(0xFF1C2536),
                thumbColor = Color.White
            ),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${cubeState.rotationSpeedMs.toInt()}ms",
            color = Color(0xFF6B7A8D),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End,
            maxLines = 1
        )
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
    var showDetails by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFF111827))
            .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Header Row — step counter + progress + close
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF0B1F12), Color(0xFF112218)))
                )
                .border(1.dp, Color(0x2230D158), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val currentStepDisplay = (appState.currentSolutionStep + 1).coerceAtMost(solution.size)
                Text(
                    text = "🧩 Çözüm: $currentStepDisplay / ${solution.size}",
                    color = Color(0xFF30D158),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                // Progress percentage
                val progress = if (solution.isNotEmpty()) {
                    (appState.currentSolutionStep.toFloat() / solution.size * 100).toInt()
                } else 0
                Text(
                    text = "%$progress tamamlandı",
                    color = Color(0xFF5A8A62),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x22FFFFFF))
                    .border(0.5.dp, Color(0x15FFFFFF), RoundedCornerShape(14.dp))
                    .clickable {
                        appState.activeSolution = null
                        appState.activeSolutionDetails = null
                        appState.isPlaybackRunning = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = Color(0xFF8A99AD),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Progress Bar
        val progressFraction = if (solution.isNotEmpty()) {
            appState.currentSolutionStep.toFloat() / solution.size
        } else 0f

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF30D158),
            trackColor = Color(0xFF1C2536),
        )

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
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF0A0E18))
                .border(0.5.dp, Color(0x08FFFFFF), RoundedCornerShape(10.dp))
                .padding(vertical = 5.dp, horizontal = 5.dp),
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
                    Modifier.background(Color(0xFF1C2536))
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(bgModifier)
                        .border(
                            width = if (isCurrent) 1.dp else 0.5.dp,
                            color = if (isCurrent) Color(0xFFE5FFEA) else Color(0x08FFFFFF),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = move.label,
                        color = when {
                            isCurrent -> Color.Black
                            isPast -> Color(0xFF5A8A62)
                            else -> Color(0xFF8A99AD)
                        },
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        // Media Controls — 3 compact buttons with icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restart
            Button(
                onClick = {
                    cubeState.setCustomState(appState.editorFaces)
                    appState.manualMoves.clear()
                    appState.currentSolutionStep = 0
                    appState.isPlaybackRunning = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, Color(0xFF2A3548)),
                modifier = Modifier.weight(1f).height(38.dp)
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
                    containerColor = if (appState.isPlaybackRunning) Color(0xFF2A1519) else Color(0xFF0B1F12),
                    contentColor = if (appState.isPlaybackRunning) Color(0xFFFF453A) else Color(0xFF30D158)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (appState.isPlaybackRunning) Color(0xFF3D1519) else Color(0xFF1A3D22)
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(1.3f).height(38.dp)
            ) {
                Text(
                    text = if (appState.isPlaybackRunning) "⏸ Durdur" else "▶ Oynat",
                    fontSize = 12.sp,
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
                            val nextMove = solution[appState.currentSolutionStep]
                            val activeDetail = appState.activeSolutionDetails?.getOrNull(appState.currentSolutionStep)
                            val phase = activeDetail?.phaseName ?: "Çözüm"
                            val mathDetails = getMoveMathDetails(nextMove)
                            logMoveDetail(nextMove.label, phase, mathDetails)

                            cubeState.executeMove(nextMove)
                            appState.currentSolutionStep++
                            appState.totalMoveCount++
                        }
                    }
                },
                enabled = appState.currentSolutionStep < solution.size && !cubeState.isAnimating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF0A0E18),
                    disabledContentColor = Color(0xFF4A5568)
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, Color(0xFF2A3548)),
                modifier = Modifier.weight(1f).height(38.dp)
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

        // Technical Details Collapsible Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x0AFFFFFF))
                    .clickable { showDetails = !showDetails }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📖 Teknik Çözüm Detayları",
                    color = Color(0xFF8A99AD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (showDetails) "▼" else "▲",
                    color = Color(0xFF8A99AD),
                    fontSize = 10.sp
                )
            }

            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0A0E18))
                        .border(0.5.dp, Color(0x10FFFFFF), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val detailsList = appState.activeSolutionDetails
                    val currentStep = appState.currentSolutionStep
                    if (detailsList != null && currentStep < detailsList.size) {
                        val activeDetail = detailsList[currentStep]
                        Text(
                            text = "Aşama: ${activeDetail.phaseName}",
                            color = Color(0xFF30D158),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeDetail.phaseDescription,
                            color = Color(0xFF8A99AD),
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yapılacak Hamle: ${activeDetail.move.label}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Kalan: ${detailsList.size - currentStep} hamle",
                                color = Color(0xFF4A5568),
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0x15FFFFFF)
                        )
                        Text(
                            text = "🔢 Matematiksel Formül & Rotasyon",
                            color = Color(0xFFFF8A00),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        val mathDetail = getMoveMathDetails(activeDetail.move)
                        Text(
                            text = mathDetail,
                            color = Color(0xFF8A99AD),
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F1724))
                                .border(0.5.dp, Color(0x0AFFFFFF), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                    } else {
                        Text(
                            text = "Çözüm Tamamlandı! 🎉",
                            color = Color(0xFF30D158),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Zeka küpü başarıyla çözülmüş durumuna ulaştı.",
                            color = Color(0xFF8A99AD),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Speed Control
        SpeedControl(cubeState = cubeState, accentColor = Color(0xFF30D158))
    }
}
