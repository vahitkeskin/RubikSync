package com.vahitkeskin.rubiksync.cube

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.state.RubikAppState

@Composable
fun CubeRotationGuide(
    appState: RubikAppState,
    currentFace: FaceName,
    modifier: Modifier = Modifier
) {
    // Smoothly animate yaw and pitch to match the target face's camera viewing angle
    val transition = updateTransition(currentFace, label = "CubeRotationGuide")
    val yaw by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 700, easing = FastOutSlowInEasing) },
        label = "yaw"
    ) { face ->
        when (face) {
            FaceName.U -> -0.5f
            FaceName.D -> -0.5f
            FaceName.L -> 1.0f
            FaceName.R -> -1.0f
            FaceName.F -> 0.0f
            FaceName.B -> 3.14159f
        }
    }
    
    val pitch by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 700, easing = FastOutSlowInEasing) },
        label = "pitch"
    ) { face ->
        when (face) {
            FaceName.U -> 0.5f
            FaceName.D -> -0.5f
            FaceName.L -> 0.2f
            FaceName.R -> 0.2f
            FaceName.F -> 0.2f
            FaceName.B -> 0.2f
        }
    }

    // Static representation of a solved cube (27 cubies)
    val miniCubies = remember {
        val list = mutableListOf<Cubie>()
        var index = 0
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    val pos = Vector3(x.toFloat(), y.toFloat(), z.toFloat())
                    list.add(Cubie(index++, pos, pos))
                }
            }
        }
        list
    }

    val faceDisplayName = when (currentFace) {
        FaceName.U -> appState.strings.faceU
        FaceName.D -> appState.strings.faceD
        FaceName.L -> appState.strings.faceL
        FaceName.R -> appState.strings.faceR
        FaceName.F -> appState.strings.faceF
        FaceName.B -> appState.strings.faceB
    }

    val centerColorLocalized = when (currentFace) {
        FaceName.U -> appState.strings.colorOrange
        FaceName.D -> appState.strings.colorRed
        FaceName.L -> appState.strings.colorYellow
        FaceName.R -> appState.strings.colorWhite
        FaceName.F -> appState.strings.colorGreen
        FaceName.B -> appState.strings.colorBlue
    }

    // Details for each face
    val faceTitle = appState.strings.faceTitleLabel
        .replaceFirst("%s", faceDisplayName)
        .replaceFirst("%s", currentFace.name)

    val faceColorName = appState.strings.centerColorLabel
        .replace("%s", centerColorLocalized)

    val faceColorHex = when (currentFace) {
        FaceName.U -> Color(0xFFFF5F00)
        FaceName.D -> Color(0xFFD6001C)
        FaceName.L -> Color(0xFFFFD500)
        FaceName.R -> Color(0xFFFFFFFF)
        FaceName.F -> Color(0xFF009B48)
        FaceName.B -> Color(0xFF0046AD)
    }

    val guideInstruction = when (currentFace) {
        FaceName.U -> appState.strings.guideU
        FaceName.D -> appState.strings.guideD
        FaceName.L -> appState.strings.guideL
        FaceName.R -> appState.strings.guideR
        FaceName.F -> appState.strings.guideF
        FaceName.B -> appState.strings.guideB
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = RubikTheme.colors.cardBackground),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: 3D Animated Guide Canvas
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(RubikTheme.colors.backgroundPrimary),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawGuideCube(miniCubies, yaw, pitch, currentFace)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Column: Instructions and 2D Unfolded Map
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = faceTitle,
                            color = RubikTheme.colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = faceColorName,
                            color = faceColorHex,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Small 2D Net Indicator
                    Mini2DNet(currentFace)
                }

                Text(
                    text = guideInstruction,
                    color = RubikTheme.colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun Mini2DNet(targetFace: FaceName) {
    val faceColorMap = mapOf(
        FaceName.U to Color(0xFFFF5F00),
        FaceName.D to Color(0xFFD6001C),
        FaceName.L to Color(0xFFFFD500),
        FaceName.R to Color(0xFFFFFFFF),
        FaceName.F to Color(0xFF009B48),
        FaceName.B to Color(0xFF0046AD)
    )

    @Composable
    fun MiniCell(face: FaceName) {
        val isTarget = face == targetFace
        val color = faceColorMap[face] ?: Color.Gray
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
                .border(
                    width = if (isTarget) 1.5.dp else 0.5.dp,
                    color = if (isTarget) RubikTheme.colors.textPrimary else RubikTheme.colors.borderSubtle,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }

    @Composable
    fun EmptyCell() {
        Box(modifier = Modifier.size(12.dp))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: U
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            EmptyCell()
            MiniCell(FaceName.U)
            EmptyCell()
            EmptyCell()
        }
        // Row 2: L, F, R, B
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            MiniCell(FaceName.L)
            MiniCell(FaceName.F)
            MiniCell(FaceName.R)
            MiniCell(FaceName.B)
        }
        // Row 3: D
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            EmptyCell()
            MiniCell(FaceName.D)
            EmptyCell()
            EmptyCell()
        }
    }
}

