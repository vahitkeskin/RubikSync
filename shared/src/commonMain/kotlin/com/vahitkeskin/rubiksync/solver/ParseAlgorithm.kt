package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType

fun parseAlgorithm(alg: String): List<MoveType> {
    return alg.split(" ").filter { it.isNotEmpty() }.flatMap { step ->
        when (step) {
            "U" -> listOf(MoveType.U)
            "U'" -> listOf(MoveType.U_PRIME)
            "D" -> listOf(MoveType.D)
            "D'" -> listOf(MoveType.D_PRIME)
            "R" -> listOf(MoveType.R)
            "R'" -> listOf(MoveType.R_PRIME)
            "L" -> listOf(MoveType.L)
            "L'" -> listOf(MoveType.L_PRIME)
            "F" -> listOf(MoveType.F)
            "F'" -> listOf(MoveType.F_PRIME)
            "B" -> listOf(MoveType.B)
            "B'" -> listOf(MoveType.B_PRIME)
            "U2" -> listOf(MoveType.U, MoveType.U)
            "D2" -> listOf(MoveType.D, MoveType.D)
            "R2" -> listOf(MoveType.R, MoveType.R)
            "L2" -> listOf(MoveType.L, MoveType.L)
            "F2" -> listOf(MoveType.F, MoveType.F)
            "B2" -> listOf(MoveType.B, MoveType.B)
            else -> throw IllegalArgumentException("Unknown step: $step")
        }
    }
}
