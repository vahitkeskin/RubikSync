package com.vahitkeskin.rubiksync.cube

import androidx.compose.ui.geometry.Offset

/**
 * Projects world-space cube coordinates into screen space for touch hit-testing
 * and drag-to-move resolution. Matches the camera model used by [CubeRenderer].
 */
internal class CubeScreenProjector(
    viewportWidth: Float,
    viewportHeight: Float,
    val yaw: Float,
    val pitch: Float,
    val cameraDistance: Float,
    val panX: Float,
    val panY: Float,
) {
    private val centerX = viewportWidth / 2f
    private val centerY = viewportHeight / 2f
    private val focalLength =
        (viewportWidth.coerceAtMost(viewportHeight) * 1.5f).coerceAtLeast(300f)

    fun project(worldPosition: Vector3): Offset {
        val cameraSpace = worldPosition.rotateY(yaw).rotateX(pitch)
        val depth = cameraDistance + cameraSpace.z
        val scale = focalLength / depth
        return Offset(
            x = centerX + panX + cameraSpace.x * scale,
            y = centerY - panY - cameraSpace.y * scale,
        )
    }

    fun depthInCameraSpace(worldPosition: Vector3): Float =
        worldPosition.rotateY(yaw).rotateX(pitch).z
}
