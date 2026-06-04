package com.vahitkeskin.rubiksync.solver

fun compressAnnotatedMoves(moves: List<AnnotatedMove>, maxPasses: Int = 3): List<AnnotatedMove> {
    return optimizeAnnotatedMoves(moves)
}
