package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.ui.state.*
import kotlinx.coroutines.launch

@Composable
internal fun MovesGrid(
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
                    if (canEditCube && !cubeState.isAnimating) Brush.verticalGradient(
                        listOf(
                            c1,
                            c2
                        )
                    )
                    else Brush.verticalGradient(
                        listOf(
                            RubikTheme.colors.buttonDisabledBg,
                            RubikTheme.colors.buttonDisabledBg
                        )
                    )
                )
                .border(0.5.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                .clickable(enabled = canEditCube && !cubeState.isAnimating) {
                    coroutineScope.launch {
                        cubeState.executeMove(move)
                        appState.addManualMove(move)
                        appState.incrementTotalMoveCount()
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

@Preview
@Composable
fun MovesGridDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val appState = rememberPreviewRubikAppState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RubikTheme.colors.backgroundPanel)
                .padding(16.dp)
        ) {
            MovesGrid(
                appState = appState,
                canEditCube = true
            )
        }
    }
}

@Preview
@Composable
fun MovesGridLightPreview() {
    PreviewRubikTheme(isDark = false) {
        val appState = rememberPreviewRubikAppState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RubikTheme.colors.backgroundPanel)
                .padding(16.dp)
        ) {
            MovesGrid(
                appState = appState,
                canEditCube = true
            )
        }
    }
}
