package com.vahitkeskin.rubiksync.ui.controlpanel

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.solver.RubikSolver
import com.vahitkeskin.rubiksync.solver.toSnapshot
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
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
        val targetPage = when (appState.showcaseStep) {
            6 -> 0
            in 7..9 -> 1
            in 10..11 -> 2
            0 -> if (wasShowcaseActive) { wasShowcaseActive = false; 0 } else null
            else -> null
        }
        if (targetPage != null && pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(page = targetPage, animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing))
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
        // Timer display - Animated Visibility
        AnimatedVisibility(
            visible = !appState.isSolved && !appState.isInitialState,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ms = appState.currentSolveDuration
                val minutes = (ms / 60000) % 60
                val seconds = (ms / 1000) % 60
                val millis = ms % 1000
                val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}:${millis.toString().padStart(3, '0')}"
                
                Text(
                    text = timeString,
                    color = RubikTheme.colors.accentOrange,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Timer Controls
                Text(
                    text = if (appState.solveStartTime != null) "⏸" else "▶",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RubikTheme.colors.backgroundPrimary)
                        .padding(8.dp)
                        .clickable { appState.toggleTimer() },
                    color = RubikTheme.colors.textPrimary
                )
                Text(
                    text = "⏹",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RubikTheme.colors.backgroundPrimary)
                        .padding(8.dp)
                        .clickable { appState.resetTimer() },
                    color = RubikTheme.colors.accentRed
                )
            }
        }

        // Timer display - Animated Visibility
        AnimatedVisibility(
            visible = !appState.isSolved && !appState.isInitialState,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ms = appState.currentSolveDuration
                val minutes = (ms / 60000) % 60
                val seconds = (ms / 1000) % 60
                val millis = ms % 1000
                val timeString = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}:${millis.toString().padStart(3, '0')}"
                
                Text(
                    text = timeString,
                    color = RubikTheme.colors.accentOrange,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Timer Controls
                Text(
                    text = if (appState.solveStartTime != null) "⏸" else "▶",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RubikTheme.colors.backgroundPrimary)
                        .padding(8.dp)
                        .clickable { appState.toggleTimer() },
                    color = RubikTheme.colors.textPrimary
                )
                Text(
                    text = "⏹",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RubikTheme.colors.backgroundPrimary)
                        .padding(8.dp)
                        .clickable { appState.resetTimer() },
                    color = RubikTheme.colors.accentRed
                )
            }
        }

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
                Box(modifier = Modifier.width(tabWidth).fillMaxHeight().offset(x = tabWidth * pageOffset).clip(RoundedCornerShape(8.dp)).background(Brush.horizontalGradient(listOf(RubikTheme.colors.tabActive, RubikTheme.colors.tabActive))).border(0.5.dp, RubikTheme.colors.tabActiveBorder, RoundedCornerShape(8.dp)))
                Row(modifier = Modifier.fillMaxSize()) {
                    listOf(appState.strings.tabMoves, appState.strings.tabActions, appState.strings.tabAI).forEachIndexed { index, title ->
                        val isSelected = pagerState.currentPage == index
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(8.dp)).clickable { coroutineScope.launch { pagerState.animateScrollToPage(index) } }, contentAlignment = Alignment.Center) {
                            Text(text = title, color = if (isSelected) RubikTheme.colors.textPrimary else RubikTheme.colors.textSecondary, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
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
                            appState.updateTargetVisuals(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height), 12.dp)
                        }
                    }) {
                        MovesGrid(appState = appState, canEditCube = canEditCube)
                    }
                }
                1 -> {
                    // Actions
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val isScrambleEnabled = canEditCube && !cubeState.isAnimating
                        Box(modifier = Modifier.weight(1f).height(44.dp).background(if (isScrambleEnabled) Color.Transparent else RubikTheme.colors.buttonDisabledBg).clip(RoundedCornerShape(12.dp)).combinedClickable(
                            onClick = { if (isScrambleEnabled) { coroutineScope.launch { appState.clearManualMoves(); cubeState.scramble() } } }
                        ), contentAlignment = Alignment.Center) {
                            Text(text = appState.strings.scrambleButton, color = if (isScrambleEnabled) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        val isUndoEnabled = canEditCube && !cubeState.isAnimating && cubeState.moveHistory.isNotEmpty()
                        Box(modifier = Modifier.weight(1f).height(44.dp).background(if (isUndoEnabled) Color.Transparent else RubikTheme.colors.buttonDisabledBg).clip(RoundedCornerShape(12.dp)).combinedClickable(
                            onClick = { if (isUndoEnabled) { coroutineScope.launch { appState.removeLastManualMove(); cubeState.undo() } } }
                        ), contentAlignment = Alignment.Center) {
                            Text(text = appState.strings.undoButton, color = if (isUndoEnabled) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        val isResetEnabled = !cubeState.isAnimating && !appState.isInitialState
                        Box(modifier = Modifier.weight(1f).height(44.dp).background(if (isResetEnabled) Color.Transparent else RubikTheme.colors.buttonDisabledBg).clip(RoundedCornerShape(12.dp)).combinedClickable(
                            onClick = { if (isResetEnabled) { coroutineScope.launch { cubeState.resetAnimated(); appState.clearManualMoves(); appState.updateTotalMoveCount(0) } } }
                        ), contentAlignment = Alignment.Center) {
                            Text(text = appState.strings.resetButton, color = if (isResetEnabled) RubikTheme.colors.accentRed else RubikTheme.colors.buttonDisabledText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                             // Complex buttons back
                            val isDesignEnabled = canEditCube && !cubeState.isAnimating
                            Box(modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(if (isDesignEnabled) RubikTheme.colors.backgroundPrimary else RubikTheme.colors.buttonDisabledBg).combinedClickable(
                                onClick = { if (isDesignEnabled) appState.updateShowEditorDialog(true) }
                            ), contentAlignment = Alignment.Center) {
                                Text(text = appState.strings.designButton, fontSize = 11.sp, color = if (isDesignEnabled) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText, fontWeight = FontWeight.Bold)
                            }
                            
                            val isSolveEnabled = canEditCube && !cubeState.isAnimating && !appState.isRecalculating && !appState.isSolved
                            Box(modifier = Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(if (isSolveEnabled) RubikTheme.colors.backgroundPrimary else RubikTheme.colors.buttonDisabledBg).combinedClickable(
                                onClick = {
                                    if (isSolveEnabled) {
                                        appState.updateRecalculating(true)
                                        coroutineScope.launch(Dispatchers.Default) {
                                            try {
                                                val currentSnapshot = cubeState.toSnapshot()
                                                val solver = RubikSolver()
                                                val lblDetails = solver.solveAnnotated(currentSnapshot)
                                                withContext(Dispatchers.Main) {
                                                    appState.updateActiveSolution(lblDetails?.map { it.move })
                                                    appState.updateActiveSolutionDetails(lblDetails)
                                                    appState.updateRecalculating(false)
                                                }
                                            } catch (e: Throwable) {
                                                withContext(Dispatchers.Main) { appState.updateRecalculating(false) }
                                            }
                                        }
                                    }
                                }
                            ), contentAlignment = Alignment.Center) {
                                if (appState.isRecalculating) CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                else Text(text = appState.strings.solveButton, fontSize = 11.sp, color = if (isSolveEnabled) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        // STATS
                        if (appState.solveSessions.isNotEmpty()) {
                            Text(text = appState.strings.bestTimesTitle, color = RubikTheme.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            appState.solveSessions.take(3).forEach { session ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${session.durationMillis / 1000}s", color = RubikTheme.colors.textSecondary, fontSize = 11.sp)
                                    Text(appState.strings.movesCountLabel.replace("%s", session.moveCount.toString()), color = RubikTheme.colors.textSecondary, fontSize = 11.sp)
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
