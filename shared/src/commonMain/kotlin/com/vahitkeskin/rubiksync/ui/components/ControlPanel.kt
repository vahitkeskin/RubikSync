package com.vahitkeskin.rubiksync.ui.components

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.animation.*

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
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
import androidx.compose.foundation.layout.fillMaxSize
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
import com.vahitkeskin.rubiksync.solver.RubikSolver
import com.vahitkeskin.rubiksync.solver.toSnapshot
import com.vahitkeskin.rubiksync.solver.AnnotatedMove
import com.vahitkeskin.rubiksync.solver.compressMoves
import com.vahitkeskin.rubiksync.cube.getMoveMathDetails
import com.vahitkeskin.rubiksync.logMoveDetail
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
fun ControlPanel(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    val cubeState = appState.cubeState
    val coroutineScope = appState.coroutineScope
    val canEditCube = appState.isCubeEditable
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(appState.showcaseStep) {
        if (appState.showcaseStep == 5) {
            selectedTab = 0
        } else if (appState.showcaseStep == 6) {
            selectedTab = 1
        } else if (appState.showcaseStep == 7) {
            selectedTab = 2
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(RubikTheme.colors.backgroundPanel)
            .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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
                .background(RubikTheme.colors.backgroundPrimary)
                .border(1.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(10.dp))
                .padding(2.dp)
        ) {
            listOf(appState.strings.tabMoves, appState.strings.tabActions, appState.strings.tabAI).forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selectedTab == index) {
                                Brush.horizontalGradient(
                                    listOf(RubikTheme.colors.tabActive, RubikTheme.colors.tabActive)
                                )
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .then(
                            if (selectedTab == index) Modifier.border(0.5.dp, RubikTheme.colors.tabActiveBorder, RoundedCornerShape(8.dp))
                            else Modifier
                        )
                        .clickable { selectedTab = index },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selectedTab == index) RubikTheme.colors.textPrimary else RubikTheme.colors.textSecondary,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            if (appState.showcaseStep == 5 && !appState.isShowcaseCompleted) {
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                appState.targetBounds = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                                appState.targetCornerRadius = 12.dp
                            }
                        }
                ) {
                    MovesGrid(appState = appState, canEditCube = canEditCube)
                    AuraBalloon(
                        text = appState.strings.showcaseMovesText,
                        isVisible = appState.showcaseStep == 5 && !appState.isShowcaseCompleted,
                        isBelow = false,
                        onDismiss = {
                            appState.advanceShowcase()
                        }
                    )
                }
            }
            1 -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            if (appState.showcaseStep == 6 && !appState.isShowcaseCompleted) {
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                appState.targetBounds = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                                appState.targetCornerRadius = 16.dp
                            }
                        }
                ) {
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
                            enabled = canEditCube && !cubeState.isAnimating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = RubikTheme.colors.textPrimary,
                                disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                                disabledContentColor = RubikTheme.colors.buttonDisabledText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                            border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = appState.strings.scrambleButton,
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
                            enabled = canEditCube && !cubeState.isAnimating && cubeState.moveHistory.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = RubikTheme.colors.textPrimary,
                                disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                                disabledContentColor = RubikTheme.colors.buttonDisabledText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                            border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = appState.strings.undoButton,
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
                                contentColor = RubikTheme.colors.accentRed,
                                disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                                disabledContentColor = RubikTheme.colors.buttonDisabledText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (!cubeState.isAnimating && !appState.isInitialState) RubikTheme.colors.accentRed.copy(alpha = 0.35f) else RubikTheme.colors.buttonBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = appState.strings.resetButton,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    AuraBalloon(
                        text = appState.strings.showcaseActionsText,
                        isVisible = appState.showcaseStep == 6 && !appState.isShowcaseCompleted,
                        isBelow = false,
                        onDismiss = {
                            appState.advanceShowcase()
                        }
                    )
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
                        enabled = canEditCube && !cubeState.isAnimating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (RubikTheme.colors.isDark) DarkGradientBg2 else AccentBlueFaintBg,
                            contentColor = RubikTheme.colors.accentBlue,
                            disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                            disabledContentColor = RubikTheme.colors.buttonDisabledText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                        border = BorderStroke(1.dp, if (RubikTheme.colors.isDark) DarkBorderPrimary else AccentBlueSoftBg),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(
                            text = appState.strings.designButton,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .onGloballyPositioned { coords ->
                                if (appState.showcaseStep == 7 && !appState.isShowcaseCompleted) {
                                    val pos = coords.positionInRoot()
                                    val size = coords.size
                                    appState.targetBounds = Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                                    appState.targetCornerRadius = 12.dp
                                }
                            }
                    ) {
                        Button(
                             onClick = {
                            appState.isRecalculating = true
                            appState.errorMessage = null
                            coroutineScope.launch(Dispatchers.Default) {
                                try {
                                    val currentSnapshot = cubeState.toSnapshot()
                                    
                                    // Check if already solved
                                    val isSolvedNow = cubeState.cubies.all { cubie ->
                                        cubie.gridPos == cubie.originalPos &&
                                        cubie.rightBasis.x > 0.9f && cubie.upBasis.y > 0.9f && cubie.forwardBasis.z > 0.9f
                                    }
                                    
                                    if (isSolvedNow) {
                                        withContext(Dispatchers.Main) {
                                            appState.activeSolution = null
                                            appState.activeSolutionDetails = null
                                            appState.successMessage = appState.strings.cubeAlreadySolved
                                            appState.isRecalculating = false
                                        }
                                        return@launch
                                    }
                                    
                                    // 1. Try reverse engineering (backtrack moves from move history)
                                    val backtrackMoves = cubeState.moveHistory.map { move ->
                                        MoveType.values().first {
                                            it.axis == move.axis &&
                                            it.layerValue == move.layerValue &&
                                            it.angleSign == -move.angleSign
                                        }
                                    }.reversed()
                                    
                                    val solver = RubikSolver()
                                    val optimizedBacktrack = compressMoves(backtrackMoves)
                                    
                                    // 2. Run the layer-by-layer solver
                                    val lblDetails = solver.solveAnnotated(currentSnapshot)
                                    
                                    // 3. Pick the shortest solution
                                    val finalSolution: List<MoveType>
                                    val finalDetails: List<AnnotatedMove>
                                    
                                    if (optimizedBacktrack.isNotEmpty() && (lblDetails == null || optimizedBacktrack.size <= lblDetails.size)) {
                                        finalSolution = optimizedBacktrack
                                        finalDetails = optimizedBacktrack.map { move ->
                                            AnnotatedMove(
                                                move = move,
                                                phaseName = "Tersine Mühendislik (Geri Alma)",
                                                phaseDescription = "Küpün karıştırma/manuel hamle geçmişi tersten oynatılarak en kısa yoldan çözülüyor."
                                            )
                                        }
                                    } else if (lblDetails != null) {
                                        finalSolution = lblDetails.map { it.move }
                                        finalDetails = lblDetails
                                    } else {
                                        // Both failed (invalid/unsolvable state)
                                        withContext(Dispatchers.Main) {
                                            appState.activeSolution = null
                                            appState.activeSolutionDetails = null
                                            appState.errorMessage = appState.strings.solutionNotFound
                                            appState.isRecalculating = false
                                        }
                                        return@launch
                                    }
                                    
                                    withContext(Dispatchers.Main) {
                                        if (finalSolution.isNotEmpty()) {
                                            appState.activeSolution = finalSolution
                                            appState.activeSolutionDetails = finalDetails
                                            appState.currentSolutionStep = 0
                                            appState.isPlaybackRunning = false
                                            appState.errorMessage = null
                                            appState.successMessage = appState.strings.solutionFound.replace("%s", finalSolution.size.toString())
                                        } else {
                                            appState.activeSolution = null
                                            appState.activeSolutionDetails = null
                                            appState.successMessage = appState.strings.cubeAlreadySolved
                                        }
                                        appState.isRecalculating = false
                                    }
                                } catch (e: Throwable) {
                                    withContext(Dispatchers.Main) {
                                        appState.errorMessage = "Hata: ${e.message ?: e.toString()}"
                                        appState.isRecalculating = false
                                    }
                                }
                            }
                        },
                            enabled = canEditCube && !cubeState.isAnimating && !appState.isRecalculating,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (RubikTheme.colors.isDark) AccentGreenShadow else AccentGreenFaintBg,
                                contentColor = RubikTheme.colors.accentGreen,
                                disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                                disabledContentColor = RubikTheme.colors.buttonDisabledText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            border = BorderStroke(1.dp, if (RubikTheme.colors.isDark) AccentGreenDeep else AccentGreenSoftBg),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            if (appState.isRecalculating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = RubikTheme.colors.accentGreen,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = appState.strings.solveButton,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        AuraBalloon(
                            text = appState.strings.showcaseSolveText,
                            isVisible = appState.showcaseStep == 7 && !appState.isShowcaseCompleted,
                            isBelow = false,
                            onDismiss = {
                                appState.advanceShowcase()
                            }
                        )
                    }
                }
            }
        }

        // Speed Control — always visible at bottom
        SpeedControl(appState = appState, accentColor = RubikTheme.colors.accentOrange)
    }
}

