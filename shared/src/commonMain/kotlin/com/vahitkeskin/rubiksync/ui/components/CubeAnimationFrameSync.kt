package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import com.vahitkeskin.rubiksync.cube.RubikCubeState

/**
 * Keeps the home-screen [androidx.compose.foundation.Canvas] in sync with cube animations.
 *
 * Draw scopes do not subscribe to [RubikCubeState]; reading a frame tick during composition
 * forces a redraw while [RubikCubeState.isAnimating] is true.
 */
@Composable
fun rememberCubeAnimationFrameTick(cubeState: RubikCubeState): Int {
    var frameTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(cubeState.isAnimating) {
        if (!cubeState.isAnimating) return@LaunchedEffect
        while (cubeState.isAnimating) {
            withFrameMillis { _ -> frameTick++ }
        }
    }

    // Observe active layer rotation (updated each frame inside executeMove).
    val layerAngle = cubeState.currentMove?.currentAngleRad ?: 0f
    return frameTick + layerAngle.toBits()
}
