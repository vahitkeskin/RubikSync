package com.vahitkeskin.rubiksync.cube

import kotlin.math.roundToInt

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
        val nextArray = stateArray.clone()
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

class ActiveState(
    val positionsAndBases: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ActiveState) return false
        return positionsAndBases.contentEquals(other.positionsAndBases)
    }

    override fun hashCode(): Int {
        return positionsAndBases.contentHashCode()
    }
}

fun CubeSnapshot.toActiveState(activeIds: List<Int>): ActiveState {
    val arr = IntArray(activeIds.size * 12)
    var idx = 0
    for (id in activeIds) {
        val offset = id * 12
        System.arraycopy(stateArray, offset, arr, idx, 12)
        idx += 12
    }
    return ActiveState(arr)
}

class RubikSolver {

    private fun bfs(
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
            val (state, path, depth) = queue.removeFirst()

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

    private fun getCubieIdFromOriginalPos(pos: IntVector3): Int {
        val x = pos.x + 1
        val y = pos.y + 1
        val z = pos.z + 1
        return x * 9 + y * 3 + z
    }

    private fun isCubieSolved(snap: CubeSnapshot, origPos: IntVector3): Boolean {
        val id = getCubieIdFromOriginalPos(origPos)
        val offset = id * 12
        return snap.stateArray[offset] == origPos.x &&
                snap.stateArray[offset + 1] == origPos.y &&
                snap.stateArray[offset + 2] == origPos.z &&
                snap.stateArray[offset + 3] == 1 && snap.stateArray[offset + 4] == 0 && snap.stateArray[offset + 5] == 0 &&
                snap.stateArray[offset + 6] == 0 && snap.stateArray[offset + 7] == 1 && snap.stateArray[offset + 8] == 0 &&
                snap.stateArray[offset + 9] == 0 && snap.stateArray[offset + 10] == 0 && snap.stateArray[offset + 11] == 1
    }

    private fun findId(snap: CubeSnapshot, pos: IntVector3): Int {
        return getCubieIdFromOriginalPos(pos)
    }

    fun solve(startState: RubikCubeState): List<MoveType>? {
        return solve(startState.toSnapshot())
    }

    fun solve(startSnapshot: CubeSnapshot): List<MoveType>? {
        var state = startSnapshot
        val allMoves = mutableListOf<MoveType>()

        // Step 1: Red Cross (Bottom Edges)
        val redEdges = listOf(
            IntVector3(0, -1, 1),  // Green-Red
            IntVector3(1, -1, 0),  // White-Red
            IntVector3(0, -1, -1), // Blue-Red
            IntVector3(-1, -1, 0)  // Yellow-Red
        )

        val redEdgeIds = redEdges.map { findId(state, it) }
        val basicMoves = MoveType.values().toList()

        for (i in redEdges.indices) {
            val goalEdges = redEdges.take(i + 1)
            val activeIds = redEdgeIds.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = basicMoves,
                macroMoves = emptyList(),
                activeIds = activeIds,
                maxDepth = 6,
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

        val redCornerIds = redCorners.map { findId(state, it) }

        val crossPreservingMoves = listOf(
            MoveType.U, MoveType.U_PRIME,
            MoveType.R, MoveType.R_PRIME,
            MoveType.L, MoveType.L_PRIME,
            MoveType.F, MoveType.F_PRIME,
            MoveType.B, MoveType.B_PRIME
        )

        val cornerMacros = listOf(
            "R U R'", "R U' R'", "R U2 R'",
            "R' U R", "R' U' R", "R' U2 R",
            "L U L'", "L U' L'", "L U2 L'",
            "L' U L", "L' U' L", "L' U2 L",
            "F U F'", "F U' F'", "F U2 F'",
            "F' U F", "F' U' F", "F' U2 F",
            "B U B'", "B U' B'", "B U2 B'",
            "B' U B", "B' U' B", "B' U2 B"
        ).map { parseAlgorithm(it) }

        for (i in redCorners.indices) {
            val goalCorners = redCorners.take(i + 1)
            val activeIds = redEdgeIds + redCornerIds.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = crossPreservingMoves,
                macroMoves = cornerMacros,
                activeIds = activeIds,
                maxDepth = 4,
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

        val middleEdgeIds = middleEdges.map { findId(state, it) }

        val middleMacros = listOf(
            "U R U' R' U' F' U F", "U' F' U F U R U' R'",
            "U B U' B' U' R' U R", "U' R' U R U B U' B'",
            "U L U' L' U' B' U B", "U' B' U B U L U' L'",
            "U F U' F' U' L' U L", "U' L' U L U F U' F'"
        ).map { parseAlgorithm(it) }

        for (i in middleEdges.indices) {
            val goalEdges = middleEdges.take(i + 1)
            val activeIds = redEdgeIds + redCornerIds + middleEdgeIds.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
                macroMoves = middleMacros,
                activeIds = activeIds,
                maxDepth = 4,
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

        val topEdgeIds = topEdges.map { findId(state, it) }

        val ollMacros = listOf(
            "F R U R' U' F'",
            "F U R U' R' F'"
        ).map { parseAlgorithm(it) }

        val ollResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = ollMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { pos ->
                    val id = getCubieIdFromOriginalPos(pos)
                    snap.stateArray[id * 12 + 7] == 1
                }
            }
        ) ?: return null
        state = ollResult.first
        allMoves.addAll(ollResult.second)

        // Step 5: Yellow/Orange Cross Permutation (Edges Position)
        val pllEdgeMacros = listOf(
            "R U R' U R U2 R'", "R U2 R' U' R U' R'",
            "L' U' L U' L' U2 L", "L' U2 L U L' U L",
            "F U F' U F U2 F'", "F U2 F' U' F U' F'",
            "B' U' B U' B' U2 B", "B' U2 B U B' U B"
        ).map { parseAlgorithm(it) }

        val pllEdgesResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllEdgeMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
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

        val topCornerIds = topCorners.map { findId(state, it) }

        val pllCornerMacros = listOf(
            "U R U' L' U R' U' L", "U' L' U R U' L U R'",
            "U F U' B' U F' U' B", "U' B' U F U' B U F'",
            "U L U' R' U L' U' R", "U' R' U L U' R U L'"
        ).map { parseAlgorithm(it) }

        val pllCornersResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllCornerMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds + topCornerIds,
            maxDepth = 6,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { isCubieSolved(snap, it) } &&
                topCorners.all { pos ->
                    val id = getCubieIdFromOriginalPos(pos)
                    snap.stateArray[id * 12] == pos.x &&
                    snap.stateArray[id * 12 + 1] == pos.y &&
                    snap.stateArray[id * 12 + 2] == pos.z
                }
            }
        ) ?: return null
        state = pllCornersResult.first
        allMoves.addAll(pllCornersResult.second)

        // Step 7: Yellow/Orange Corners Orientation (Solve Cube)
        val movesList = mutableListOf<MoveType>()
        var finalState = state
        
        for (i in 0..3) {
            var cornerId = -1
            for (id in 0 until 27) {
                val offset = id * 12
                if (finalState.stateArray[offset] == 1 &&
                    finalState.stateArray[offset + 1] == 1 &&
                    finalState.stateArray[offset + 2] == 1) {
                    cornerId = id
                    break
                }
            }
            if (cornerId == -1) return null

            var orientSafety = 0
            while (orientSafety < 10) {
                val offset = cornerId * 12
                if (finalState.stateArray[offset] == 1 &&
                    finalState.stateArray[offset + 1] == 1 &&
                    finalState.stateArray[offset + 2] == 1 &&
                    finalState.stateArray[offset + 7] == 1) {
                    break
                }
                
                val stepMoves = parseAlgorithm("R' D' R D")
                for (move in stepMoves) {
                    finalState = finalState.applyMove(move)
                    movesList.add(move)
                }
                orientSafety++
            }
            if (orientSafety == 10) {
                return null
            }
            if (i < 3) {
                finalState = finalState.applyMove(MoveType.U)
                movesList.add(MoveType.U)
            }
        }

        // Align top layer
        var safety = 0
        val testEdgeId = getCubieIdFromOriginalPos(IntVector3(0, 1, 1))
        while (safety < 4) {
            val offset = testEdgeId * 12
            if (finalState.stateArray[offset] == 0 &&
                finalState.stateArray[offset + 1] == 1 &&
                finalState.stateArray[offset + 2] == 1) {
                break
            }
            finalState = finalState.applyMove(MoveType.U)
            movesList.add(MoveType.U)
            safety++
        }
        if (safety == 4) {
            val offset = testEdgeId * 12
            if (finalState.stateArray[offset] != 0 ||
                finalState.stateArray[offset + 1] != 1 ||
                finalState.stateArray[offset + 2] != 1) {
                return null
            }
        }

        // Final verification
        val allCubiePositions = mutableListOf<IntVector3>()
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    allCubiePositions.add(IntVector3(x, y, z))
                }
            }
        }
        val unsolved = allCubiePositions.filter { pos ->
            val sum = kotlin.math.abs(pos.x) + kotlin.math.abs(pos.y) + kotlin.math.abs(pos.z)
            sum >= 2 && !isCubieSolved(finalState, pos)
        }
        if (unsolved.isNotEmpty()) {
            return null
        }

        allMoves.addAll(movesList)
        return compressMoves(allMoves)
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
                if (m1.axis == m2.axis && m1.layerValue == m2.layerValue && m1.angleSign == -m2.angleSign) {
                    list.removeAt(i + 1)
                    list.removeAt(i)
                    changed = true
                    break
                }
                
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
                
                if (i < list.size - 2) {
                    val m3 = list[i + 2]
                    val commutes = (m1.axis == m2.axis && m1.layerValue != m2.layerValue)
                    if (commutes && m1.axis == m3.axis && m1.layerValue == m3.layerValue && m1.angleSign == -m3.angleSign) {
                        list.removeAt(i + 2)
                        list.removeAt(i)
                        changed = true
                        break
                    }
                }

                if (i < list.size - 2) {
                    val m3 = list[i + 2]
                    val commutes = (m1.axis == m2.axis && m1.layerValue != m2.layerValue)
                    if (commutes && m1 == m3) {
                        list[i + 1] = m3
                        list[i + 2] = m2
                        changed = true
                        break
                    }
                }
                
                i++
            }
        }
        return list
    }

    fun solveAnnotated(startState: RubikCubeState): List<AnnotatedMove>? {
        return solveAnnotated(startState.toSnapshot())
    }

    fun solveAnnotated(startSnapshot: CubeSnapshot): List<AnnotatedMove>? {
        var state = startSnapshot
        val allMoves = mutableListOf<AnnotatedMove>()

        // Step 1: Red Cross (Bottom Edges)
        val redEdges = listOf(
            IntVector3(0, -1, 1),  // Green-Red
            IntVector3(1, -1, 0),  // White-Red
            IntVector3(0, -1, -1), // Blue-Red
            IntVector3(-1, -1, 0)  // Yellow-Red
        )

        val redEdgeIds = redEdges.map { findId(state, it) }
        val basicMoves = MoveType.values().toList()

        val phase1Name = "Alt Artı Oluşturma"
        val phase1Desc = "Alt katmanda kırmızı renkte artı (cross) simgesi oluşturuluyor. Bu adım, sonraki katmanların doğru konumlandırılması için referans noktası sağlar."

        for (i in redEdges.indices) {
            val goalEdges = redEdges.take(i + 1)
            val activeIds = redEdgeIds.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = basicMoves,
                macroMoves = emptyList(),
                activeIds = activeIds,
                maxDepth = 6,
                isGoal = { snap -> goalEdges.all { isCubieSolved(snap, it) } }
            ) ?: return null
            state = result.first
            result.second.forEach {
                allMoves.add(AnnotatedMove(it, phase1Name, phase1Desc))
            }
        }

        // Step 2: Red Corners (Bottom Corners)
        val redCorners = listOf(
            IntVector3(1, -1, 1),   // Green-White-Red
            IntVector3(1, -1, -1),  // White-Blue-Red
            IntVector3(-1, -1, -1), // Blue-Yellow-Red
            IntVector3(-1, -1, 1)   // Yellow-Green-Red
        )

        val redCornerIds = redCorners.map { findId(state, it) }

        val crossPreservingMoves = listOf(
            MoveType.U, MoveType.U_PRIME,
            MoveType.R, MoveType.R_PRIME,
            MoveType.L, MoveType.L_PRIME,
            MoveType.F, MoveType.F_PRIME,
            MoveType.B, MoveType.B_PRIME
        )

        val cornerMacros = listOf(
            "R U R'", "R U' R'", "R U2 R'",
            "R' U R", "R' U' R", "R' U2 R",
            "L U L'", "L U' L'", "L U2 L'",
            "L' U L", "L' U' L", "L' U2 L",
            "F U F'", "F U' F'", "F U2 F'",
            "F' U F", "F' U' F", "F' U2 F",
            "B U B'", "B U' B'", "B U2 B'",
            "B' U B", "B' U' B", "B' U2 B"
        ).map { parseAlgorithm(it) }

        val phase2Name = "Alt Köşelerin Yerleşimi"
        val phase2Desc = "Kırmızı renkli alt köşeler doğru yerlerine yerleştirilerek ilk katman tamamlanıyor. Bu adım, küpün tabanını tamamen çözer."

        for (i in redCorners.indices) {
            val goalCorners = redCorners.take(i + 1)
            val activeIds = redEdgeIds + redCornerIds.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = crossPreservingMoves,
                macroMoves = cornerMacros,
                activeIds = activeIds,
                maxDepth = 4,
                isGoal = { snap ->
                    redEdges.all { isCubieSolved(snap, it) } && goalCorners.all { isCubieSolved(snap, it) }
                }
            ) ?: return null
            state = result.first
            result.second.forEach {
                allMoves.add(AnnotatedMove(it, phase2Name, phase2Desc))
            }
        }

        // Step 3: Middle Layer Edges
        val middleEdges = listOf(
            IntVector3(1, 0, 1),   // Green-White
            IntVector3(1, 0, -1),  // White-Blue
            IntVector3(-1, 0, -1), // Blue-Yellow
            IntVector3(-1, 0, 1)   // Yellow-Green
        )

        val middleEdgeIds = middleEdges.map { findId(state, it) }

        val middleMacros = listOf(
            "U R U' R' U' F' U F", "U' F' U F U R U' R'",
            "U B U' B' U' R' U R", "U' R' U R U B U' B'",
            "U L U' L' U' B' U B", "U' B' U B U L U' L'",
            "U F U' F' U' L' U L", "U' L' U L U F U' F'"
        ).map { parseAlgorithm(it) }

        val phase3Name = "Orta Katman Kenarları"
        val phase3Desc = "Orta katmandaki 4 kenar parçası, önceden çözülmüş alt katmanları bozmayacak özel algoritmalarla yerleştirilerek ikinci katman tamamlanıyor."

        for (i in middleEdges.indices) {
            val goalEdges = middleEdges.take(i + 1)
            val activeIds = redEdgeIds + redCornerIds + middleEdgeIds.take(i + 1)
            val result = bfs(
                start = state,
                allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
                macroMoves = middleMacros,
                activeIds = activeIds,
                maxDepth = 4,
                isGoal = { snap ->
                    redEdges.all { isCubieSolved(snap, it) } &&
                    redCorners.all { isCubieSolved(snap, it) } &&
                    goalEdges.all { isCubieSolved(snap, it) }
                }
            ) ?: return null
            state = result.first
            result.second.forEach {
                allMoves.add(AnnotatedMove(it, phase3Name, phase3Desc))
            }
        }

        // Step 4: Yellow/Orange Cross (OLL Edges Orientation)
        val topEdges = listOf(
            IntVector3(0, 1, 1),
            IntVector3(1, 1, 0),
            IntVector3(0, 1, -1),
            IntVector3(-1, 1, 0)
        )

        val topEdgeIds = topEdges.map { findId(state, it) }

        val ollMacros = listOf(
            "F R U R' U' F'",
            "F U R U' R' F'"
        ).map { parseAlgorithm(it) }

        val phase4Name = "Turuncu Artı Yönelimi (OLL)"
        val phase4Desc = "Üst katmanda (turuncu yüz) artı simgesi oluşturuluyor. OLL (Orientation of Last Layer) adımı ile üst yüzdeki renklerin tamamı yukarı bakar hale getirilir."

        val ollResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = ollMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { pos ->
                    val id = getCubieIdFromOriginalPos(pos)
                    snap.stateArray[id * 12 + 7] == 1
                }
            }
        ) ?: return null
        state = ollResult.first
        ollResult.second.forEach {
            allMoves.add(AnnotatedMove(it, phase4Name, phase4Desc))
        }

        // Step 5: Yellow/Orange Cross Permutation (Edges Position)
        val pllEdgeMacros = listOf(
            "R U R' U R U2 R'", "R U2 R' U' R U' R'",
            "L' U' L U' L' U2 L", "L' U2 L U L' U L",
            "F U F' U F U2 F'", "F U2 F' U' F U' F'",
            "B' U' B U' B' U2 B", "B' U2 B U B' U B"
        ).map { parseAlgorithm(it) }

        val phase5Name = "Turuncu Artı Konumlandırma"
        val phase5Desc = "Üst katmandaki artı kenarları kendi yan yüz renkleriyle hizalanıyor. Bu sayede üst artı parçaları doğru yerlerine oturur."

        val pllEdgesResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllEdgeMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { isCubieSolved(snap, it) }
            }
        ) ?: return null
        state = pllEdgesResult.first
        pllEdgesResult.second.forEach {
            allMoves.add(AnnotatedMove(it, phase5Name, phase5Desc))
        }

        // Step 6: Yellow/Orange Corners Positioning (Corners Position)
        val topCorners = listOf(
            IntVector3(1, 1, 1),
            IntVector3(1, 1, -1),
            IntVector3(-1, 1, -1),
            IntVector3(-1, 1, 1)
        )

        val topCornerIds = topCorners.map { findId(state, it) }

        val pllCornerMacros = listOf(
            "U R U' L' U R' U' L", "U' L' U R U' L U R'",
            "U F U' B' U F' U' B", "U' B' U F U' B U F'",
            "U L U' R' U L' U' R", "U' R' U L U' R U L'"
        ).map { parseAlgorithm(it) }

        val phase6Name = "Üst Köşe Konumlandırma"
        val phase6Desc = "Üst katmandaki 4 köşenin (renkleri ters dönmüş olsa dahi) kendi doğru köşelerine yerleşmesi sağlanır."

        val pllCornersResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllCornerMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds + topCornerIds,
            maxDepth = 6,
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { isCubieSolved(snap, it) } &&
                topCorners.all { pos ->
                    val id = getCubieIdFromOriginalPos(pos)
                    snap.stateArray[id * 12] == pos.x &&
                    snap.stateArray[id * 12 + 1] == pos.y &&
                    snap.stateArray[id * 12 + 2] == pos.z
                }
            }
        ) ?: return null
        state = pllCornersResult.first
        pllCornersResult.second.forEach {
            allMoves.add(AnnotatedMove(it, phase6Name, phase6Desc))
        }

        // Step 7: Yellow/Orange Corners Orientation (Solve Cube)
        val movesList = mutableListOf<MoveType>()
        var finalState = state
        
        val phase7Name = "Üst Köşe Yönelimi (Çözüm)"
        val phase7Desc = "Üst katmandaki köşelerin renk yönleri 'R' D' R D' algoritmasıyla düzeltilerek tüm küp tamamen çözülmüş hale getirilir."

        for (i in 0..3) {
            var cornerId = -1
            for (id in 0 until 27) {
                val offset = id * 12
                if (finalState.stateArray[offset] == 1 &&
                    finalState.stateArray[offset + 1] == 1 &&
                    finalState.stateArray[offset + 2] == 1) {
                    cornerId = id
                    break
                }
            }
            if (cornerId == -1) return null

            var orientSafety = 0
            while (orientSafety < 10) {
                val offset = cornerId * 12
                if (finalState.stateArray[offset] == 1 &&
                    finalState.stateArray[offset + 1] == 1 &&
                    finalState.stateArray[offset + 2] == 1 &&
                    finalState.stateArray[offset + 7] == 1) {
                    break
                }
                
                val stepMoves = parseAlgorithm("R' D' R D")
                for (move in stepMoves) {
                    finalState = finalState.applyMove(move)
                    movesList.add(move)
                }
                orientSafety++
            }
            if (orientSafety == 10) {
                return null
            }
            if (i < 3) {
                finalState = finalState.applyMove(MoveType.U)
                movesList.add(MoveType.U)
            }
        }

        var safety = 0
        val testEdgeId = getCubieIdFromOriginalPos(IntVector3(0, 1, 1))
        while (safety < 4) {
            val offset = testEdgeId * 12
            if (finalState.stateArray[offset] == 0 &&
                finalState.stateArray[offset + 1] == 1 &&
                finalState.stateArray[offset + 2] == 1) {
                break
            }
            finalState = finalState.applyMove(MoveType.U)
            movesList.add(MoveType.U)
            safety++
        }
        if (safety == 4) {
            val offset = testEdgeId * 12
            if (finalState.stateArray[offset] != 0 ||
                finalState.stateArray[offset + 1] != 1 ||
                finalState.stateArray[offset + 2] != 1) {
                return null
            }
        }

        movesList.forEach {
            allMoves.add(AnnotatedMove(it, phase7Name, phase7Desc))
        }

        val allCubiePositions = mutableListOf<IntVector3>()
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    allCubiePositions.add(IntVector3(x, y, z))
                }
            }
        }
        val unsolved = allCubiePositions.filter { pos ->
            val sum = kotlin.math.abs(pos.x) + kotlin.math.abs(pos.y) + kotlin.math.abs(pos.z)
            sum >= 2 && !isCubieSolved(finalState, pos)
        }
        if (unsolved.isNotEmpty()) {
            return null
        }

        return compressAnnotatedMoves(allMoves)
    }

    private fun optimizeAnnotatedMoves(moves: List<AnnotatedMove>): List<AnnotatedMove> {
        val list = moves.toMutableList()
        var changed = true
        while (changed) {
            changed = false
            var i = 0
            while (i < list.size - 1) {
                val m1 = list[i]
                
                val m2 = list[i + 1]
                if (m1.move.axis == m2.move.axis && m1.move.layerValue == m2.move.layerValue && m1.move.angleSign == -m2.move.angleSign) {
                    list.removeAt(i + 1)
                    list.removeAt(i)
                    changed = true
                    break
                }
                
                if (i < list.size - 2) {
                    val m3 = list[i + 2]
                    if (m1.move == m2.move && m2.move == m3.move) {
                        val inverse = MoveType.values().first {
                            it.axis == m1.move.axis && it.layerValue == m1.move.layerValue && it.angleSign == -m1.move.angleSign
                        }
                        list.removeAt(i + 2)
                        list.removeAt(i + 1)
                        list[i] = AnnotatedMove(inverse, m1.phaseName, m1.phaseDescription)
                        changed = true
                        break
                    }
                }
                
                if (i < list.size - 2) {
                    val m3 = list[i + 2]
                    val commutes = (m1.move.axis == m2.move.axis && m1.move.layerValue != m2.move.layerValue)
                    if (commutes && m1.move.axis == m3.move.axis && m1.move.layerValue == m3.move.layerValue && m1.move.angleSign == -m3.move.angleSign) {
                        list.removeAt(i + 2)
                        list.removeAt(i)
                        changed = true
                        break
                    }
                }

                if (i < list.size - 2) {
                    val m3 = list[i + 2]
                    val commutes = (m1.move.axis == m2.move.axis && m1.move.layerValue != m2.move.layerValue)
                    if (commutes && m1.move == m3.move) {
                        list[i + 1] = m3
                        list[i + 2] = m2
                        changed = true
                        break
                    }
                }
                
                i++
            }
        }
        return list
    }

    private fun createSolvedSnapshot(): CubeSnapshot {
        val arr = IntArray(27 * 12)
        var idx = 0
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    arr[idx++] = x
                    arr[idx++] = y
                    arr[idx++] = z
                    arr[idx++] = 1
                    arr[idx++] = 0
                    arr[idx++] = 0
                    arr[idx++] = 0
                    arr[idx++] = 1
                    arr[idx++] = 0
                    arr[idx++] = 0
                    arr[idx++] = 0
                    arr[idx++] = 1
                }
            }
        }
        return CubeSnapshot(arr)
    }

    private fun findShortestTransform(
        start: CubeSnapshot,
        target: CubeSnapshot,
        maxDepth: Int
    ): List<MoveType>? {
        if (start == target) return emptyList()
        if (maxDepth <= 0) return null
        
        val activeIds = (0 until 27).toList()
        val targetActive = target.toActiveState(activeIds)
        
        val queue = ArrayDeque<Triple<CubeSnapshot, List<MoveType>, Int>>()
        val visited = mutableSetOf<ActiveState>()
        
        val startActive = start.toActiveState(activeIds)
        queue.add(Triple(start, emptyList(), 0))
        visited.add(startActive)
        
        val basicMoves = MoveType.values().toList()
        
        while (queue.isNotEmpty()) {
            val (state, path, depth) = queue.removeFirst()
            
            if (depth >= maxDepth) continue
            
            for (move in basicMoves) {
                if (path.isNotEmpty()) {
                    val last = path.last()
                    if (last.axis == move.axis && last.layerValue == move.layerValue && last.angleSign == -move.angleSign) {
                        continue
                    }
                }
                
                val nextState = state.applyMove(move)
                val nextActive = nextState.toActiveState(activeIds)
                if (nextActive !in visited) {
                    val nextPath = path + move
                    if (nextActive == targetActive) {
                        return nextPath
                    }
                    visited.add(nextActive)
                    queue.add(Triple(nextState, nextPath, depth + 1))
                }
            }
        }
        return null
    }

    fun compressMoves(moves: List<MoveType>, maxPasses: Int = 3): List<MoveType> {
        return optimizeMoves(moves)
    }

    fun compressAnnotatedMoves(moves: List<AnnotatedMove>, maxPasses: Int = 3): List<AnnotatedMove> {
        return optimizeAnnotatedMoves(moves)
    }
}

data class AnnotatedMove(
    val move: MoveType,
    val phaseName: String,
    val phaseDescription: String
)
