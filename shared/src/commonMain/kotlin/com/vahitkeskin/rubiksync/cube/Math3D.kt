package com.vahitkeskin.rubiksync.cube

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Float) = Vector3(x * factor, y * factor, z * factor)
    operator fun div(factor: Float) = Vector3(x / factor, y / factor, z / factor)

    fun length() = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.0001f) this / len else this
    }

    fun dot(other: Vector3) = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3) = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    // Rotates this vector around an arbitrary axis by an angle (in radians)
    // Uses Rodrigues' rotation formula
    fun rotateAround(axis: Vector3, angleRad: Float): Vector3 {
        val u = axis.normalized()
        val cosTheta = cos(angleRad)
        val sinTheta = sin(angleRad)

        // v * cos(theta)
        val term1 = this * cosTheta
        // (u x v) * sin(theta)
        val term2 = u.cross(this) * sinTheta
        // u * (u . v) * (1 - cos(theta))
        val term3 = u * (u.dot(this) * (1f - cosTheta))

        return term1 + term2 + term3
    }

    fun rotateX(angleRad: Float): Vector3 {
        val cosT = cos(angleRad)
        val sinT = sin(angleRad)
        return Vector3(x, y * cosT - z * sinT, y * sinT + z * cosT)
    }

    fun rotateY(angleRad: Float): Vector3 {
        val cosT = cos(angleRad)
        val sinT = sin(angleRad)
        return Vector3(x * cosT + z * sinT, y, -x * sinT + z * cosT)
    }

    fun rotateZ(angleRad: Float): Vector3 {
        val cosT = cos(angleRad)
        val sinT = sin(angleRad)
        return Vector3(x * cosT - y * sinT, x * sinT + y * cosT, z)
    }

    companion object {
        val Zero = Vector3(0f, 0f, 0f)
        val UnitX = Vector3(1f, 0f, 0f)
        val UnitY = Vector3(0f, 1f, 0f)
        val UnitZ = Vector3(0f, 0f, 1f)
    }
}
