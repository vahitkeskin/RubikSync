package com.vahitkeskin.rubiksync.cube

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import kotlin.math.PI
import kotlin.math.roundToInt

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

    private fun applyDiscreteRotation(move: MoveType) {
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
