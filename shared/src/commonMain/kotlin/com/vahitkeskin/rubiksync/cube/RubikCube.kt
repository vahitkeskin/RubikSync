package com.vahitkeskin.rubiksync.cube

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import kotlin.math.PI
import kotlin.math.roundToInt

enum class FaceName { U, D, L, R, F, B }

enum class CubeColor(val rgb: Long) {
    ORANGE(0xFFFF5F00), // Top face - rich orange matching image
    RED(0xFFD6001C),    // Bottom face - classic red
    YELLOW(0xFFFFD500), // Left face - vibrant yellow matching image
    WHITE(0xFFFFFFFF),  // Right face - pure white
    GREEN(0xFF009B48),  // Front face - green matching image
    BLUE(0xFF0046AD),   // Back face - classic blue
    INTERNAL(0xFF1E1E1E) // Inner hidden faces - slate gray/black
}

data class CubieFace(
    val localNormal: Vector3,
    val color: CubeColor
)

class Cubie(
    val id: Int,
    val originalPos: Vector3,
    initialPos: Vector3
) {
    var gridPos by mutableStateOf(initialPos)
    var rightBasis by mutableStateOf(Vector3.UnitX)
    var upBasis by mutableStateOf(Vector3.UnitY)
    var forwardBasis by mutableStateOf(Vector3.UnitZ)

    val faces = listOf(
        // Left Face (-X)
        CubieFace(Vector3(-1f, 0f, 0f), if (originalPos.x < -0.5f) CubeColor.YELLOW else CubeColor.INTERNAL),
        // Right Face (+X)
        CubieFace(Vector3(1f, 0f, 0f), if (originalPos.x > 0.5f) CubeColor.WHITE else CubeColor.INTERNAL),
        // Down Face (-Y)
        CubieFace(Vector3(0f, -1f, 0f), if (originalPos.y < -0.5f) CubeColor.RED else CubeColor.INTERNAL),
        // Up Face (+Y)
        CubieFace(Vector3(0f, 1f, 0f), if (originalPos.y > 0.5f) CubeColor.ORANGE else CubeColor.INTERNAL),
        // Back Face (-Z)
        CubieFace(Vector3(0f, 0f, -1f), if (originalPos.z < -0.5f) CubeColor.BLUE else CubeColor.INTERNAL),
        // Front Face (+Z)
        CubieFace(Vector3(0f, 0f, 1f), if (originalPos.z > 0.5f) CubeColor.GREEN else CubeColor.INTERNAL)
    )

    fun getWorldNormal(face: CubieFace): Vector3 {
        return rightBasis * face.localNormal.x +
                upBasis * face.localNormal.y +
                forwardBasis * face.localNormal.z
    }
}

class ActiveMove(
    val axis: Vector3,
    val layerValue: Int,
    initialAngle: Float,
    val targetAngle: Float
) {
    var currentAngleRad by mutableStateOf(initialAngle)
}

enum class MoveType(val axis: Vector3, val layerValue: Int, val angleSign: Float, val label: String) {
    U(Vector3.UnitY, 1, -1f, "U"),
    U_PRIME(Vector3.UnitY, 1, 1f, "U'"),
    D(Vector3.UnitY, -1, 1f, "D"),
    D_PRIME(Vector3.UnitY, -1, -1f, "D'"),

    R(Vector3.UnitX, 1, -1f, "R"),
    R_PRIME(Vector3.UnitX, 1, 1f, "R'"),
    L(Vector3.UnitX, -1, 1f, "L"),
    L_PRIME(Vector3.UnitX, -1, -1f, "L'"),

    F(Vector3.UnitZ, 1, -1f, "F"),
    F_PRIME(Vector3.UnitZ, 1, 1f, "F'"),
    B(Vector3.UnitZ, -1, 1f, "B"),
    B_PRIME(Vector3.UnitZ, -1, -1f, "B'")
}

class RubikCubeState {
    val cubies: List<Cubie>

    init {
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
        cubies = list
    }

    var currentMove by mutableStateOf<ActiveMove?>(null)
    var rotationSpeedMs by mutableStateOf(250f) // ms per 90 degree turn
    val moveHistory = mutableListOf<MoveType>()

    var isAnimating by mutableStateOf(false)
        private set

    // Rounds coordinates to prevent floating point drift after rotations
    private fun Float.roundToHalfOrWhole(): Float {
        return (this * 2f).roundToInt() / 2f
    }

