package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType

fun compressMoves(moves: List<MoveType>, maxPasses: Int = 3): List<MoveType> {
    return optimizeMoves(moves)
}