// 3D Projection & Rendering for the Mini Guide Cube
private fun DrawScope.drawGuideCube(
    cubies: List<Cubie>,
    yaw: Float,
    pitch: Float,
    targetFace: FaceName
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val cameraDistance = 6.5f
    val focalLength = size.width * 1.5f

    // Bounding outlines for cubies and stickers
    val cubieOutline = getGuideRoundedPoints(0.96f, 0.16f)
    val stickerOutline = getGuideRoundedPoints(0.78f, 0.12f)

    class GuideRenderFace(
        val face: CubieFace,
        val projectedBodyPoints: List<Offset>,
        val projectedStickerPoints: List<Offset>,
        val depth: Float,
        val worldNormal: Vector3,
        val isTarget: Boolean
    )

    fun getFaceNameOfNormal(normal: Vector3): FaceName? {
        return when {
            normal.y > 0.5f -> FaceName.U
            normal.y < -0.5f -> FaceName.D
            normal.x < -0.5f -> FaceName.L
            normal.x > 0.5f -> FaceName.R
            normal.z > 0.5f -> FaceName.F
            normal.z < -0.5f -> FaceName.B
            else -> null
        }
    }

    val renderFaces = mutableListOf<GuideRenderFace>()

    cubies.forEach { cubie ->
        cubie.faces.forEach { face ->
            if (face.color == CubeColor.INTERNAL) return@forEach

            val worldNormal = cubie.getWorldNormal(face)
            val faceName = getFaceNameOfNormal(worldNormal)
            val isTarget = faceName == targetFace

            // camera normal mapping
            var cameraNormal = worldNormal.rotateY(yaw)
            cameraNormal = cameraNormal.rotateX(pitch)

            val worldCenter = cubie.gridPos + worldNormal * 0.5f
            var cameraCenter = worldCenter.rotateY(yaw)
            cameraCenter = cameraCenter.rotateX(pitch)

            // Back face culling
            val viewVector = cameraCenter + Vector3(0f, 0f, cameraDistance)
            if (cameraNormal.dot(viewVector) >= 0f) return@forEach

            // Translate points
            val bodyPointsWorld = cubieOutline.map { localP ->
                val faceP = mapToGuideLocalFace(localP, face.localNormal)
                cubie.gridPos + cubie.rightBasis * faceP.x + cubie.upBasis * faceP.y + cubie.forwardBasis * faceP.z
            }

            val stickerPointsWorld = stickerOutline.map { localP ->
                val faceP = mapToGuideLocalFace(localP, face.localNormal)
                cubie.gridPos + cubie.rightBasis * faceP.x + cubie.upBasis * faceP.y + cubie.forwardBasis * faceP.z
            }

            val projectedBody = bodyPointsWorld.map { p ->
                val camPos = p.rotateY(yaw).rotateX(pitch)
                val zDepth = cameraDistance + camPos.z
                val scale = focalLength / zDepth
                Offset(centerX + camPos.x * scale, centerY - camPos.y * scale)
            }

            val projectedSticker = stickerPointsWorld.map { p ->
                val camPos = p.rotateY(yaw).rotateX(pitch)
                val zDepth = cameraDistance + camPos.z
                val scale = focalLength / zDepth
                Offset(centerX + camPos.x * scale, centerY - camPos.y * scale)
            }

            renderFaces.add(
                GuideRenderFace(
                    face = face,
                    projectedBodyPoints = projectedBody,
                    projectedStickerPoints = projectedSticker,
                    depth = cameraCenter.z,
                    worldNormal = worldNormal,
                    isTarget = isTarget
                )
            )
        }
    }

    // Sort by depth
    renderFaces.sortByDescending { it.depth }

    val lightDir = Vector3(1.2f, 1.8f, 2.2f).normalized()
    val viewDir = Vector3(0f, 0f, -1f).rotateX(-pitch).rotateY(-yaw).normalized()
    val halfway = (lightDir + viewDir).normalized()

    // Draw faces
    renderFaces.forEach { rf ->
        val ambient = 0.35f
        val diffuse = rf.worldNormal.dot(lightDir).coerceAtLeast(0f) * 0.50f
        val spec = rf.worldNormal.dot(halfway).coerceAtLeast(0f).pow(24f) * 0.32f
        val totalLight = (ambient + diffuse).coerceIn(0f, 1f)

        // Fade/Desaturate non-target faces to highlight target
        val highlightFactor = if (rf.isTarget) 1.0f else 0.22f
        val targetAlpha = if (rf.isTarget) 1.0f else 0.35f

        // Draw cubie plastic body
        val bodyColor = Color(
            red = ((0.98f * totalLight + spec) * (if (rf.isTarget) 1f else 0.5f)).coerceIn(0f, 1f),
            green = ((0.98f * totalLight + spec) * (if (rf.isTarget) 1f else 0.5f)).coerceIn(0f, 1f),
            blue = ((0.98f * totalLight + spec) * (if (rf.isTarget) 1f else 0.5f)).coerceIn(0f, 1f),
            alpha = targetAlpha
        )
        drawGuidePolygon(projectedPoints = rf.projectedBodyPoints, color = bodyColor)

        // Draw sticker
        val baseColor = rf.face.color
        val r = ((baseColor.rgb shr 16) and 0xFF) / 255f
        val g = ((baseColor.rgb shr 8) and 0xFF) / 255f
        val b = (baseColor.rgb and 0xFF) / 255f

        val stickerColor = Color(
            red = (r * totalLight + spec).coerceIn(0f, 1f),
            green = (g * totalLight + spec).coerceIn(0f, 1f),
            blue = (b * totalLight + spec).coerceIn(0f, 1f),
            alpha = targetAlpha * highlightFactor
        )
        drawGuidePolygon(projectedPoints = rf.projectedStickerPoints, color = stickerColor)
    }
}

private fun DrawScope.drawGuidePolygon(projectedPoints: List<Offset>, color: Color) {
    if (projectedPoints.isEmpty()) return
    val path = Path()
    path.moveTo(projectedPoints[0].x, projectedPoints[0].y)
    for (i in 1 until projectedPoints.size) {
        path.lineTo(projectedPoints[i].x, projectedPoints[i].y)
    }
    path.close()
    drawPath(path, color, style = Fill)
}

private fun getGuideRoundedPoints(size: Float, radius: Float, samples: Int = 3): List<Vector3> {
    val points = mutableListOf<Vector3>()
    val half = size / 2f
    val r = radius.coerceAtMost(half)
    val inner = half - r

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

private fun mapToGuideLocalFace(p: Vector3, localNormal: Vector3): Vector3 {
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
