package com.vahitkeskin.rubiksync.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ArrowForwardIcon: ImageVector = ImageVector.Builder(
    name = "ArrowForward",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = SolidColor(Color.Black),
    pathFillType = androidx.compose.ui.graphics.PathFillType.NonZero
) {
    moveTo(4f, 11f)
    lineTo(16.17f, 11f)
    lineTo(10.58f, 5.41f)
    lineTo(12f, 4f)
    lineTo(20f, 12f)
    lineTo(12f, 20f)
    lineTo(10.59f, 18.59f)
    lineTo(16.17f, 13f)
    lineTo(4f, 13f)
    close()
}.build()
