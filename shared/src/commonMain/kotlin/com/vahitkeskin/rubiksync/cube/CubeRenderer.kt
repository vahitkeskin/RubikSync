package com.vahitkeskin.rubiksync.cube

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class CubeRenderer(
    private val state: RubikCubeState,
    private val yaw: Float,   // horizontal camera angle
    private val pitch: Float, // vertical camera angle
    private val cameraDistance: Float = 7.0f,
    private val panX: Float = 0f,
    private val panY: Float = 0f,
    private val isDark: Boolean = true
) {
    // 3D outline of a rounded square centered at origin in the XY plane.
    // Size = 1.0f (from -0.5 to 0.5), corner radius = 0.15f for cubies
    private val cubieOutline = getRoundedSquarePoints(0.96f, 0.16f)

    // Size = 0.82f, corner radius = 0.12f for stickers
    private val stickerOutline = getRoundedSquarePoints(0.78f, 0.12f)

    // Generate points for a rounded square in XY plane (Z = 0)
    private fun getRoundedSquarePoints(size: Float, radius: Float, samples: Int = 3): List<Vector3> {
        val points = mutableListOf<Vector3>()
        val half = size / 2f
        val r = radius.coerceAtMost(half)
        val inner = half - r

        // Helper to add an arc
        fun addArc(cx: Float, cy: Float, startAngle: Float) {
            for (i in 0..samples) {
                val theta = startAngle + (i.toFloat() / samples) * (PI / 2.0).toFloat()
                points.add(Vector3(cx + r * cos(theta), cy + r * sin(theta), 0f))
            }
        }

        addArc(inner, inner, 0f)                          // Top-Right
        addArc(-inner, inner, (PI / 2.0).toFloat())        // Top-Left
        addArc(-inner, -inner, PI.toFloat())               // Bottom-Left
        addArc(inner, -inner, (3.0 * PI / 2.0).toFloat())  // Bottom-Right

        return points
    }

    // Maps a point in the XY plane to a specific local 3D face on the cubie
    private fun mapToLocalFace(p: Vector3, localNormal: Vector3): Vector3 {
        // Find tangent vectors u and v perpendicular to the local normal
        val u: Vector3
        val v: Vector3
        when {
            // Up face (+Y)
            localNormal.y > 0.5f -> {
                u = Vector3.UnitX
                v = Vector3.UnitZ
            }
            // Down face (-Y)
            localNormal.y < -0.5f -> {
                u = Vector3.UnitX
                v = Vector3(0f, 0f, -1f)
            }
            // Right face (+X)
            localNormal.x > 0.5f -> {
                u = Vector3(0f, 0f, -1f)
                v = Vector3.UnitY
            }
            // Left face (-X)
            localNormal.x < -0.5f -> {
                u = Vector3.UnitZ
                v = Vector3.UnitY
            }
            // Front face (+Z)
            localNormal.z > 0.5f -> {
                u = Vector3.UnitX
                v = Vector3.UnitY
            }
            // Back face (-Z)
            localNormal.z < -0.5f -> {
                u = Vector3(-1f, 0f, 0f)
                v = Vector3.UnitY
            }
            else -> {
                u = Vector3.UnitX
                v = Vector3.UnitY
            }
        }
        // Offset along normal by 0.5f to place on the cube surface, then add offset in face plane
        return localNormal * 0.5f + u * p.x + v * p.y
    }

    // Projects a 3D point in world coordinates to 2D screen coordinates
    private fun project(
        worldPos: Vector3,
        centerX: Float,
        centerY: Float,
        focalLength: Float
    ): Offset {
        // Camera rotation: Orbit camera
        // 1. Rotate around Y axis (yaw)
        var camPos = worldPos.rotateY(yaw)
        // 2. Rotate around X axis (pitch)
        camPos = camPos.rotateX(pitch)

        // Translate camera along Z-axis
        val zDepth = cameraDistance + camPos.z
        val scale = focalLength / zDepth

        // Apply pan offset
        val screenX = centerX + panX + camPos.x * scale
        val screenY = centerY - panY - camPos.y * scale // Flip Y since screen Y goes down

        return Offset(screenX, screenY)
    }

    // Represents a face prepared for drawing
    private class RenderFace(
        val cubie: Cubie,
        val face: CubieFace,
        val projectedBodyPoints: List<Offset>,
        val projectedStickerPoints: List<Offset>,
        val depth: Float,
        val worldNormal: Vector3
    )

    fun draw(drawScope: DrawScope, width: Float, height: Float) {
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Dynamic focal length based on size to keep the cube framed perfectly
        val focalLength = (width.coerceAtMost(height) * 1.5f).coerceAtLeast(300f)

        // Draw soft floor shadow first
        drawFloorShadow(drawScope, centerX, centerY, focalLength)

        // Step 1: Collect and transform all visible outer faces
        val renderFaces = mutableListOf<RenderFace>()

        state.cubies.forEach { cubie ->
            cubie.faces.forEach { face ->
                // Skip inner faces (no sticker)
                if (face.color == CubeColor.INTERNAL) return@forEach

                // Get face normal in world space
                var worldNormal = cubie.getWorldNormal(face)

                // If this cubie is currently rotating in the active move
                val activeMove = state.currentMove
                var rotating = false
                if (activeMove != null && state.isCubieInLayer(cubie, activeMove.axis, activeMove.layerValue)) {
                    rotating = true
                    worldNormal = worldNormal.rotateAround(activeMove.axis, activeMove.currentAngleRad)
                }

                // Transform normal to camera space to verify visibility
                var cameraNormal = worldNormal.rotateY(yaw)
                cameraNormal = cameraNormal.rotateX(pitch)

                // Get face center in world coordinates
                var worldCenter = cubie.gridPos + worldNormal * 0.5f
                if (rotating && activeMove != null) {
                    worldCenter = worldCenter.rotateAround(activeMove.axis, activeMove.currentAngleRad)
                }

                // Face center in camera space
                var cameraCenter = worldCenter.rotateY(yaw)
                cameraCenter = cameraCenter.rotateX(pitch)

                // Back-Face Culling: If face points away from camera, cull it
                // Center vector from camera (located at (0, 0, -cameraDistance) in rotated space) to face center
                val viewVector = cameraCenter + Vector3(0f, 0f, cameraDistance)
                if (cameraNormal.dot(viewVector) >= 0f) return@forEach

                // Generate points in world space for the cubie body and sticker
                val bodyPointsWorld = cubieOutline.map { localP ->
                    val faceP = mapToLocalFace(localP, face.localNormal)
                    var pWorld = cubie.gridPos + cubie.rightBasis * faceP.x + cubie.upBasis * faceP.y + cubie.forwardBasis * faceP.z
                    if (rotating && activeMove != null) {
                        pWorld = pWorld.rotateAround(activeMove.axis, activeMove.currentAngleRad)
                    }
                    pWorld
                }

                val stickerPointsWorld = stickerOutline.map { localP ->
                    val faceP = mapToLocalFace(localP, face.localNormal)
                    var pWorld = cubie.gridPos + cubie.rightBasis * faceP.x + cubie.upBasis * faceP.y + cubie.forwardBasis * faceP.z
                    if (rotating && activeMove != null) {
                        pWorld = pWorld.rotateAround(activeMove.axis, activeMove.currentAngleRad)
                    }
                    pWorld
                }

                // Project points to screen space
                val projectedBody = bodyPointsWorld.map { project(it, centerX, centerY, focalLength) }
                val projectedSticker = stickerPointsWorld.map { project(it, centerX, centerY, focalLength) }

                // Depth is the Z coordinate in camera space. Larger Z is further away.
                // We use cameraCenter.z as depth reference
                renderFaces.add(
                    RenderFace(
                        cubie = cubie,
                        face = face,
                        projectedBodyPoints = projectedBody,
                        projectedStickerPoints = projectedSticker,
                        depth = cameraCenter.z,
                        worldNormal = worldNormal
                    )
                )
            }
        }

        // Step 2: Sort visible faces by depth (back to front)
        // Descending order: largest depth (furthest) is drawn first
        renderFaces.sortByDescending { it.depth }

        // Lighting vectors
        val lightDir = Vector3(1.2f, 1.8f, 2.2f).normalized() // Top-Right-Front directional light
        
        // Camera direction in world space (points from origin to camera)
        // Since camera position in camera space is (0, 0, -D), in world space it is:
        val viewDir = Vector3(0f, 0f, -1f).rotateX(-pitch).rotateY(-yaw).normalized()
        val halfway = (lightDir + viewDir).normalized()

        // Step 3: Draw faces in sorted order
        renderFaces.forEach { rf ->
            // Calculate Lighting/Shading factors (brightened to remove the gray/muddy filter look)
            val ambient = 0.70f
            val diffuse = rf.worldNormal.dot(lightDir).coerceAtLeast(0f) * 0.30f
            
            // Specular highlights for shiny plastic look (tightened for elegance)
            val spec = rf.worldNormal.dot(halfway).coerceAtLeast(0f).pow(32f) * 0.20f

            val totalLight = (ambient + diffuse).coerceIn(0f, 1f)

            // Draw Premium Light/White Cubie Body
            val bodyBase = if (isDark) 0.80f else 0.92f
            val bodyColor = Color(
                red = (bodyBase * totalLight + spec * 0.15f).coerceIn(0f, 1f),
                green = (bodyBase * totalLight + spec * 0.15f).coerceIn(0f, 1f),
                blue = (bodyBase * totalLight + spec * 0.15f).coerceIn(0f, 1f)
            )
            drawPolygon(drawScope, rf.projectedBodyPoints, bodyColor, style = Fill)

            // Draw a subtle border around the cubie body for 3D depth and separation
            val bodyOutlineColor = if (isDark) Color_FF1E1E1E else Color_33000000
            drawPolygon(drawScope, rf.projectedBodyPoints, bodyOutlineColor, style = Stroke(width = 0.5f))

            // Draw Colored Sticker
            val baseColor = rf.face.color
            val r = ((baseColor.rgb shr 16) and 0xFF) / 255f
            val g = ((baseColor.rgb shr 8) and 0xFF) / 255f
            val b = (baseColor.rgb and 0xFF) / 255f

            val stickerColor = Color(
                red = (r * totalLight + spec).coerceIn(0f, 1f),
                green = (g * totalLight + spec).coerceIn(0f, 1f),
                blue = (b * totalLight + spec).coerceIn(0f, 1f)
            )
            drawPolygon(drawScope, rf.projectedStickerPoints, stickerColor, style = Fill)

            // Draw a distinct, elegant border around the sticker to keep colors highly distinct
            val stickerBorderColor = if (isDark) Color_FF1A1A1A else Color_66000000
            drawPolygon(drawScope, rf.projectedStickerPoints, stickerBorderColor, style = Stroke(width = 1f))
        }
    }

    private fun drawPolygon(
        drawScope: DrawScope,
        points: List<Offset>,
        color: Color,
        style: androidx.compose.ui.graphics.drawscope.DrawStyle = Fill
    ) {
        if (points.isEmpty()) return
        val path = Path()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        path.close()
        drawScope.drawPath(path, color, style = style)
    }

    private fun drawFloorShadow(drawScope: DrawScope, centerX: Float, centerY: Float, focalLength: Float) {
        // Floor plane is at Y = -2.2f
        val floorY = -2.3f
        
        // We project the center of the shadow (0, floorY, 0)
        var shadowCenterCam = Vector3(0f, floorY, 0f).rotateY(yaw).rotateX(pitch)
        val zDepth = cameraDistance + shadowCenterCam.z
        
        if (zDepth <= 0.1f) return // Behind camera
        
        val scale = focalLength / zDepth
        val shadowScreenX = centerX + panX + shadowCenterCam.x * scale
        val shadowScreenY = centerY - panY - shadowCenterCam.y * scale

        // Shadow width/height scale with distance
        val radius = 1.6f * scale
        
        // Draw soft floor shadow as a radial gradient or simple transparent oval
        drawScope.drawOval(
            color = Color.Black.copy(alpha = (0.28f * (5f / cameraDistance)).coerceIn(0f, 0.45f)),
            topLeft = Offset(shadowScreenX - radius * 1.2f, shadowScreenY - radius * 0.6f),
            size = androidx.compose.ui.geometry.Size(radius * 2.4f, radius * 1.2f)
        )
    }
}
