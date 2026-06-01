package com.vahitkeskin.rubiksync.cube

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class HitFaceInfo(
    val cubie: Cubie,
    val face: CubieFace,
    val worldCenter: Vector3,
    val projectedCenter: Offset
)

class GestureHandler(
    private val state: RubikCubeState,
    private val onCameraOrbit: (deltaYaw: Float, deltaPitch: Float) -> Unit,
    private val onCameraZoom: (deltaZoom: Float) -> Unit,
    private val onCameraPan: (deltaPanX: Float, deltaPanY: Float) -> Unit,
    private val onLayerRotate: (move: MoveType) -> Unit
) {
    private var dragStartOffset: Offset? = null
    private var hitFace: HitFaceInfo? = null
    private var isDraggingFace = false

    // Point-in-Polygon test (ray casting algorithm)
    private fun isPointInPolygon(p: Offset, poly: List<Offset>): Boolean {
        var isInside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val vi = poly[i]
            val vj = poly[j]
            if (((vi.y > p.y) != (vj.y > p.y)) &&
                (p.x < (vj.x - vi.x) * (p.y - vi.y) / (vj.y - vi.y + 0.00001f) + vi.x)
            ) {
                isInside = !isInside
            }
            j = i
        }
        return isInside
    }

    // Called on touch down (pointer pressed)
    fun handleTouchDown(
        pressOffset: Offset,
        width: Float,
        height: Float,
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float
    ) {
        dragStartOffset = pressOffset
        isDraggingFace = false
        hitFace = null

        // If the cube is currently animating a move, don't allow layer drags
        if (state.isAnimating) return

        // We run a hit-test against all visible faces
        // First, generate the projection coordinates of all faces, similar to the renderer
        val centerX = width / 2f
        val centerY = height / 2f
        val focalLength = (width.coerceAtMost(height) * 1.5f).coerceAtLeast(300f)

        // Helper to project a point
        fun project(worldPos: Vector3): Offset {
            var camPos = worldPos.rotateY(yaw)
            camPos = camPos.rotateX(pitch)
            val zDepth = cameraDistance + camPos.z
            val scale = focalLength / zDepth
            return Offset(centerX + panX + camPos.x * scale, centerY - panY - camPos.y * scale)
        }

        val visibleFaces = mutableListOf<HitFaceInfo>()
        val faceDepths = mutableListOf<Float>()
        val facePolygons = mutableListOf<List<Offset>>()

        // Cube sizes and outlines
        val cubieOutline = getRoundedSquarePoints(0.96f, 0.16f)

        state.cubies.forEach { cubie ->
            cubie.faces.forEach { face ->
                if (face.color == CubeColor.INTERNAL) return@forEach

                val worldNormal = cubie.getWorldNormal(face)
                var cameraNormal = worldNormal.rotateY(yaw)
                cameraNormal = cameraNormal.rotateX(pitch)

                val worldCenter = cubie.gridPos + worldNormal * 0.5f
                var cameraCenter = worldCenter.rotateY(yaw)
                cameraCenter = cameraCenter.rotateX(pitch)

                // Backface culling
                val viewVector = cameraCenter + Vector3(0f, 0f, cameraDistance)
                if (cameraNormal.dot(viewVector) >= 0f) return@forEach

                // Generate polygon vertices
                val poly = cubieOutline.map { localP ->
                    val faceP = mapToLocalFace(localP, face.localNormal)
                    val pWorld = cubie.gridPos + cubie.rightBasis * faceP.x + cubie.upBasis * faceP.y + cubie.forwardBasis * faceP.z
                    project(pWorld)
                }

                visibleFaces.add(HitFaceInfo(cubie, face, worldCenter, project(worldCenter)))
                faceDepths.add(cameraCenter.z)
                facePolygons.add(poly)
            }
        }

        // Find the closest hit face ( Painter's order: from front to back, i.e., smallest depth first )
        // We zip them together to keep references
        val zipped = visibleFaces.indices.map { i -> Triple(visibleFaces[i], faceDepths[i], facePolygons[i]) }
            .sortedBy { it.second } // Smallest camera depth (closest to screen) first

        for (item in zipped) {
            val info = item.first
            val poly = item.third
            if (isPointInPolygon(pressOffset, poly)) {
                hitFace = info
                isDraggingFace = true
                break
            }
        }
    }

    // Called on touch drag
    fun handleTouchDrag(
        currentOffset: Offset,
        dragAmount: Offset,
        width: Float,
        height: Float,
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float
    ) {
        val start = dragStartOffset ?: return

        if (isDraggingFace && hitFace != null) {
            val dx = currentOffset.x - start.x
            val dy = currentOffset.y - start.y
            val dist = sqrt(dx * dx + dy * dy)

            // Trigger layer rotation if drag exceeds threshold (25 pixels/dp)
            if (dist > 25f) {
                isDraggingFace = false // Trigger only once per gesture
                processLayerDrag(Offset(dx, dy), hitFace!!, width, height, yaw, pitch, cameraDistance, panX, panY)
            }
        } else {
            // Background drag: Camera orbit
            // Standard sensitivity: 0.007 radians per pixel
            val deltaYaw = -dragAmount.x * 0.007f
            val deltaPitch = -dragAmount.y * 0.007f
            onCameraOrbit(deltaYaw, deltaPitch)
        }
    }

    // Called on touch release
    fun handleTouchUp() {
        dragStartOffset = null
        hitFace = null
        isDraggingFace = false
    }

    private fun processLayerDrag(
        dragVector: Offset,
        hit: HitFaceInfo,
        width: Float,
        height: Float,
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float
    ) {
        val worldNormal = hit.cubie.getWorldNormal(hit.face)

        // Find the axes perpendicular to the normal
        val axes = mutableListOf<Vector3>()
        if (Math.abs(worldNormal.x) < 0.5f) axes.add(Vector3.UnitX)
        if (Math.abs(worldNormal.y) < 0.5f) axes.add(Vector3.UnitY)
        if (Math.abs(worldNormal.z) < 0.5f) axes.add(Vector3.UnitZ)

        if (axes.size != 2) return // Error checking

        // We check 4 possible moves: (Axis1 CW, Axis1 CCW, Axis2 CW, Axis2 CCW)
        // For each, we project the 3D rotation motion to screen and dot it with the drag vector
        val centerX = width / 2f
        val centerY = height / 2f
        val focalLength = (width.coerceAtMost(height) * 1.5f).coerceAtLeast(300f)

        fun project(worldPos: Vector3): Offset {
            var camPos = worldPos.rotateY(yaw)
            camPos = camPos.rotateX(pitch)
            val zDepth = cameraDistance + camPos.z
            val scale = focalLength / zDepth
            return Offset(centerX + panX + camPos.x * scale, centerY - panY - camPos.y * scale)
        }

        var bestMove: MoveType? = null
        var maxDot = -Float.MAX_VALUE

        // Look at all available moves in the enum
        MoveType.values().forEach { move ->
            // The move must rotate a layer that contains the hit cubie
            if (state.isCubieInLayer(hit.cubie, move.axis, move.layerValue)) {
                // The rotation axis of the move must be perpendicular to the face normal
                // (i.e. dot product of move axis and face normal is near 0)
                if (Math.abs(move.axis.dot(worldNormal)) < 0.5f) {
                    // Compute 3D direction of motion at the face center under this move
                    // velocity = axis x radius (vector from origin to face center)
                    val motionWorld = move.axis.cross(hit.worldCenter).normalized() * 0.4f
                    
                    // Project the motion vector to screen space
                    val p1 = project(hit.worldCenter)
                    val p2 = project(hit.worldCenter + motionWorld)
                    val motionScreen = p2 - p1

                    // Dot product with user's drag vector
                    val dot = dragVector.x * motionScreen.x + dragVector.y * motionScreen.y
                    if (dot > maxDot) {
                        maxDot = dot
                        bestMove = move
                    }
                }
            }
        }

        bestMove?.let {
            onLayerRotate(it)
        }
    }

    // Helper functions replicated from CubeRenderer for projection
    private fun getRoundedSquarePoints(size: Float, radius: Float): List<Vector3> {
        val points = mutableListOf<Vector3>()
        val half = size / 2f
        val r = radius.coerceAtMost(half)
        val inner = half - r
        val samples = 3

        fun addArc(cx: Float, cy: Float, startAngle: Float) {
            for (i in 0..samples) {
                val theta = startAngle + (i.toFloat() / samples) * (PI / 2.0).toFloat()
                points.add(Vector3(cx + r * cos(theta), cy + r * sin(theta), 0f))
            }
        }

        addArc(inner, inner, 0f)
        addArc(-inner, inner, (PI / 2.0).toFloat())
        addArc(-inner, -inner, PI.toFloat())
        addArc(inner, -inner, (3.0 * PI / 2.0).toFloat())
        return points
    }

    private fun mapToLocalFace(p: Vector3, localNormal: Vector3): Vector3 {
        val u: Vector3
        val v: Vector3
        when {
            localNormal.y > 0.5f -> { u = Vector3.UnitX; v = Vector3.UnitZ }
            localNormal.y < -0.5f -> { u = Vector3.UnitX; v = Vector3(0f, 0f, -1f) }
            localNormal.x > 0.5f -> { u = Vector3(0f, 0f, -1f); v = Vector3.UnitY }
            localNormal.x < -0.5f -> { u = Vector3.UnitZ; v = Vector3.UnitY }
            localNormal.z > 0.5f -> { u = Vector3.UnitX; v = Vector3.UnitY }
            localNormal.z < -0.5f -> { u = Vector3(-1f, 0f, 0f); v = Vector3.UnitY }
            else -> { u = Vector3.UnitX; v = Vector3.UnitY }
        }
        return localNormal * 0.5f + u * p.x + v * p.y
    }
}
