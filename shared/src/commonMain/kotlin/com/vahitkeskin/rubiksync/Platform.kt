package com.vahitkeskin.rubiksync

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vahitkeskin.rubiksync.solver.IntVector3

import androidx.compose.ui.graphics.ImageBitmap


interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentYear(): Int

expect fun runDetectionScript(): String

interface PixelGrid {
    val width: Int
    val height: Int
    fun getRGB(x: Int, y: Int): IntVector3
}

expect fun loadImagePixels(filePath: String): PixelGrid?

expect fun loadImageBitmap(filePath: String): ImageBitmap?

expect val isCameraSupported: Boolean

@Composable
expect fun CameraCaptureOrPicker(
    faceName: String,
    onImageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
)

expect fun logMoveDetail(moveLabel: String, phase: String, mathDetails: String)

@Composable
expect fun BindBackHandler(enabled: Boolean = true, onBack: () -> Unit)