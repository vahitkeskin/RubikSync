package com.vahitkeskin.rubiksync.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ArrowBackIcon: ImageVector = ImageVector.Builder(
    name = "ArrowBack",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = SolidColor(Color.Black),
    pathFillType = androidx.compose.ui.graphics.PathFillType.NonZero
) {
    moveTo(20f, 11f)
    lineTo(7.83f, 11f)
    lineTo(13.42f, 5.41f)
    lineTo(12f, 4f)
    lineTo(4f, 12f)
    lineTo(12f, 20f)
    lineTo(13.41f, 18.59f)
    lineTo(7.83f, 13f)
    lineTo(20f, 13f)
    close()
}.build()
