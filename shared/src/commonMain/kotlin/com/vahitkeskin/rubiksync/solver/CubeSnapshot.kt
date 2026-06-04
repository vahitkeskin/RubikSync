package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.Vector3
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import kotlin.math.roundToInt

class CubeSnapshot(
    val stateArray: IntArray
) {
    private val cachedHashCode = stateArray.contentHashCode()

    override fun hashCode() = cachedHashCode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CubeSnapshot) return false
        if (cachedHashCode != other.cachedHashCode) return false
        return stateArray.contentEquals(other.stateArray)
    }

    fun applyMove(move: MoveType): CubeSnapshot {
        val nextArray = stateArray.copyOf()
        val axis = move.axis
        val layerValue = move.layerValue
        val angleSign = move.angleSign.roundToInt()

        for (i in 0 until 27) {
            val offset = i * 12
            val gx = nextArray[offset]
            val gy = nextArray[offset + 1]
            val gz = nextArray[offset + 2]

            val inLayer = when {
                axis.x > 0.5f -> gx == layerValue
                axis.y > 0.5f -> gy == layerValue
                axis.z > 0.5f -> gz == layerValue
                else -> false
            }

            if (inLayer) {
                // Rotate position
                val (rx, ry, rz) = rotateIntVector(gx, gy, gz, axis, angleSign)
                nextArray[offset] = rx
                nextArray[offset + 1] = ry
                nextArray[offset + 2] = rz

                // Rotate rightBasis
                val (rbx, rby, rbz) = rotateIntVector(nextArray[offset + 3], nextArray[offset + 4], nextArray[offset + 5], axis, angleSign)
                nextArray[offset + 3] = rbx
                nextArray[offset + 4] = rby
                nextArray[offset + 5] = rbz

                // Rotate upBasis
                val (ubx, uby, ubz) = rotateIntVector(nextArray[offset + 6], nextArray[offset + 7], nextArray[offset + 8], axis, angleSign)
                nextArray[offset + 6] = ubx
                nextArray[offset + 7] = uby
                nextArray[offset + 8] = ubz

                // Rotate forwardBasis
                val (fbx, fby, fbz) = rotateIntVector(nextArray[offset + 9], nextArray[offset + 10], nextArray[offset + 11], axis, angleSign)
                nextArray[offset + 9] = fbx
                nextArray[offset + 10] = fby
                nextArray[offset + 11] = fbz
            }
        }
        return CubeSnapshot(nextArray)
    }

    private fun rotateIntVector(x: Int, y: Int, z: Int, axis: Vector3, s: Int): Triple<Int, Int, Int> {
        return when {
            axis.x > 0.5f -> Triple(x, -z * s, y * s)
            axis.y > 0.5f -> Triple(z * s, y, -x * s)
            axis.z > 0.5f -> Triple(-y * s, x * s, z)
            else -> Triple(x, y, z)
        }
    }
}

fun RubikCubeState.toSnapshot(): CubeSnapshot {
    val arr = IntArray(27 * 12)
    var idx = 0
    for (cubie in this.cubies) {
        arr[idx++] = cubie.gridPos.x.roundToInt()
        arr[idx++] = cubie.gridPos.y.roundToInt()
        arr[idx++] = cubie.gridPos.z.roundToInt()
        arr[idx++] = cubie.rightBasis.x.roundToInt()
        arr[idx++] = cubie.rightBasis.y.roundToInt()
        arr[idx++] = cubie.rightBasis.z.roundToInt()
        arr[idx++] = cubie.upBasis.x.roundToInt()
        arr[idx++] = cubie.upBasis.y.roundToInt()
        arr[idx++] = cubie.upBasis.z.roundToInt()
        arr[idx++] = cubie.forwardBasis.x.roundToInt()
        arr[idx++] = cubie.forwardBasis.y.roundToInt()
        arr[idx++] = cubie.forwardBasis.z.roundToInt()
    }
    return CubeSnapshot(arr)
}
