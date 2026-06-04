package com.vahitkeskin.rubiksync.cube

import androidx.compose.ui.geometry.Offset

class GestureHandler(
    private val cubeState: RubikCubeState,
    private val onCameraOrbit: (deltaYaw: Float, deltaPitch: Float) -> Unit,
    private val onCameraZoom: (deltaZoom: Float) -> Unit,
    private val onCameraPan: (deltaPanX: Float, deltaPanY: Float) -> Unit,
    private val onLayerRotate: (move: MoveType) -> Unit,
) {
    private var dragStart: Offset? = null
    private var activeSticker: StickerHit? = null
    private var isLayerDragActive = false

    fun handleTouchDown(
        pressOffset: Offset,
        width: Float,
        height: Float,
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        layerTurnsEnabled: Boolean,
    ) {
        dragStart = pressOffset
        activeSticker = null
        isLayerDragActive = false

        if (cubeState.isAnimating || !layerTurnsEnabled) return

        val projector = projector(width, height, yaw, pitch, cameraDistance, panX, panY)
        activeSticker = findFrontmostSticker(pressOffset, projector)
        isLayerDragActive = activeSticker != null
    }

    fun handleTouchDrag(
        currentOffset: Offset,
        dragAmount: Offset,
        width: Float,
        height: Float,
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        layerTurnsEnabled: Boolean,
    ) {
        val start = dragStart ?: return

        if (!layerTurnsEnabled) {
            onCameraOrbit(-dragAmount.x * ORBIT_RADIANS_PER_PIXEL, -dragAmount.y * ORBIT_RADIANS_PER_PIXEL)
            return
        }

        val sticker = activeSticker

        if (isLayerDragActive && sticker != null) {
            val dragVector = currentOffset - start
            if (dragVector.getDistance() > LAYER_DRAG_THRESHOLD_PX) {
                isLayerDragActive = false
                val projector = projector(width, height, yaw, pitch, cameraDistance, panX, panY)
                LayerMoveSelector.selectMove(dragVector, sticker, cubeState, projector)
                    ?.let(onLayerRotate)
            }
            return
        }

        onCameraOrbit(-dragAmount.x * ORBIT_RADIANS_PER_PIXEL, -dragAmount.y * ORBIT_RADIANS_PER_PIXEL)
    }

    fun handleTouchUp() {
        dragStart = null
        activeSticker = null
        isLayerDragActive = false
    }

    private fun findFrontmostSticker(touchPoint: Offset, projector: CubeScreenProjector): StickerHit? {
        val outline = roundedSquareOutline(size = 0.96f, cornerRadius = 0.16f)
        val candidates = mutableListOf<Pair<StickerHit, Float>>()

        cubeState.cubies.forEach { cubie ->
            cubie.faces.forEach { face ->
                if (face.color == CubeColor.INTERNAL) return@forEach

                val worldNormal = cubie.getWorldNormal(face)
                val stickerCenter = cubie.gridPos + worldNormal * 0.5f

                if (!isStickerFacingCamera(worldNormal, stickerCenter, projector)) return@forEach

                val polygon = outline.map { localPoint ->
                    val onFace = localPoint.onFace(face.localNormal)
                    val worldPoint = cubie.gridPos +
                        cubie.rightBasis * onFace.x +
                        cubie.upBasis * onFace.y +
                        cubie.forwardBasis * onFace.z
                    projector.project(worldPoint)
                }

                if (!touchPoint.isInsidePolygon(polygon)) return@forEach

                val depth = projector.depthInCameraSpace(stickerCenter)
                candidates += StickerHit(cubie, face, stickerCenter) to depth
            }
        }

        return candidates.minByOrNull { it.second }?.first
    }

    private fun projector(
        width: Float,
        height: Float,
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
    ) = CubeScreenProjector(width, height, yaw, pitch, cameraDistance, panX, panY)

    private companion object {
        const val LAYER_DRAG_THRESHOLD_PX = 25f
        const val ORBIT_RADIANS_PER_PIXEL = 0.007f
    }
}