    private fun Vector3.roundComponents(): Vector3 {
        return Vector3(
            x.roundToHalfOrWhole(),
            y.roundToHalfOrWhole(),
            z.roundToHalfOrWhole()
        )
    }

    fun isCubieInLayer(cubie: Cubie, axis: Vector3, layerValue: Int): Boolean {
        return when {
            axis.x > 0.5f -> cubie.gridPos.x.roundToInt() == layerValue
            axis.y > 0.5f -> cubie.gridPos.y.roundToInt() == layerValue
            axis.z > 0.5f -> cubie.gridPos.z.roundToInt() == layerValue
            else -> false
        }
    }

    suspend fun executeMove(move: MoveType, skipAnimation: Boolean = false) {
        if (isAnimating) return
        isAnimating = true

        if (skipAnimation) {
            applyDiscreteRotation(move)
            moveHistory.add(move)
            isAnimating = false
            return
        }

        val targetAngle = move.angleSign * (PI / 2.0).toFloat()
        val activeMove = ActiveMove(move.axis, move.layerValue, 0f, targetAngle)
        currentMove = activeMove

        val duration = rotationSpeedMs
        val startFrameTime = withFrameMillis { it }
        var elapsed = 0f

        while (elapsed < duration) {
            val currentFrameTime = withFrameMillis { it }
            elapsed = (currentFrameTime - startFrameTime).toFloat()
            val t = (elapsed / duration).coerceIn(0f, 1f)
            // Cubic easing for premium feel
            val easeT = t * t * (3f - 2f * t)
            activeMove.currentAngleRad = targetAngle * easeT
        }

        applyDiscreteRotation(move)
        moveHistory.add(move)
        currentMove = null
        isAnimating = false
    }

    fun applyDiscreteRotation(move: MoveType) {
        val angleRad = move.angleSign * (PI / 2.0).toFloat()
        cubies.forEach { cubie ->
            if (isCubieInLayer(cubie, move.axis, move.layerValue)) {
                // Rotate position
                val rotatedPos = cubie.gridPos.rotateAround(move.axis, angleRad)
                cubie.gridPos = rotatedPos.roundComponents()

                // Rotate orientation bases
                cubie.rightBasis = cubie.rightBasis.rotateAround(move.axis, angleRad).roundComponents()
                cubie.upBasis = cubie.upBasis.rotateAround(move.axis, angleRad).roundComponents()
                cubie.forwardBasis = cubie.forwardBasis.rotateAround(move.axis, angleRad).roundComponents()
            }
        }
    }

    suspend fun scramble(turns: Int = 20) {
        if (isAnimating) return
        // Scramble with slightly faster speed for visual pacing
        val originalSpeed = rotationSpeedMs
        rotationSpeedMs = 120f

        val moves = MoveType.values()
        var lastMove: MoveType? = null

        repeat(turns) {
            // Pick a move that doesn't immediately undo the previous one
            var move = moves.random()
            while (lastMove != null && move.axis == lastMove.axis && move.layerValue == lastMove.layerValue && move.angleSign == -lastMove.angleSign) {
                move = moves.random()
            }
            executeMove(move)
            lastMove = move
        }

        rotationSpeedMs = originalSpeed
    }

    suspend fun undo() {
        if (isAnimating || moveHistory.isEmpty()) return
        val lastMove = moveHistory.removeLast()
        
        // The inverse of a move has the opposite angleSign
        val inverseMove = MoveType.values().first {
            it.axis == lastMove.axis &&
            it.layerValue == lastMove.layerValue &&
            it.angleSign == -lastMove.angleSign
        }
        
        // Remove the inverse move from history since executeMove will add it
        executeMove(inverseMove)
        moveHistory.removeLast() // pop the inverseMove we just added
    }

