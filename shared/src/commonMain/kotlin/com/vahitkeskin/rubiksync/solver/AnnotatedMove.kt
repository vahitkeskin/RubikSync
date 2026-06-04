package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType

data class AnnotatedMove(
    val move: MoveType,
    val phaseName: String,
    val phaseDescription: String
)
