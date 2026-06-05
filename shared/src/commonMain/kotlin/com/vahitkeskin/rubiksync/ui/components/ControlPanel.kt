package com.vahitkeskin.rubiksync.ui.components

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.animation.*
import androidx.compose.animation.core.*

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ControlPanel(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    val cubeState = appState.cubeState
    val coroutineScope = appState.coroutineScope
    val canEditCube = appState.isCubeEditable
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    // Tooltips are now managed globally via appState.activeTooltipId

    var wasShowcaseActive by remember { mutableStateOf(false) }

    LaunchedEffect(appState.showcaseStep) {
        val isActive = appState.showcaseStep != 0 && !appState.isShowcaseCompleted
        if (isActive) {
            wasShowcaseActive = true
        }

        val targetPage = when (appState.showcaseStep) {
            6 -> 0
            in 7..9 -> 1
            in 10..11 -> 2
            0 -> {
                if (wasShowcaseActive) {
                    wasShowcaseActive = false
                    0
                } else {
                    null
                }
            }
            else -> null
        }
        if (targetPage != null && pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(
                page = targetPage,
                animationSpec = tween(
                    durationMillis = 1200, // Smoothly transition over 1.2 seconds for tutorial readability
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(RubikTheme.colors.backgroundPanel)
            .border(
                1.dp,
                RubikTheme.colors.cardBorder,
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
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
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabWidth = maxWidth / 3
                val pageOffset by remember {
                    derivedStateOf {
                        pagerState.currentPage + pagerState.currentPageOffsetFraction
                    }
                }

                // Active Tab background slider
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
                        .offset(x = tabWidth * pageOffset)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(RubikTheme.colors.tabActive, RubikTheme.colors.tabActive)
                            )
                        )
                        .border(0.5.dp, RubikTheme.colors.tabActiveBorder, RoundedCornerShape(8.dp))
                )

                // Tab Text buttons
                Row(modifier = Modifier.fillMaxSize()) {
                    listOf(
                        appState.strings.tabMoves,
                        appState.strings.tabActions,
                        appState.strings.tabAI
                    ).forEachIndexed { index, title ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) RubikTheme.colors.textPrimary else RubikTheme.colors.textSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Tab Content Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                if (appState.showcaseStep == 6 && !appState.isShowcaseCompleted) {
                                    val pos = coords.positionInRoot()
                                    val size = coords.size
                                    appState.updateTargetVisuals(
                                        Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height),
                                        12.dp
                                    )
                                }
                            }
                    ) {
                        MovesGrid(appState = appState, canEditCube = canEditCube)
                        AuraBalloon(
                            text = appState.strings.showcaseMovesText,
                            isVisible = appState.showcaseStep == 6 && !appState.isShowcaseCompleted,
                            isBelow = false,
                            onDismiss = {
                                appState.advanceShowcase()
                            }
                        )
                    }
                }

                1 -> {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // ACTIONS TAB — 3 equal-width buttons with icons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isScrambleEnabled = canEditCube && !cubeState.isAnimating
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .onGloballyPositioned { coords ->
                                        if (appState.showcaseStep == 7 && !appState.isShowcaseCompleted) {
                                            val pos = coords.positionInRoot()
                                            val size = coords.size
                                            appState.updateTargetVisuals(
                                                Rect(
                                                    pos.x,
                                                    pos.y,
                                                    pos.x + size.width,
                                                    pos.y + size.height
                                                ),
                                                12.dp
                                            )
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isScrambleEnabled) Color.Transparent
                                            else RubikTheme.colors.buttonDisabledBg
                                        )
                                        .then(
                                            if (isScrambleEnabled) Modifier.border(
                                                1.dp,
                                                RubikTheme.colors.buttonBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            else Modifier
                                        )
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = if (isScrambleEnabled) LocalIndication.current else null,
                                            onClick = {
                                                if (isScrambleEnabled) {
                                                    coroutineScope.launch {
                                                        appState.clearManualMoves()
                                                        cubeState.scramble()
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                appState.showTooltip("scramble")
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = appState.strings.scrambleButton,
                                        color = if (isScrambleEnabled) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                AuraBalloon(
                                    text = appState.strings.showcaseScrambleText,
                                    isVisible = (appState.showcaseStep == 7 && !appState.isShowcaseCompleted) || appState.activeTooltipId == "scramble",
                                    isBelow = false,
                                    onDismiss = {
                                        if (appState.activeTooltipId == "scramble") {
                                            appState.dismissTooltip("scramble")
                                        } else {
                                            appState.advanceShowcase()
                                        }
                                    }
                                )
                            }

                            val isUndoEnabled =
                                canEditCube && !cubeState.isAnimating && cubeState.moveHistory.isNotEmpty()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .onGloballyPositioned { coords ->
                                        if (appState.showcaseStep == 8 && !appState.isShowcaseCompleted) {
                                            val pos = coords.positionInRoot()
                                            val size = coords.size
                                            appState.updateTargetVisuals(
                                                Rect(
                                                    pos.x,
                                                    pos.y,
                                                    pos.x + size.width,
                                                    pos.y + size.height
                                                ),
                                                12.dp
                                            )
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isUndoEnabled) Color.Transparent
                                            else RubikTheme.colors.buttonDisabledBg
                                        )
                                        .then(
                                            if (isUndoEnabled) Modifier.border(
                                                1.dp,
                                                RubikTheme.colors.buttonBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            else Modifier
                                        )
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = if (isUndoEnabled) LocalIndication.current else null,
                                            onClick = {
                                                if (isUndoEnabled) {
                                                    coroutineScope.launch {
                                                        appState.removeLastManualMove()
                                                        cubeState.undo()
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                appState.showTooltip("undo")
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = appState.strings.undoButton,
                                        color = if (isUndoEnabled) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                AuraBalloon(
                                    text = appState.strings.showcaseUndoText,
                                    isVisible = (appState.showcaseStep == 8 && !appState.isShowcaseCompleted) || appState.activeTooltipId == "undo",
                                    isBelow = false,
                                    onDismiss = {
                                        if (appState.activeTooltipId == "undo") {
                                            appState.dismissTooltip("undo")
                                        } else {
                                            appState.advanceShowcase()
                                        }
                                    }
                                )
                            }

                            val isResetEnabled = !cubeState.isAnimating && !appState.isInitialState
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .onGloballyPositioned { coords ->
                                        if (appState.showcaseStep == 9 && !appState.isShowcaseCompleted) {
                                            val pos = coords.positionInRoot()
                                            val size = coords.size
                                            appState.updateTargetVisuals(
                                                Rect(
                                                    pos.x,
                                                    pos.y,
                                                    pos.x + size.width,
                                                    pos.y + size.height
                                                ),
                                                12.dp
                                            )
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isResetEnabled) Color.Transparent
                                            else RubikTheme.colors.buttonDisabledBg
                                        )
                                        .then(
                                            if (isResetEnabled) Modifier.border(
                                                1.dp,
                                                RubikTheme.colors.accentRed.copy(alpha = 0.35f),
                                                RoundedCornerShape(12.dp)
                                            ) else Modifier.border(
                                                1.dp,
                                                RubikTheme.colors.buttonBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                        )
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = if (isResetEnabled) LocalIndication.current else null,
                                            onClick = {
                                                if (isResetEnabled) {
                                                    coroutineScope.launch {
                                                        val startYaw = appState.yaw
                                                        val startPitch = appState.pitch
                                                        val startDist = appState.cameraDistance
                                                        val startPanX = appState.panX
                                                        val startPanY = appState.panY

                                                        cubeState.resetAnimated(durationMs = 500f) { progress ->
                                                            appState.updateYaw(startYaw + (-0.55f - startYaw) * progress)
                                                            appState.updatePitch(startPitch + (0.40f - startPitch) * progress)
                                                            appState.updateCameraDistance(startDist + (10.0f - startDist) * progress)
                                                            appState.updatePanX(startPanX + (0f - startPanX) * progress)
                                                            appState.updatePanY(startPanY + (0f - startPanY) * progress)
                                                        }
                                                        appState.clearManualMoves()
                                                        appState.updateTotalMoveCount(0)
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                appState.showTooltip("reset")
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = appState.strings.resetButton,
                                        color = if (isResetEnabled) RubikTheme.colors.accentRed else RubikTheme.colors.buttonDisabledText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                AuraBalloon(
                                    text = appState.strings.showcaseResetText,
                                    isVisible = (appState.showcaseStep == 9 && !appState.isShowcaseCompleted) || appState.activeTooltipId == "reset",
                                    isBelow = false,
                                    onDismiss = {
                                        if (appState.activeTooltipId == "reset") {
                                            appState.dismissTooltip("reset")
                                        } else {
                                            appState.advanceShowcase()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // AI & TOOLS TAB — 2 equal-width buttons with icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isDesignEnabled = canEditCube && !cubeState.isAnimating
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .onGloballyPositioned { coords ->
                                    if (appState.showcaseStep == 10 && !appState.isShowcaseCompleted) {
                                        val pos = coords.positionInRoot()
                                        val size = coords.size
                                        appState.updateTargetVisuals(
                                            Rect(
                                                pos.x,
                                                pos.y,
                                                pos.x + size.width,
                                                pos.y + size.height
                                            ),
                                            12.dp
                                        )
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isDesignEnabled) {
                                            if (RubikTheme.colors.isDark) DarkGradientBg2 else AccentBlueFaintBg
                                        } else {
                                            RubikTheme.colors.buttonDisabledBg
                                        }
                                    )
                                    .then(
                                        if (isDesignEnabled) {
                                            Modifier.border(
                                                1.dp,
                                                if (RubikTheme.colors.isDark) DarkBorderPrimary else AccentBlueSoftBg,
                                                RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = if (isDesignEnabled) LocalIndication.current else null,
                                        onClick = {
                                            if (isDesignEnabled) {
                                                appState.updateShowEditorDialog(true)
                                            }
                                        },
                                        onLongClick = {
                                            appState.showTooltip("design")
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = appState.strings.designButton,
                                    color = if (isDesignEnabled) RubikTheme.colors.accentBlue else RubikTheme.colors.buttonDisabledText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            AuraBalloon(
                                text = appState.strings.showcaseDesignText,
                                isVisible = (appState.showcaseStep == 10 && !appState.isShowcaseCompleted) || appState.activeTooltipId == "design",
                                isBelow = false,
                                onDismiss = {
                                    if (appState.activeTooltipId == "design") {
                                        appState.dismissTooltip("design")
                                    } else {
                                        appState.advanceShowcase()
                                    }
                                }
                            )
                        }

                        val isSolveEnabled =
                            canEditCube && !cubeState.isAnimating && !appState.isRecalculating && !appState.isSolved
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .onGloballyPositioned { coords ->
                                    if (appState.showcaseStep == 11 && !appState.isShowcaseCompleted) {
                                        val pos = coords.positionInRoot()
                                        val size = coords.size
                                        appState.updateTargetVisuals(
                                            Rect(
                                                pos.x,
                                                pos.y,
                                                pos.x + size.width,
                                                pos.y + size.height
                                            ),
                                            12.dp
                                        )
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSolveEnabled) {
                                            if (RubikTheme.colors.isDark) AccentGreenShadow else AccentGreenFaintBg
                                        } else {
                                            RubikTheme.colors.buttonDisabledBg
                                        }
                                    )
                                    .then(
                                        if (isSolveEnabled) {
                                            Modifier.border(
                                                1.dp,
                                                if (RubikTheme.colors.isDark) AccentGreenDeep else AccentGreenSoftBg,
                                                RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .combinedClickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = if (isSolveEnabled) LocalIndication.current else null,
                                        onClick = {
                                            if (isSolveEnabled) {
                                                appState.updateRecalculating(true)
                                                appState.updateErrorMessage(null)
                                                coroutineScope.launch(Dispatchers.Default) {
                                                    try {
                                                        val currentSnapshot = cubeState.toSnapshot()

                                                        // Check if already solved
                                                        val isSolvedNow =
                                                            cubeState.cubies.all { cubie ->
                                                                cubie.gridPos == cubie.originalPos &&
                                                                        cubie.rightBasis.x > 0.9f && cubie.upBasis.y > 0.9f && cubie.forwardBasis.z > 0.9f
                                                            }

                                                        if (isSolvedNow) {
                                                            withContext(Dispatchers.Main) {
                                                                appState.updateActiveSolution(null)
                                                                appState.updateActiveSolutionDetails(
                                                                    null
                                                                )
                                                                appState.updateSuccessMessage(
                                                                    appState.strings.cubeAlreadySolved
                                                                )
                                                                appState.updateRecalculating(false)
                                                            }
                                                            return@launch
                                                        }

                                                        // 1. Try reverse engineering (backtrack moves from move history)
                                                        val backtrackMoves =
                                                            cubeState.moveHistory.map { move ->
                                                                MoveType.values().first {
                                                                    it.axis == move.axis &&
                                                                            it.layerValue == move.layerValue &&
                                                                            it.angleSign == -move.angleSign
                                                                }
                                                            }.reversed()

                                                        val solver = RubikSolver()
                                                        val optimizedBacktrack =
                                                            compressMoves(backtrackMoves)

                                                        // 2. Run the layer-by-layer solver
                                                        val lblDetails =
                                                            solver.solveAnnotated(currentSnapshot)

                                                        // 3. Pick the shortest solution
                                                        val finalSolution: List<MoveType>
                                                        val finalDetails: List<AnnotatedMove>

                                                        if (optimizedBacktrack.isNotEmpty() && (lblDetails == null || optimizedBacktrack.size <= lblDetails.size)) {
                                                            finalSolution = optimizedBacktrack
                                                            finalDetails =
                                                                optimizedBacktrack.map { move ->
                                                                    AnnotatedMove(
                                                                        move = move,
                                                                        phaseName = "Tersine Mühendislik (Geri Alma)",
                                                                        phaseDescription = "Küpün karıştırma/manuel hamle geçmişi tersten oynatılarak en kısa yoldan çözülüyor."
                                                                    )
                                                                }
                                                        } else if (lblDetails != null) {
                                                            finalSolution =
                                                                lblDetails.map { it.move }
                                                            finalDetails = lblDetails
                                                        } else {
                                                            // Both failed (invalid/unsolvable state)
                                                            withContext(Dispatchers.Main) {
                                                                appState.updateActiveSolution(null)
                                                                appState.updateActiveSolutionDetails(
                                                                    null
                                                                )
                                                                appState.updateErrorMessage(appState.strings.solutionNotFound)
                                                                appState.updateRecalculating(false)
                                                            }
                                                            return@launch
                                                        }

                                                        withContext(Dispatchers.Main) {
                                                            if (finalSolution.isNotEmpty()) {
                                                                appState.updateActiveSolution(
                                                                    finalSolution
                                                                )
                                                                appState.updateActiveSolutionDetails(
                                                                    finalDetails
                                                                )
                                                                appState.updateCurrentSolutionStep(0)
                                                                appState.updatePlaybackRunning(false)
                                                                appState.updateErrorMessage(null)
                                                                appState.updateSuccessMessage(
                                                                    appState.strings.solutionFound.replace(
                                                                        "%s",
                                                                        finalSolution.size.toString()
                                                                    )
                                                                )
                                                            } else {
                                                                appState.updateActiveSolution(null)
                                                                appState.updateActiveSolutionDetails(
                                                                    null
                                                                )
                                                                appState.updateSuccessMessage(
                                                                    appState.strings.cubeAlreadySolved
                                                                )
                                                            }
                                                            appState.updateRecalculating(false)
                                                        }
                                                    } catch (e: Throwable) {
                                                        withContext(Dispatchers.Main) {
                                                            appState.updateErrorMessage("Hata: ${e.message ?: e.toString()}")
                                                            appState.updateRecalculating(false)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            appState.showTooltip("solve")
                                        }
                                    ),
                                contentAlignment = Alignment.Center
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
                                        color = if (isSolveEnabled) RubikTheme.colors.accentGreen else RubikTheme.colors.buttonDisabledText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            AuraBalloon(
                                text = appState.strings.showcaseSolveText,
                                isVisible = (appState.showcaseStep == 11 && !appState.isShowcaseCompleted) || appState.activeTooltipId == "solve",
                                isBelow = false,
                                onDismiss = {
                                    if (appState.activeTooltipId == "solve") {
                                        appState.dismissTooltip("solve")
                                    } else {
                                        appState.advanceShowcase()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Speed Control — always visible at bottom
        SpeedControl(appState = appState, accentColor = RubikTheme.colors.accentOrange)
    }
}
