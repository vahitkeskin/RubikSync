package com.vahitkeskin.rubiksync.ui.components

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
fun DashboardHeader(
    cubeState: RubikCubeState,
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Row — Logo-style with accent dot
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Accent dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AccentOrange, AccentRedCoral)
                            )
                        )
                )
                Column {
                    Text(
                        text = appState.strings.appTitle,
                        color = RubikTheme.colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                        maxLines = 1
                    )
                    Text(
                        text = appState.strings.appSubtitle,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        maxLines = 1
                    )
                }
            }

            // Status & Stats — mini glassmorphism cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Move Counter
                StatChip(
                    emoji = "🎯",
                    value = "${cubeState.moveHistory.size}",
                    label = appState.strings.movesLabel,
                    accentColor = RubikTheme.colors.accentBlue
                )

                // Solved Status
                val solved = appState.isSolved
                StatChip(
                    emoji = if (solved) "✅" else "🔄",
                    value = if (solved) appState.strings.solvedStatus else appState.strings.scrambledStatus,
                    label = appState.strings.statusLabel,
                    accentColor = if (solved) RubikTheme.colors.accentGreen else RubikTheme.colors.accentOrange
                )

                // Küp düzenleme ikonu — kilit/açık durumuna göre
                val editableBgColor by animateColorAsState(
                    targetValue = if (appState.isCubeEditable) {
                        RubikTheme.colors.accentGreen.copy(alpha = 0.15f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.15f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "editableBg"
                )
                val editableBorderColor by animateColorAsState(
                    targetValue = if (appState.isCubeEditable) {
                        RubikTheme.colors.accentGreen.copy(alpha = 0.35f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.35f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "editableBorder"
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(editableBgColor)
                        .border(0.5.dp, editableBorderColor, RoundedCornerShape(8.dp))
                        .clickable(
                            enabled = !cubeState.isAnimating
                        ) {
                            appState.updateCubeEditable(!appState.isCubeEditable)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (appState.isCubeEditable) "🔓" else "🔒",
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }

                // Zeka küpü döndürme ses ikonu — hoparlör/sessiz
                val soundBgColor by animateColorAsState(
                    targetValue = if (!appState.isCubeEditable) {
                        RubikTheme.colors.cardBackground.copy(alpha = 0.5f)
                    } else if (appState.isSoundEnabled) {
                        RubikTheme.colors.accentBlue.copy(alpha = 0.15f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.15f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "soundBg"
                )
                val soundBorderColor by animateColorAsState(
                    targetValue = if (!appState.isCubeEditable) {
                        RubikTheme.colors.cardBorder.copy(alpha = 0.3f)
                    } else if (appState.isSoundEnabled) {
                        RubikTheme.colors.accentBlue.copy(alpha = 0.35f)
                    } else {
                        RubikTheme.colors.accentRed.copy(alpha = 0.35f)
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "soundBorder"
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(soundBgColor)
                        .border(0.5.dp, soundBorderColor, RoundedCornerShape(8.dp))
                        .clickable(
                            enabled = appState.isCubeEditable && !cubeState.isAnimating
                        ) {
                            appState.updateSoundEnabled(!appState.isSoundEnabled)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (appState.isSoundEnabled) "🔊" else "🔇",
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.alpha(if (appState.isCubeEditable) 1f else 0.4f)
                    )
                }

                // Ayarlar butonu
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RubikTheme.colors.cardBackground)
                        .border(0.5.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(8.dp))
                        .clickable { appState.showSettingsScreen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Move History — scrollable chips with refined styling
        if (cubeState.moveHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(RubikTheme.colors.cardBackground)
                    .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(10.dp))
                    .padding(vertical = 5.dp, horizontal = 8.dp)
                    .horizontalScroll(rememberScrollState(initial = 10000)),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝",
                    fontSize = 9.sp,
                    maxLines = 1
                )

                cubeState.moveHistory.takeLast(14).forEachIndexed { idx, move ->
                    val isLast = idx == cubeState.moveHistory.takeLast(14).lastIndex

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isLast) {
                                    Brush.horizontalGradient(listOf(AccentOrange, AccentRedCoral))
                                } else {
                                    Brush.horizontalGradient(listOf(RubikTheme.colors.backgroundTertiary, RubikTheme.colors.backgroundTertiary))
                                }
                            )
                            .then(
                                if (!isLast) Modifier.border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(6.dp))
                                else Modifier
                            )
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = move.label,
                            color = if (isLast) Color.White else RubikTheme.colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = if (isLast) FontWeight.ExtraBold else FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun StatChip(
    emoji: String,
    value: String,
    label: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RubikTheme.colors.cardBackground)
            .border(0.5.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 10.sp,
            maxLines = 1
        )
        Column {
            Text(
                text = value,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                lineHeight = 12.sp
            )
            Text(
                text = label,
                color = RubikTheme.colors.textSecondary,
                fontSize = 7.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                lineHeight = 8.sp
            )
        }
    }
}