@Composable
private fun MovesGrid(
    appState: RubikAppState,
    canEditCube: Boolean,
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
                    if (canEditCube && !cubeState.isAnimating) Brush.verticalGradient(listOf(c1, c2))
                    else Brush.verticalGradient(listOf(RubikTheme.colors.buttonDisabledBg, RubikTheme.colors.buttonDisabledBg))
                )
                .border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                .clickable(enabled = canEditCube && !cubeState.isAnimating) {
                    coroutineScope.launch {
                        cubeState.executeMove(move)
                        appState.manualMoves.add(move)
                        appState.totalMoveCount++
                        appState.saveCurrentState()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (canEditCube && !cubeState.isAnimating) {
                    if (isLight) Color.Black else Color.White
                } else RubikTheme.colors.buttonDisabledText,
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
            MoveBtn(MoveType.U, "U", AccentOrangeMedium, CubeOrange)
            MoveBtn(MoveType.D, "D", CubeRed, AccentRedDark)
            MoveBtn(MoveType.R, "R", LightBorderFaint, LightTextMuted)
            MoveBtn(MoveType.L, "L", CubeYellow, AccentYellowDark)
            MoveBtn(MoveType.F, "F", CubeGreen, CubeGreenDark)
            MoveBtn(MoveType.B, "B", CubeBlue, AccentBlueDeep)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MoveBtn(MoveType.U_PRIME, "U'", AccentOrangeMedium, CubeOrange)
            MoveBtn(MoveType.D_PRIME, "D'", CubeRed, AccentRedDark)
            MoveBtn(MoveType.R_PRIME, "R'", LightBorderFaint, LightTextMuted)
            MoveBtn(MoveType.L_PRIME, "L'", CubeYellow, AccentYellowDark)
            MoveBtn(MoveType.F_PRIME, "F'", CubeGreen, CubeGreenDark)
            MoveBtn(MoveType.B_PRIME, "B'", CubeBlue, AccentBlueDeep)
        }
    }
}

