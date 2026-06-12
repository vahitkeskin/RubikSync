package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType

fun bfs(
    start: CubeSnapshot,
    allowedMoves: List<MoveType>,
    macroMoves: List<List<MoveType>>,
    activeIds: List<Int>,
    maxDepth: Int = 6,
    isGoal: (CubeSnapshot) -> Boolean
): Pair<CubeSnapshot, List<MoveType>>? {
    if (isGoal(start)) return start to emptyList()

    val queue = ArrayDeque<Triple<CubeSnapshot, List<MoveType>, Int>>()
    val visited = mutableSetOf<ActiveState>()

    val startActive = start.toActiveState(activeIds)
    queue.add(Triple(start, emptyList(), 0))
    visited.add(startActive)

    while (queue.isNotEmpty()) {
        if (visited.size > 50000) return null
        val (state, path, depth) = queue.removeAt(0)

        if (depth >= maxDepth) continue

        // Try single moves
        for (move in allowedMoves) {
            if (path.isNotEmpty()) {
                val last = path.last()
                if (last.axis == move.axis && last.layerValue == move.layerValue && last.angleSign == -move.angleSign) {
                    continue
                }
                if (path.size >= 2) {
                    val secondLast = path[path.size - 2]
                    if (last == move && secondLast == move) {
                        continue
                    }
                }
            }

            val nextState = state.applyMove(move)
            val nextActive = nextState.toActiveState(activeIds)
            if (nextActive !in visited) {
                val nextPath = path + move
                if (isGoal(nextState)) {
                    return nextState to nextPath
                }
                visited.add(nextActive)
                queue.add(Triple(nextState, nextPath, depth + 1))
            }
        }

        // Try macro moves
        for (macro in macroMoves) {
            if (macro.isEmpty()) continue

            if (path.isNotEmpty()) {
                val last = path.last()
                val firstOfMacro = macro.first()
                if (last.axis == firstOfMacro.axis && last.layerValue == firstOfMacro.layerValue && last.angleSign == -firstOfMacro.angleSign) {
                    continue
                }
            }

            var nextState = state
            for (move in macro) {
                nextState = nextState.applyMove(move)
            }
            val nextActive = nextState.toActiveState(activeIds)
            if (nextActive !in visited) {
                val nextPath = path + macro
                if (isGoal(nextState)) {
                    return nextState to nextPath
                }
                visited.add(nextActive)
                queue.add(Triple(nextState, nextPath, depth + 1))
            }
        }
    }
    return null
}