    fun setCustomState(faces: Map<FaceName, Array<Array<CubeColor>>>): Boolean {
        if (isAnimating) return false
        
        fun getFaceletColor(pos: Vector3, normal: Vector3): CubeColor {
            val px = pos.x.roundToInt()
            val py = pos.y.roundToInt()
            val pz = pos.z.roundToInt()
            val nx = normal.x.roundToInt()
            val ny = normal.y.roundToInt()
            val nz = normal.z.roundToInt()
            
            return when {
                ny == 1 -> faces[FaceName.U]!![pz + 1][px + 1]
                ny == -1 -> faces[FaceName.D]!![1 - pz][px + 1]
                nx == -1 -> faces[FaceName.L]!![1 - py][pz + 1]
                nx == 1 -> faces[FaceName.R]!![1 - py][1 - pz]
                nz == 1 -> faces[FaceName.F]!![1 - py][px + 1]
                nz == -1 -> faces[FaceName.B]!![1 - py][1 - px]
                else -> CubeColor.INTERNAL
            }
        }

        val matchedCubieIds = mutableSetOf<Int>()
        val targetStates = mutableListOf<Pair<Cubie, Vector3>>()
        val targetBases = mutableListOf<Triple<Vector3, Vector3, Vector3>>()
        
        val normals = listOf(
            Vector3(-1f, 0f, 0f), Vector3(1f, 0f, 0f),
            Vector3(0f, -1f, 0f), Vector3(0f, 1f, 0f),
            Vector3(0f, 0f, -1f), Vector3(0f, 0f, 1f)
        )

        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    if (x == 0 && y == 0 && z == 0) continue
                    
                    val gridPos = Vector3(x.toFloat(), y.toFloat(), z.toFloat())
                    val colorDirections = mutableMapOf<CubeColor, Vector3>()
                    
                    normals.forEach { normal ->
                        val isOutward = (normal.x < -0.5f && x == -1) ||
                                        (normal.x > 0.5f && x == 1) ||
                                        (normal.y < -0.5f && y == -1) ||
                                        (normal.y > 0.5f && y == 1) ||
                                        (normal.z < -0.5f && z == -1) ||
                                        (normal.z > 0.5f && z == 1)
                        if (isOutward) {
                            val color = getFaceletColor(gridPos, normal)
                            if (color != CubeColor.INTERNAL) {
                                colorDirections[color] = normal
                            }
                        }
                    }
                    
                    val requiredColors = colorDirections.keys
                    val matchingCubie = cubies.find { cubie ->
                        val origColors = cubie.faces
                            .map { it.color }
                            .filter { it != CubeColor.INTERNAL }
                            .toSet()
                        origColors == requiredColors
                    }
                    
                    if (matchingCubie == null || matchingCubie.id in matchedCubieIds) {
                        return false
                    }
                    
                    matchedCubieIds.add(matchingCubie.id)
                    targetStates.add(matchingCubie to gridPos)
                    
                    var right = Vector3.UnitX
                    var up = Vector3.UnitY
                    var forward = Vector3.UnitZ
                    var rightSet = false
                    var upSet = false
                    var forwardSet = false
                    
                    matchingCubie.faces.forEach { face ->
                        if (face.color == CubeColor.INTERNAL) return@forEach
                        val targetDir = colorDirections[face.color] ?: return@forEach
                        
                        when {
                            face.localNormal.x > 0.5f -> { right = targetDir; rightSet = true }
                            face.localNormal.x < -0.5f -> { right = targetDir * -1f; rightSet = true }
                            face.localNormal.y > 0.5f -> { up = targetDir; upSet = true }
                            face.localNormal.y < -0.5f -> { up = targetDir * -1f; upSet = true }
                            face.localNormal.z > 0.5f -> { forward = targetDir; forwardSet = true }
                            face.localNormal.z < -0.5f -> { forward = targetDir * -1f; forwardSet = true }
                        }
                    }
                    
                    if (rightSet && upSet && !forwardSet) {
                        forward = right.cross(up)
                    } else if (rightSet && !upSet && forwardSet) {
                        up = forward.cross(right)
                    } else if (!rightSet && upSet && forwardSet) {
                        right = up.cross(forward)
                    }
                    
                    targetBases.add(Triple(right, up, forward))
                }
            }
        }
        
        targetStates.forEachIndexed { i, (cubie, gridPos) ->
            cubie.gridPos = gridPos
            val (r, u, f) = targetBases[i]
            cubie.rightBasis = r
            cubie.upBasis = u
            cubie.forwardBasis = f
        }
        
        moveHistory.clear()
        currentMove = null
        return true
    }

    fun reset() {
        if (isAnimating) return
        currentMove = null
        moveHistory.clear()
        cubies.forEach { cubie ->
            cubie.gridPos = cubie.originalPos
            cubie.rightBasis = Vector3.UnitX
            cubie.upBasis = Vector3.UnitY
            cubie.forwardBasis = Vector3.UnitZ
        }
    }
}
