package com.vahitkeskin.rubiksync.cube

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Rounded square outline in the sticker's local XY plane (matches [CubeRenderer] topology). */
internal fun roundedSquareOutline(size: Float, cornerRadius: Float, arcSamples: Int = 3): List<Vector3> {
    val points = mutableListOf<Vector3>()
    val half = size / 2f
    val radius = cornerRadius.coerceAtMost(half)
    val inner = half - radius

    fun addArc(centerX: Float, centerY: Float, startAngle: Float) {
        repeat(arcSamples + 1) { step ->
            val angle = startAngle + (step.toFloat() / arcSamples) * (PI / 2f).toFloat()
            points += Vector3(
                centerX + radius * cos(angle),
                centerY + radius * sin(angle),
                0f,
            )
        }
    }

    addArc(inner, inner, 0f)
    addArc(-inner, inner, (PI / 2f).toFloat())
    addArc(-inner, -inner, PI.toFloat())
    addArc(inner, -inner, (3f * PI / 2f).toFloat())
    return points
}

/** Maps a 2D outline point onto the cubie face plane in local space. */
internal fun Vector3.onFace(localNormal: Vector3): Vector3 {
    val tangentU: Vector3
    val tangentV: Vector3
    when {
        localNormal.y > 0.5f -> { tangentU = Vector3.UnitX; tangentV = Vector3.UnitZ }
        localNormal.y < -0.5f -> { tangentU = Vector3.UnitX; tangentV = Vector3(0f, 0f, -1f) }
        localNormal.x > 0.5f -> { tangentU = Vector3(0f, 0f, -1f); tangentV = Vector3.UnitY }
        localNormal.x < -0.5f -> { tangentU = Vector3.UnitZ; tangentV = Vector3.UnitY }
        localNormal.z > 0.5f -> { tangentU = Vector3.UnitX; tangentV = Vector3.UnitY }
        localNormal.z < -0.5f -> { tangentU = Vector3(-1f, 0f, 0f); tangentV = Vector3.UnitY }
        else -> { tangentU = Vector3.UnitX; tangentV = Vector3.UnitY }
    }
    return localNormal * 0.5f + tangentU * x + tangentV * y
}

internal fun isStickerFacingCamera(
    worldNormal: Vector3,
    stickerCenter: Vector3,
    projector: CubeScreenProjector,
): Boolean {
    val cameraNormal = worldNormal.rotateY(projector.yaw).rotateX(projector.pitch)
    val cameraCenter = stickerCenter.rotateY(projector.yaw).rotateX(projector.pitch)
    val viewDirection = cameraCenter + Vector3(0f, 0f, projector.cameraDistance)
    return cameraNormal.dot(viewDirection) < 0f
}

internal fun Offset.isInsidePolygon(polygon: List<Offset>): Boolean {
    var inside = false
    var previous = polygon.lastIndex
    for (current in polygon.indices) {
        val a = polygon[current]
        val b = polygon[previous]
        val crossesHorizontalBand = (a.y > y) != (b.y > y)
        val crossesRay = x < (b.x - a.x) * (y - a.y) / (b.y - a.y + 1e-5f) + a.x
        if (crossesHorizontalBand && crossesRay) inside = !inside
        previous = current
    }
    return inside
}
