package com.vahitkeskin.rubiksync.solver

data class IntVector3(val x: Int, val y: Int, val z: Int) {
    private val cachedHashCode = 31 * (31 * x + y) + z
    override fun hashCode(): Int = cachedHashCode

    operator fun plus(other: IntVector3) = IntVector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: IntVector3) = IntVector3(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Int) = IntVector3(x * factor, y * factor, z * factor)

    fun dot(other: IntVector3) = x * other.x + y * other.y + z * other.z
    fun cross(other: IntVector3) = IntVector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
}
