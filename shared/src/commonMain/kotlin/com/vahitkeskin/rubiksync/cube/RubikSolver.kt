package com.vahitkeskin.rubiksync.cube

import kotlin.math.roundToInt

data class IntVector3(val x: Int, val y: Int, val z: Int) {
    operator fun plus(other: IntVector3) = IntVector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: IntVector3) = IntVector3(x - other.x, y - other.y, z - other.z)
    operator fun times(factor: Int) = IntVector3(x * factor, y * factor, z * factor)

    fun dot(other: IntVector3) = x * other.x + y * other.y + z * other.z
    fun cross(other: IntVector3) = IntVector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun rotate(axis: Vector3, angleSign: Float): IntVector3 {
        val s = angleSign.roundToInt()
        return when {
            axis.x > 0.5f -> IntVector3(x, -z * s, y * s)
            axis.y > 0.5f -> IntVector3(z * s, y, -x * s)
            axis.z > 0.5f -> IntVector3(-y * s, x * s, z)
            else -> this
        }
    }
}

data class CubieSnapshot(
    val id: Int,
    val originalPos: IntVector3,
    val gridPos: IntVector3,
    val rightBasis: IntVector3,
    val upBasis: IntVector3,
    val forwardBasis: IntVector3
) {
    fun rotate(axis: Vector3, angleSign: Float): CubieSnapshot {
        return CubieSnapshot(
            id = id,
            originalPos = originalPos,
            gridPos = gridPos.rotate(axis, angleSign),
            rightBasis = rightBasis.rotate(axis, angleSign),
            upBasis = upBasis.rotate(axis, angleSign),
            forwardBasis = forwardBasis.rotate(axis, angleSign)
        )
    }
}

data class CubeSnapshot(
    val cubies: List<CubieSnapshot>
) {
    fun applyMove(move: MoveType): CubeSnapshot {
        return CubeSnapshot(
            cubies.map { cubie ->
                if (isCubieInLayer(cubie.gridPos, move.axis, move.layerValue)) {
                    cubie.rotate(move.axis, move.angleSign)
                } else {
                    cubie
                }
            }
        )
    }

    private fun isCubieInLayer(gridPos: IntVector3, axis: Vector3, layerValue: Int): Boolean {
        return when {
            axis.x > 0.5f -> gridPos.x == layerValue
            axis.y > 0.5f -> gridPos.y == layerValue
            axis.z > 0.5f -> gridPos.z == layerValue
            else -> false
        }
    }
}

fun RubikCubeState.toSnapshot(): CubeSnapshot {
    return CubeSnapshot(
        cubies = this.cubies.map { cubie ->
            CubieSnapshot(
                id = cubie.id,
                originalPos = IntVector3(cubie.originalPos.x.roundToInt(), cubie.originalPos.y.roundToInt(), cubie.originalPos.z.roundToInt()),
                gridPos = IntVector3(cubie.gridPos.x.roundToInt(), cubie.gridPos.y.roundToInt(), cubie.gridPos.z.roundToInt()),
                rightBasis = IntVector3(cubie.rightBasis.x.roundToInt(), cubie.rightBasis.y.roundToInt(), cubie.rightBasis.z.roundToInt()),
                upBasis = IntVector3(cubie.upBasis.x.roundToInt(), cubie.upBasis.y.roundToInt(), cubie.upBasis.z.roundToInt()),
                forwardBasis = IntVector3(cubie.forwardBasis.x.roundToInt(), cubie.forwardBasis.y.roundToInt(), cubie.forwardBasis.z.roundToInt())
            )
        }
    )
}

class RubikSolver {

    // Runs a BFS to solve the next sub-goal
    private fun bfs(
        start: CubeSnapshot,
        allowedMoves: List<MoveType>,
        macroMoves: List<List<MoveType>>,
        isGoal: (CubeSnapshot) -> Boolean
    ): Pair<CubeSnapshot, List<MoveType>>? {
        if (isGoal(start)) return start to emptyList()

        val queue = ArrayDeque<Pair<CubeSnapshot, List<MoveType>>>()
        val visited = mutableSetOf<CubeSnapshot>()

        queue.add(start to emptyList())
        visited.add(start)

        while (queue.isNotEmpty()) {
            val (state, path) = queue.removeFirst()

            // Try single moves
            for (move in allowedMoves) {
                val nextState = state.applyMove(move)
                if (nextState !in visited) {
                    val nextPath = path + move
                    if (isGoal(nextState)) {
                        return nextState to nextPath
                    }
                    if (nextPath.size < 5) {
                        visited.add(nextState)
                        queue.add(nextState to nextPath)
                    }
                }
            }

            // Try macro moves
            for (macro in macroMoves) {
                var nextState = state
                for (move in macro) {
                    nextState = nextState.applyMove(move)
                }
                if (nextState !in visited) {
                    val nextPath = path + macro
                    if (isGoal(nextState)) {
                        return nextState to nextPath
                    }
                    if (nextPath.size < 6) {
                        visited.add(nextState)
                        queue.add(nextState to nextPath)
                    }
                }
            }
        }
        return null
    }

