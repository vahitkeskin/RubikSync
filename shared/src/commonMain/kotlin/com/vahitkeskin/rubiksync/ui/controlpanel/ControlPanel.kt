package com.vahitkeskin.rubiksync.ui.controlpanel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.vahitkeskin.rubiksync.utils.combinedClickableSingle
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.solver.RubikSolver
import com.vahitkeskin.rubiksync.ui.components.balloon.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.state.DesignBgDark
import com.vahitkeskin.rubiksync.ui.state.DesignBgLight
import com.vahitkeskin.rubiksync.ui.state.SolveBgDark
import com.vahitkeskin.rubiksync.ui.state.SolveBgLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    var wasShowcaseActive by remember { mutableStateOf(false) }

    LaunchedEffect(appState.showcaseStep) {
        val isActive = appState.showcaseStep != 0 && !appState.isShowcaseCompleted
        if (isActive) wasShowcaseActive = true
    }

    val targetPage = remember(appState.showcaseStep, wasShowcaseActive) {
        val current = appState.showcaseStep
        val step = kotlin.math.abs(current)
        val isTransitioning = current < 0
        
        val actualStepForPager = if (isTransitioning) {
            if (step == 11) 0 else step + 1
        } else {
            step
        }

        when (actualStepForPager) {
            6 -> 0
            in 7..9 -> 1
            in 10..11 -> 2
            0 -> if (wasShowcaseActive) 0 else null
            else -> null
        }
    }

    LaunchedEffect(targetPage) {
        if (targetPage != null) {
            try {
                pagerState.animateScrollToPage(
                    page = targetPage,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
            } catch (e: Exception) {
                // If cancelled by touch, ignore
            } finally {
                // If showcase finished and we animated back to 0, clear the flag now
                if (appState.showcaseStep == 0 && targetPage == 0) {
                    wasShowcaseActive = false
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Timer display - Animated Visibility
        AnimatedVisibility(
            visible = !appState.isSolved && !appState.isInitialState,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RubikTheme.colors.backgroundPanel)
                    .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val ms = appState.currentSolveDuration
                val minutes = (ms / 60000) % 60
                val seconds = (ms / 1000) % 60
                val millis = ms % 1000
                val timeString = "${minutes.toString().padStart(2, '0')}:${
                    seconds.toString().padStart(2, '0')
                }:${millis.toString().padStart(3, '0')}"

                Text(
                    text = timeString,
                    color = RubikTheme.colors.accentOrange,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )

                // Timer Controls
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Timer Controls
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RubikTheme.colors.backgroundPrimary)
                                .border(
                                    1.dp,
                                    RubikTheme.colors.buttonBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { appState.toggleTimer() },
                            contentAlignment = Alignment.Center
                        ) {
                            // Using text for now as icon library issue persisted
                            Text(
                                text = if (appState.solveStartTime != null) "⏸" else "▶",
                                color = RubikTheme.colors.textPrimary,
                                fontSize = 18.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RubikTheme.colors.backgroundPrimary)
                                .border(
                                    1.dp,
                                    RubikTheme.colors.buttonBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { appState.resetTimer() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⏹",
                                color = RubikTheme.colors.accentRed,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
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

            // Tab Selector
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
                    val pageOffset by remember { derivedStateOf { pagerState.currentPage + pagerState.currentPageOffsetFraction } }
                    Box(
                        modifier = Modifier.width(tabWidth).fillMaxHeight()
                            .offset(x = tabWidth * pageOffset).clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        RubikTheme.colors.tabActive,
                                        RubikTheme.colors.tabActive
                                    )
                                )
                            ).border(
                                0.5.dp,
                                RubikTheme.colors.tabActiveBorder,
                                RoundedCornerShape(8.dp)
                            )
                    )
                    Row(modifier = Modifier.fillMaxSize()) {
                        listOf(
                            appState.strings.tabMoves,
                            appState.strings.tabActions,
                            appState.strings.tabAI
                        ).forEachIndexed { index, title ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp)).clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }, contentAlignment = Alignment.Center
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

            // Horizontal Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().animateContentSize()
            ) { page ->
                when (page) {
                    0 -> {
                        Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { coords ->
                            if (appState.showcaseStep == 6 && !appState.isShowcaseCompleted) {
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                appState.updateTargetVisuals(
                                    Rect(
                                        pos.x,
                                        pos.y,
                                        pos.x + size.width,
                                        pos.y + size.height
                                    ), 12.dp
                                )
                            }
                        }) {
                            AuraBalloon(
                                text = appState.strings.showcaseMovesText,
                                isVisible = appState.showcaseStep == 6 && !appState.isShowcaseCompleted,
                                isBelow = false,
                                onDismiss = { appState.advanceShowcase() }
                            )
                            MovesGrid(appState = appState, canEditCube = canEditCube)
                        }
                    }

                    1 -> {
                        // Actions
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
                                                    if (!appState.isScrambleSoundTooltipShown) {
                                                        appState.showTooltip("sound")
                                                        appState.updateScrambleSoundTooltipShown(true)
                                                        coroutineScope.launch {
                                                            kotlinx.coroutines.delay(3000)
                                                            appState.dismissTooltip("sound")
                                                        }
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

                                                        val targetYaw = -0.55f
                                                        val targetPitch = 0.40f
                                                        val targetDist = 10.0f
                                                        val targetPanX = 0f
                                                        val targetPanY = 0f

                                                        cubeState.resetAnimated(
                                                            onProgress = { progress ->
                                                                appState.updateYaw(startYaw + (targetYaw - startYaw) * progress)
                                                                appState.updatePitch(startPitch + (targetPitch - startPitch) * progress)
                                                                appState.updateCameraDistance(startDist + (targetDist - startDist) * progress)
                                                                appState.updatePanX(startPanX + (targetPanX - startPanX) * progress)
                                                                appState.updatePanY(startPanY + (targetPanY - startPanY) * progress)
                                                            }
                                                        )
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
                            }
                        }
                    }

                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val isDesignEnabled = canEditCube && !cubeState.isAnimating
                                Box(
                                    modifier = Modifier.weight(1f).height(44.dp)
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isDesignEnabled) {
                                                    if (RubikTheme.colors.isDark) DesignBgDark else DesignBgLight
                                                } else {
                                                    RubikTheme.colors.buttonDisabledBg
                                                }
                                            )
                                            .combinedClickableSingle(
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
                                }

                                val isSolveEnabled =
                                    canEditCube && !cubeState.isAnimating && !appState.isRecalculating && !appState.isSolved
                                Box(
                                    modifier = Modifier.weight(1f).height(44.dp)
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
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSolveEnabled) {
                                                    if (RubikTheme.colors.isDark) SolveBgDark else SolveBgLight
                                                } else {
                                                    RubikTheme.colors.buttonDisabledBg
                                                }
                                            )
                                            .combinedClickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = if (isSolveEnabled) LocalIndication.current else null,
                                                onClick = {
                                                    if (isSolveEnabled) {
                                                        appState.updateRecalculating(true)
                                                        coroutineScope.launch(Dispatchers.Default) {
                                                            try {
                                                                val solver = RubikSolver()
                                                                val lblDetails = solver.solveAnnotated(
                                                                    cubeState
                                                                )
                                                                withContext(Dispatchers.Main) {
                                                                    appState.updateActiveSolution(
                                                                        lblDetails?.map { it.move })
                                                                    appState.updateActiveSolutionDetails(
                                                                        lblDetails
                                                                    )
                                                                    appState.updateRecalculating(false)
                                                                }
                                                            } catch (e: Throwable) {
                                                                withContext(Dispatchers.Main) {
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
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                        } else {
                                            Text(
                                                text = appState.strings.solveButton,
                                                color = if (isSolveEnabled) RubikTheme.colors.accentGreen else RubikTheme.colors.buttonDisabledText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            if (appState.solveSessions.isNotEmpty()) {
                                Text(
                                    text = appState.strings.bestTimesTitle,
                                    color = RubikTheme.colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                appState.solveSessions.take(3).forEach { session ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "${session.durationMillis / 1000}s",
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            appState.strings.movesCountLabel.replace(
                                                "%s",
                                                session.moveCount.toString()
                                            ),
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SpeedControl(appState = appState, accentColor = RubikTheme.colors.accentOrange)
        }
    }
}
