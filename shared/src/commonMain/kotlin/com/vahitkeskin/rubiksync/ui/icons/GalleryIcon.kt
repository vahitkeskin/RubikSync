package com.vahitkeskin.rubiksync.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val GalleryIcon: ImageVector = ImageVector.Builder(
    name = "Gallery",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = SolidColor(Color.Black),
    pathFillType = androidx.compose.ui.graphics.PathFillType.NonZero
) {
    // Photo/image icon
    moveTo(21f, 19f)
    lineTo(21f, 5f)
    curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
    lineTo(5f, 3f)
    curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
    lineTo(3f, 19f)
    curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
    lineTo(19f, 21f)
    curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
    close()
    // Mountain landscape
    moveTo(8.5f, 13.5f)
    lineTo(11f, 16.51f)
    lineTo(14.5f, 12f)
    lineTo(19f, 18f)
    lineTo(5f, 18f)
    close()
}.build()