    private fun parseAlgorithm(alg: String): List<MoveType> {
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

    private fun isCubieSolved(snap: CubeSnapshot, origPos: IntVector3): Boolean {
        val c = snap.cubies.find { it.originalPos == origPos } ?: return false
        return c.gridPos == origPos &&
                c.rightBasis == IntVector3(1, 0, 0) &&
                c.upBasis == IntVector3(0, 1, 0) &&
                c.forwardBasis == IntVector3(0, 0, 1)
    }

    fun solve(startState: RubikCubeState): List<MoveType>? {
        var state = startState.toSnapshot()
        val allMoves = mutableListOf<MoveType>()

        // Step 1: Red Cross (Bottom Edges)
        val redEdges = listOf(
            IntVector3(0, -1, 1),  // Green-Red
            IntVector3(1, -1, 0),  // White-Red
            IntVector3(0, -1, -1), // Blue-Red
            IntVector3(-1, -1, 0)  // Yellow-Red
        )
        
        val basicMoves = MoveType.values().toList()

        for (i in redEdges.indices) {
            val goalEdges = redEdges.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = basicMoves,
                macroMoves = emptyList(),
                isGoal = { snap -> goalEdges.all { isCubieSolved(snap, it) } }
            ) ?: return null
            state = result.first
            allMoves.addAll(result.second)
        }

        // Step 2: Red Corners (Bottom Corners)
        val redCorners = listOf(
            IntVector3(1, -1, 1),   // Green-White-Red
            IntVector3(1, -1, -1),  // White-Blue-Red
            IntVector3(-1, -1, -1), // Blue-Yellow-Red
            IntVector3(-1, -1, 1)   // Yellow-Green-Red
        )

        // Restrict moves to not disrupt bottom cross permanent centers
        val crossPreservingMoves = listOf(
            MoveType.U, MoveType.U_PRIME,
            MoveType.R, MoveType.R_PRIME,
            MoveType.L, MoveType.L_PRIME,
            MoveType.F, MoveType.F_PRIME,
            MoveType.B, MoveType.B_PRIME
        )

        for (i in redCorners.indices) {
            val goalCorners = redCorners.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = crossPreservingMoves,
                macroMoves = emptyList(),
                isGoal = { snap ->
                    redEdges.all { isCubieSolved(snap, it) } && goalCorners.all { isCubieSolved(snap, it) }
                }
            ) ?: return null
            state = result.first
            allMoves.addAll(result.second)
        }

        // Step 3: Middle Layer Edges
        val middleEdges = listOf(
            IntVector3(1, 0, 1),   // Green-White
            IntVector3(1, 0, -1),  // White-Blue
            IntVector3(-1, 0, -1), // Blue-Yellow
            IntVector3(-1, 0, 1)   // Yellow-Green
        )

        // Use macro algorithms for middle layer to keep search shallow
        val middleMacros = listOf(
            "U R U' R' U' F' U F", "U' F' U F U R U' R'",
            "U B U' B' U' R' U R", "U' R' U R U B U' B'",
            "U L U' L' U' B' U B", "U' B' U B U L U' L'",
            "U F U' F' U' L' U L", "U' L' U L U F U' F'"
        ).map { parseAlgorithm(it) }

