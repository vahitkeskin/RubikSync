package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.ui.state.*

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

@Preview
@Composable
fun SpeedControlDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RubikTheme.colors.backgroundPanel)
                .padding(16.dp)
        ) {
            SpeedControl(
                appState = rememberPreviewRubikAppState(),
                accentColor = RubikTheme.colors.accentOrange
            )
        }
    }
}

@Preview
@Composable
fun SpeedControlLightPreview() {
    PreviewRubikTheme(isDark = false) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(RubikTheme.colors.backgroundPanel)
                .padding(16.dp)
        ) {
            SpeedControl(
                appState = rememberPreviewRubikAppState(),
                accentColor = RubikTheme.colors.accentOrange
            )
        }
    }
}
