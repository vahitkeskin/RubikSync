package com.vahitkeskin.rubiksync.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CameraIcon: ImageVector = ImageVector.Builder(
    name = "Camera",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = SolidColor(Color.Black),
    pathFillType = androidx.compose.ui.graphics.PathFillType.NonZero
) {
    // Camera body
    moveTo(12f, 10.8f)
    curveTo(10.79f, 10.8f, 9.8f, 11.79f, 9.8f, 13f)
    curveTo(9.8f, 14.21f, 10.79f, 15.2f, 12f, 15.2f)
    curveTo(13.21f, 15.2f, 14.2f, 14.21f, 14.2f, 13f)
    curveTo(14.2f, 11.79f, 13.21f, 10.8f, 12f, 10.8f)
    close()
    // Outer shell
    moveTo(9f, 2f)
    lineTo(7.17f, 4f)
    lineTo(4f, 4f)
    curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
    lineTo(2f, 20f)
    curveTo(2f, 21.1f, 2.9f, 22f, 4f, 22f)
    lineTo(20f, 22f)
    curveTo(21.1f, 22f, 22f, 21.1f, 22f, 20f)
    lineTo(22f, 6f)
    curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
    lineTo(16.83f, 4f)
    lineTo(15f, 2f)
    close()
    moveTo(12f, 17f)
    curveTo(9.79f, 17f, 8f, 15.21f, 8f, 13f)
    curveTo(8f, 10.79f, 9.79f, 9f, 12f, 9f)
    curveTo(14.21f, 9f, 16f, 10.79f, 16f, 13f)
    curveTo(16f, 15.21f, 14.21f, 17f, 12f, 17f)
    close()
}.build()