@Composable
fun SpeedControl(
    appState: RubikAppState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val cubeState = appState.cubeState
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = appState.strings.speedLabel,
            color = RubikTheme.colors.textSecondary,
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
                inactiveTrackColor = RubikTheme.colors.speedTrack,
                thumbColor = if (RubikTheme.colors.isDark) Color.White else RubikTheme.colors.accentOrange
            ),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "${cubeState.rotationSpeedMs.toInt()}ms",
            color = RubikTheme.colors.textSecondary,
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
    val canEditCube = appState.isCubeEditable
    var showDetails by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(RubikTheme.colors.backgroundPanel)
            .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
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
                    Brush.horizontalGradient(
                        if (RubikTheme.colors.isDark) {
                            listOf(AccentGreenShadow, AccentGreenVeryDark)
                        } else {
                            listOf(AccentGreenFaintBg, AccentGreenSoftBg)
                        }
                    )
                )
                .border(1.dp, if (RubikTheme.colors.isDark) AccentGreenAlpha13 else AccentGreenAlpha20, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val currentStepDisplay = (appState.currentSolutionStep + 1).coerceAtMost(solution.size)
                Text(
                    text = "${appState.strings.solutionTitle}$currentStepDisplay / ${solution.size}",
                    color = RubikTheme.colors.accentGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                // Progress percentage
                val progress = if (solution.isNotEmpty()) {
                    (appState.currentSolutionStep.toFloat() / solution.size * 100).toInt()
                } else 0
                Text(
                    text = "$progress%${appState.strings.progressLabel}",
                    color = if (RubikTheme.colors.isDark) AccentGreenSage else AccentGreenForest,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (RubikTheme.colors.isDark) WhiteAlpha13 else BlackAlpha05)
                    .border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                    .clickable {
                        appState.activeSolution = null
                        appState.activeSolutionDetails = null
                        appState.isPlaybackRunning = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = RubikTheme.colors.textSecondary,
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
            color = RubikTheme.colors.accentGreen,
            trackColor = RubikTheme.colors.progressTrack,
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
                .background(RubikTheme.colors.backgroundPrimary)
                .border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(10.dp))
                .padding(vertical = 5.dp, horizontal = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(solution.size) { index ->
                val move = solution[index]
                val isCurrent = index == appState.currentSolutionStep
                val isPast = index < appState.currentSolutionStep

                val bgModifier = if (isCurrent) {
                    Modifier.background(Brush.horizontalGradient(listOf(AccentGreen, AccentGreenVibrant)))
                } else if (isPast) {
                    Modifier.background(if (RubikTheme.colors.isDark) AccentGreenNavy else AccentGreenFaintBg)
                } else {
                    Modifier.background(RubikTheme.colors.backgroundTertiary)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .then(bgModifier)
                        .border(
                            width = if (isCurrent) 1.dp else 0.5.dp,
                            color = if (isCurrent) AccentGreenMintBg else RubikTheme.colors.borderFaint,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = move.label,
                        color = when {
                            isCurrent -> Color.White
                            isPast -> if (RubikTheme.colors.isDark) AccentGreenSage else AccentGreenForest
                            else -> RubikTheme.colors.textSecondary
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
                    coroutineScope.launch {
                        appState.manualMoves.clear()
                        cubeState.setCustomStateAnimated(appState.editorFaces)
                        appState.currentSolutionStep = 0
                        appState.isPlaybackRunning = false
                    }
                },
                enabled = canEditCube && !cubeState.isAnimating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = RubikTheme.colors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text(
                    text = appState.strings.backToStart,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play / Pause — wider, accent color
            Button(
                onClick = { appState.isPlaybackRunning = !appState.isPlaybackRunning },
                enabled = canEditCube,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (appState.isPlaybackRunning) {
                        if (RubikTheme.colors.isDark) SelectionMediumOrange else AccentRedPinkBg
                    } else {
                        if (RubikTheme.colors.isDark) AccentGreenShadow else AccentGreenFaintBg
                    },
                    contentColor = if (appState.isPlaybackRunning) RubikTheme.colors.accentRed else RubikTheme.colors.accentGreen
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (appState.isPlaybackRunning) {
                        if (RubikTheme.colors.isDark) SelectionMediumMagenta else AccentRedSoftBg
                    } else {
                        if (RubikTheme.colors.isDark) AccentGreenDeep else AccentGreenSoftBg
                    }
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier.weight(1.3f).height(38.dp)
            ) {
                Text(
                    text = if (appState.isPlaybackRunning) appState.strings.playbackPause else appState.strings.playbackPlay,
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
                enabled = canEditCube && appState.currentSolutionStep < solution.size && !cubeState.isAnimating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = RubikTheme.colors.textPrimary,
                    disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                    disabledContentColor = RubikTheme.colors.buttonDisabledText
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Text(
                    text = appState.strings.nextStep,
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
                    .background(RubikTheme.colors.backgroundTertiary)
                    .clickable { showDetails = !showDetails }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = appState.strings.technicalDetails,
                    color = RubikTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (showDetails) "▼" else "▲",
                    color = RubikTheme.colors.textSecondary,
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
                        .background(RubikTheme.colors.backgroundPrimary)
                        .border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val detailsList = appState.activeSolutionDetails
                    val currentStep = appState.currentSolutionStep
                    if (detailsList != null && currentStep < detailsList.size) {
                        val activeDetail = detailsList[currentStep]
                        Text(
                            text = "${appState.strings.phaseLabel}${activeDetail.phaseName}",
                            color = RubikTheme.colors.accentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = activeDetail.phaseDescription,
                            color = RubikTheme.colors.textSecondary,
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
                                text = "${appState.strings.nextMoveLabel}${activeDetail.move.label}",
                                color = RubikTheme.colors.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = appState.strings.remainingLabel.replace("%s", (detailsList.size - currentStep).toString()),
                                color = RubikTheme.colors.textMuted,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        androidx.compose.material3.HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = RubikTheme.colors.borderFaint
                        )
                        Text(
                            text = appState.strings.mathFormulaTitle,
                            color = RubikTheme.colors.accentOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        val mathDetail = getMoveMathDetails(activeDetail.move)
                        Text(
                            text = mathDetail,
                            color = RubikTheme.colors.textSecondary,
                            fontSize = 9.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            lineHeight = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(RubikTheme.colors.backgroundTertiary)
                                .border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        )
                    } else {
                        Text(
                            text = appState.strings.solutionComplete,
                            color = RubikTheme.colors.accentGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = appState.strings.solvedSuccessDesc,
                            color = RubikTheme.colors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Speed Control
        SpeedControl(appState = appState, accentColor = RubikTheme.colors.accentGreen)
    }
}