        for (i in middleEdges.indices) {
            val goalEdges = middleEdges.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
                macroMoves = middleMacros,
                isGoal = { snap ->
                    redEdges.all { isCubieSolved(snap, it) } &&
                    redCorners.all { isCubieSolved(snap, it) } &&
                    goalEdges.all { isCubieSolved(snap, it) }
                }
            ) ?: return null
            state = result.first
            allMoves.addAll(result.second)
        }

        // Step 4: Yellow/Orange Cross (OLL Edges Orientation)
        val topEdges = listOf(
            IntVector3(0, 1, 1),
            IntVector3(1, 1, 0),
            IntVector3(0, 1, -1),
            IntVector3(-1, 1, 0)
        )

        val ollMacros = listOf(
            "F R U R' U' F'",
            "F U R U' R' F'"
        ).map { parseAlgorithm(it) }

        val ollResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = ollMacros,
            isGoal = { snap ->
                // Check that first two layers are intact
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                // Top edges oriented (Orange pointing UP, i.e. upBasis.y == 1)
                topEdges.all { pos ->
                    val c = snap.cubies.find { it.gridPos == pos }!!
                    c.upBasis.y == 1
                }
            }
        ) ?: return null
        state = ollResult.first
        allMoves.addAll(ollResult.second)

        // Step 5: Yellow/Orange Cross Permutation (Edges Position)
        val pllEdgeMacros = listOf(
            "R U R' U R U2 R'",
            "R U2 R' U' R U' R'"
        ).map { parseAlgorithm(it) }

        val pllEdgesResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllEdgeMacros,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                // Top edges fully solved
                topEdges.all { isCubieSolved(snap, it) }
            }
        ) ?: return null
        state = pllEdgesResult.first
        allMoves.addAll(pllEdgesResult.second)

        // Step 6: Yellow/Orange Corners Positioning (Corners Position)
        val topCorners = listOf(
            IntVector3(1, 1, 1),
            IntVector3(1, 1, -1),
            IntVector3(-1, 1, -1),
            IntVector3(-1, 1, 1)
        )

        val pllCornerMacros = listOf(
            "U R U' L' U R' U' L",
            "U' L' U R U' L U R'"
        ).map { parseAlgorithm(it) }

        val pllCornersResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllCornerMacros,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { isCubieSolved(snap, it) } &&
                // Top corners are in their correct positions (even if twisted)
                topCorners.all { pos ->
                    val c = snap.cubies.find { it.gridPos == pos }!!
                    c.originalPos == pos
                }
            }
        ) ?: return null
        state = pllCornersResult.first
        allMoves.addAll(pllCornersResult.second)

        // Step 7: Yellow/Orange Corners Orientation (Solve Cube)
        // Deterministic orientation algorithm
        val movesList = mutableListOf<MoveType>()
        var finalState = state
        
        for (i in 0..3) {
            val cornerId = finalState.cubies.find { it.gridPos == IntVector3(1, 1, 1) }!!.id
            while (true) {
                val c = finalState.cubies.find { it.id == cornerId }!!
                if (c.rightBasis == IntVector3(1, 0, 0) && c.upBasis == IntVector3(0, 1, 0)) {
                    break
                }
                
                // Apply R' D' R D
                val stepMoves = parseAlgorithm("R' D' R D")
                for (move in stepMoves) {
                    finalState = finalState.applyMove(move)
                    movesList.add(move)
                }
            }
            if (i < 3) {
                finalState = finalState.applyMove(MoveType.U)
                movesList.add(MoveType.U)
            }
        }

        // Align top layer
        while (true) {
            val testEdge = finalState.cubies.find { it.originalPos == IntVector3(0, 1, 1) }!!
            if (testEdge.gridPos == IntVector3(0, 1, 1)) {
                break
            }
            finalState = finalState.applyMove(MoveType.U)
            movesList.add(MoveType.U)
        }

        allMoves.addAll(movesList)

        // Optimize move sequence (remove redundant moves like U U U -> U', U U' -> cancel, etc.)
        return optimizeMoves(allMoves)
    }

    private fun optimizeMoves(moves: List<MoveType>): List<MoveType> {
        val list = moves.toMutableList()
        var changed = true
        while (changed) {
            changed = false
            var i = 0
            while (i < list.size - 1) {
                val m1 = list[i]
                val m2 = list[i + 1]
                
                // Opposite moves cancel out: e.g. R R' -> nothing
                if (m1.axis == m2.axis && m1.layerValue == m2.layerValue && m1.angleSign == -m2.angleSign) {
                    list.removeAt(i + 1)
                    list.removeAt(i)
                    changed = true
                    break
                }
                
                // Same moves merge: e.g. U U -> U2 (represented as U, U in our MoveType)
                // If there are three same moves, they merge to the inverse: e.g. U U U -> U'
                if (i < list.size - 2) {
                    val m3 = list[i + 2]
                    if (m1 == m2 && m2 == m3) {
                        val inverse = MoveType.values().first {
                            it.axis == m1.axis && it.layerValue == m1.layerValue && it.angleSign == -m1.angleSign
                        }
                        list.removeAt(i + 2)
                        list.removeAt(i + 1)
                        list[i] = inverse
                        changed = true
                        break
                    }
                }
                
                i++
            }
        }
        return list
    }
}
