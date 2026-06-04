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
 * Drives Compose recomposition on every animation frame.
 * Canvas draw lambdas do not observe [RubikCubeState] changes unless state is read during composition.
 */
@Composable
fun rememberCubeAnimationFrameTick(cubeState: RubikCubeState): Int {
    var frameTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(cubeState.isAnimating) {
        if (!cubeState.isAnimating) return@LaunchedEffect
        while (cubeState.isAnimating) {
            withFrameMillis {
                frameTick++
            }
        }
    }

    // Layer-turn animation updates this property inside [RubikCubeState.executeMove]
    val layerRotation = cubeState.currentMove?.currentAngleRad
    return frameTick + (layerRotation?.toBits() ?: 0)
}
