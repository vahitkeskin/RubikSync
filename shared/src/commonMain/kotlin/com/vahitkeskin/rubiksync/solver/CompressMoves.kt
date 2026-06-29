package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType

fun compressMoves(moves: List<MoveType>): List<MoveType> {
    return optimizeMoves(moves)
}
