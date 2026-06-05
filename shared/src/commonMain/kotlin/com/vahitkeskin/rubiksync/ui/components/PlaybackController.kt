package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.getMoveMathDetails
import com.vahitkeskin.rubiksync.logMoveDetail
import com.vahitkeskin.rubiksync.ui.state.*
import kotlinx.coroutines.launch

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
            .border(
                1.dp,
                RubikTheme.colors.cardBorder,
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
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
                .border(
                    1.dp,
                    if (RubikTheme.colors.isDark) AccentGreenAlpha13 else AccentGreenAlpha20,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val currentStepDisplay =
                    (appState.currentSolutionStep + 1).coerceAtMost(solution.size)
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
                        appState.updateActiveSolution(null)
                        appState.updateActiveSolutionDetails(null)
                        appState.updatePlaybackRunning(false)
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
                    Modifier.background(
                        Brush.horizontalGradient(
                            listOf(
                                AccentGreen,
                                AccentGreenVibrant
                            )
                        )
                    )
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
                        appState.clearManualMoves()
                        cubeState.setCustomStateAnimated(appState.editorFaces)
                        appState.updateCurrentSolutionStep(0)
                        appState.updatePlaybackRunning(false)
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
                onClick = { appState.updatePlaybackRunning(!appState.isPlaybackRunning) },
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
                            val activeDetail =
                                appState.activeSolutionDetails?.getOrNull(appState.currentSolutionStep)
                            val phase = activeDetail?.phaseName ?: "Çözüm"
                            val mathDetails = getMoveMathDetails(nextMove)
                            logMoveDetail(nextMove.label, phase, mathDetails)

                            cubeState.executeMove(nextMove)
                            appState.incrementSolutionStep()
                            appState.incrementTotalMoveCount()
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
                                text = appState.strings.remainingLabel.replace(
                                    "%s",
                                    (detailsList.size - currentStep).toString()
                                ),
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
                                .border(
                                    0.5.dp,
                                    RubikTheme.colors.borderSubtle,
                                    RoundedCornerShape(6.dp)
                                )
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

@Preview
@Composable
fun PlaybackControllerDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val appState = rememberPreviewRubikAppState()
        appState.updateActiveSolution(listOf(MoveType.U, MoveType.R_PRIME, MoveType.F))
        PlaybackController(
            appState = appState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
fun PlaybackControllerLightPreview() {
    PreviewRubikTheme(isDark = false) {
        val appState = rememberPreviewRubikAppState()
        appState.updateActiveSolution(listOf(MoveType.U, MoveType.R_PRIME, MoveType.F))
        PlaybackController(
            appState = appState,
            modifier = Modifier.padding(16.dp)
        )
    }
}
