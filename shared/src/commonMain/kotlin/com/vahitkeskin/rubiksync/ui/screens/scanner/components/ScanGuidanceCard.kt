package com.vahitkeskin.rubiksync.ui.screens.scanner.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun ScanGuidanceCard(
    appState: RubikAppState,
    guidanceMessage: String
) {
    val displayMessage = appState.infoMessage ?: guidanceMessage
    val isSuccess = appState.infoMessage != null

    val currentFace = FaceName.entries[appState.scannerStep]
    val isCurrentFaceScanned = appState.scannedFilePaths.containsKey(currentFace)
    val allScanned = appState.scannedFilePaths.size == 6

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.horizontalGradient(
                    if (isSuccess) {
                        if (RubikTheme.colors.isDark) {
                            listOf(AccentGreenShadow, AccentGreenVeryDark)
                        } else {
                            listOf(AccentGreenFaintBg, AccentGreenSoftBg)
                        }
                    } else {
                        if (RubikTheme.colors.isDark) {
                            listOf(DarkBgSecondary, DarkBgTertiary)
                        } else {
                            listOf(LightBgPrimary, LightBgTertiary)
                        }
                    }
                )
            )
            .border(
                width = 0.5.dp,
                color = if (isSuccess) {
                    if (RubikTheme.colors.isDark) AccentGreenAlpha13 else AccentGreenAlpha20
                } else {
                    RubikTheme.colors.borderSubtle
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = isSuccess) { appState.updateInfoMessage(null) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSuccess) {
            Text(
                text = "✅ $displayMessage",
                color = if (RubikTheme.colors.isDark) AccentGreenBright else AccentGreenDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        } else if (allScanned) {
            Text(
                text = "✅ $displayMessage",
                color = if (RubikTheme.colors.isDark) AccentGreenBright else AccentGreenDark,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        } else if (!isCurrentFaceScanned) {
            // Enhanced guidance with mini face preview for unscanned face
            val centerColor = when (currentFace) {
                FaceName.U -> CubeOrange
                FaceName.D -> CubeRed
                FaceName.L -> CubeYellow
                FaceName.R -> White
                FaceName.F -> CubeGreen
                FaceName.B -> CubeBlue
            }

            val exampleText = when (currentFace) {
                FaceName.U -> appState.strings.scanGuidanceExampleU
                FaceName.D -> appState.strings.scanGuidanceExampleD
                FaceName.L -> appState.strings.scanGuidanceExampleL
                FaceName.R -> appState.strings.scanGuidanceExampleR
                FaceName.F -> appState.strings.scanGuidanceExampleF
                FaceName.B -> appState.strings.scanGuidanceExampleB
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Mini 3x3 scrambled face preview showing center color
                MiniFacePreview(
                    centerColor = centerColor,
                    isDark = RubikTheme.colors.isDark
                )

                // Text info section
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "📸 $displayMessage",
                        color = RubikTheme.colors.textPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "💡 $exampleText",
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Text(
                text = "📸 $displayMessage",
                color = RubikTheme.colors.textPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Mini 3x3 face preview that simulates a scrambled cube face.
 * The center sticker always shows the actual face color (since center never moves),
 * while surrounding stickers show random/mixed colors to indicate a scrambled state.
 * A pulsing border highlights the center to draw attention to "find this color".
 */
@Composable
private fun MiniFacePreview(
    centerColor: Color,
    isDark: Boolean
) {
    // All possible cube colors for the scrambled surrounding stickers
    val allColors = listOf(CubeOrange, CubeRed, CubeYellow, White, CubeGreen, CubeBlue)

    // Fixed scrambled pattern: surrounding stickers use different colors
    // to clearly show this is NOT a solved face — only center matters
    val scrambledColors = listOf(
        allColors.filter { it != centerColor }.let { others ->
            listOf(
                others[0], others[2], others[4],
                others[1], centerColor, others[3],
                others[4], others[0], others[2]
            )
        }
    ).first()

    val cellSize = 11.dp
    val gap = 1.dp
    val borderColor = if (isDark) WhiteAlpha20 else BlackAlpha20
    val bgColor = if (isDark) DarkBgQuaternary else LightBgTertiary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(0.5.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(gap),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0..2) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        val isCenter = (row == 1 && col == 1)
                        val stickerColor = scrambledColors[index]

                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(2.dp))
                                .background(stickerColor)
                                .then(
                                    if (isCenter) {
                                        // Highlight center sticker with a prominent border
                                        Modifier.border(
                                            width = 1.5.dp,
                                            color = if (isDark) White else Color.Black,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                    } else {
                                        Modifier.border(
                                            width = 0.5.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Small dot indicator on center to draw extra attention
                            if (isCenter) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (centerColor == White) Color.Black.copy(alpha = 0.5f)
                                            else White.copy(alpha = 0.7f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
