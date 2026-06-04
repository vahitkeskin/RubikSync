package com.vahitkeskin.rubiksync.cube

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

/**
 * Picks the layer turn that best matches a screen-space drag on a sticker.
 *
 * Each candidate move is scored by projecting its instantaneous sticker velocity
 * (right-hand rule: axis × position, scaled by [MoveType.angleSign]) onto the drag vector.
 */
internal object LayerMoveSelector {

    private const val MOTION_SAMPLE_DISTANCE = 0.4f
    private const val MIN_TANGENT_LENGTH = 0.001f
    private const val AXIS_PERPENDICULAR_EPSILON = 0.5f

    fun selectMove(
        dragVector: Offset,
        hit: StickerHit,
        cubeState: RubikCubeState,
        projector: CubeScreenProjector,
    ): MoveType? {
        val faceNormal = hit.cubie.getWorldNormal(hit.face)

        var bestMove: MoveType? = null
        var bestAlignment = Float.NEGATIVE_INFINITY

        for (move in MoveType.values()) {
            if (!cubeState.isCubieInLayer(hit.cubie, move.axis, move.layerValue)) continue
            if (abs(move.axis.dot(faceNormal)) >= AXIS_PERPENDICULAR_EPSILON) continue

            val screenMotion = screenMotionForMove(move, hit, projector) ?: continue
            val alignment = dragVector.x * screenMotion.x + dragVector.y * screenMotion.y

            if (alignment > bestAlignment) {
                bestAlignment = alignment
                bestMove = move
            }
        }

        return bestMove
    }

    private fun screenMotionForMove(
        move: MoveType,
        hit: StickerHit,
        projector: CubeScreenProjector,
    ): Offset? {
        val tangent = move.axis.cross(hit.cubie.gridPos)
        if (tangent.length() < MIN_TANGENT_LENGTH) return null

        val worldVelocity = tangent.normalized() * move.angleSign
        val origin = projector.project(hit.stickerCenter)
        val tip = projector.project(hit.stickerCenter + worldVelocity * MOTION_SAMPLE_DISTANCE)
        return tip - origin
    }
}

internal data class StickerHit(
    val cubie: Cubie,
    val face: CubieFace,
    val stickerCenter: Vector3,
)
