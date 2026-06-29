package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState


class RubikSolver {

    fun solve(startState: RubikCubeState): List<MoveType>? {
        val startSnapshot = startState.toSnapshot()
        
        // 1. Check if already solved
        if (isCubeSolved(startSnapshot)) {
            return emptyList()
        }
        
        // 2. Try bidirectional BFS for absolute shortest path up to depth 6
        val shortSolution = solveBidirectional(startSnapshot, maxDepth = 6)
        if (shortSolution != null) {
            return shortSolution
        }
        
        // 3. Try to use inverse history if it exists and solves the cube
        if (startState.moveHistory.isNotEmpty()) {
            val inverseHistory = startState.moveHistory.map { current ->
                MoveType.entries.first {
                    it.axis == current.axis &&
                    it.layerValue == current.layerValue &&
                    it.angleSign == -current.angleSign
                }
            }.reversed()
            val optimized = compressMoves(inverseHistory)
            if (verifySolution(startSnapshot, optimized)) {
                return optimized
            }
        }
        
        // 4. Fallback to full LBL solver
        return solve(startSnapshot)
    }

    fun solve(startSnapshot: CubeSnapshot): List<MoveType>? {
        var state = startSnapshot
        val allMoves = mutableListOf<MoveType>()

        val redEdges = listOf(
            IntVector3(0, -1, 1),
            IntVector3(1, -1, 0),
            IntVector3(0, -1, -1),
            IntVector3(-1, -1, 0)
        )
        val redEdgeIds = redEdges.map { getCubieIdFromOriginalPos(it) }
        val basicMoves = MoveType.entries

        for (i in redEdges.indices) {
            val goalEdges = redEdges.take(i + 1)
            val activeIds = redEdgeIds.take(i + 1)
            // bfs fonksiyonu ile çözüm aranıyor
            val result = bfs(
                start = state,
                allowedMoves = basicMoves,
                macroMoves = emptyList(),
                activeIds = activeIds,
                maxDepth = 6,
                // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
                isGoal = { snap -> goalEdges.all { isCubieSolved(snap, it) } }
            ) ?: return null
            state = result.first
            allMoves.addAll(result.second)
        }

        val redCorners = listOf(
            IntVector3(1, -1, 1),
            IntVector3(1, -1, -1),
            IntVector3(-1, -1, -1),
            IntVector3(-1, -1, 1)
        )
        val redCornerIds = redCorners.map { getCubieIdFromOriginalPos(it) }

        val crossPreservingMoves = listOf(
            MoveType.U, MoveType.U_PRIME,
            MoveType.R, MoveType.R_PRIME,
            MoveType.L, MoveType.L_PRIME,
            MoveType.F, MoveType.F_PRIME,
            MoveType.B, MoveType.B_PRIME
        )

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
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
            // bfs fonksiyonu ile çözüm aranıyor
            val result = bfs(
                start = state,
                allowedMoves = crossPreservingMoves,
                macroMoves = cornerMacros,
                activeIds = activeIds,
                maxDepth = 4,
                // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
                isGoal = { snap ->
                    redEdges.all { isCubieSolved(snap, it) } && goalCorners.all { isCubieSolved(snap, it) }
                }
            ) ?: return null
            state = result.first
            allMoves.addAll(result.second)
        }

        val middleEdges = listOf(
            IntVector3(1, 0, 1),
            IntVector3(1, 0, -1),
            IntVector3(-1, 0, -1),
            IntVector3(-1, 0, 1)
        )
        val middleEdgeIds = middleEdges.map { getCubieIdFromOriginalPos(it) }

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
        val middleMacros = listOf(
            "U R U' R' U' F' U F", "U' F' U F U R U' R'",
            "U B U' B' U' R' U R", "U' R' U R U B U' B'",
            "U L U' L' U' B' U B", "U' B' U B U L U' L'",
            "U F U' F' U' L' U L", "U' L' U L U F U' F'"
        ).map { parseAlgorithm(it) }

        for (i in middleEdges.indices) {
            val goalEdges = middleEdges.take(i + 1)
            val activeIds = redEdgeIds + redCornerIds + middleEdgeIds.take(i + 1)
            // bfs fonksiyonu ile çözüm aranıyor
            val result = bfs(
                start = state,
                allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
                macroMoves = middleMacros,
                activeIds = activeIds,
                maxDepth = 4,
                // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
                isGoal = { snap ->
                    redEdges.all { isCubieSolved(snap, it) } &&
                    redCorners.all { isCubieSolved(snap, it) } &&
                    goalEdges.all { isCubieSolved(snap, it) }
                }
            ) ?: return null
            state = result.first
            allMoves.addAll(result.second)
        }

        val topEdges = listOf(
            IntVector3(0, 1, 1),
            IntVector3(1, 1, 0),
            IntVector3(0, 1, -1),
            IntVector3(-1, 1, 0)
        )
        val topEdgeIds = topEdges.map { getCubieIdFromOriginalPos(it) }

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
        val ollMacros = listOf(
            "F R U R' U' F'",
            "F U R U' R' F'"
        ).map { parseAlgorithm(it) }

        // bfs fonksiyonu ile çözüm aranıyor
        val ollResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = ollMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { pos ->
                    // getCubieIdFromOriginalPos fonksiyonu ile asıl ID bulunuyor
                    val id = getCubieIdFromOriginalPos(pos)
                    snap.stateArray[id * 12 + 7] == 1
                }
            }
        ) ?: return null
        state = ollResult.first
        allMoves.addAll(ollResult.second)

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
        val pllEdgeMacros = listOf(
            "R U R' U R U2 R'", "R U2 R' U' R U' R'",
            "L' U' L U' L' U2 L", "L' U2 L U L' U L",
            "F U F' U F U2 F'", "F U2 F' U' F U' F'",
            "B' U' B U' B' U2 B", "B' U2 B U B' U B"
        ).map { parseAlgorithm(it) }

        // bfs fonksiyonu ile çözüm aranıyor
        val pllEdgesResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllEdgeMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { isCubieSolved(snap, it) }
            }
        ) ?: return null
        state = pllEdgesResult.first
        allMoves.addAll(pllEdgesResult.second)

        val topCorners = listOf(
            IntVector3(1, 1, 1),
            IntVector3(1, 1, -1),
            IntVector3(-1, 1, -1),
            IntVector3(-1, 1, 1)
        )
        val topCornerIds = topCorners.map { getCubieIdFromOriginalPos(it) }

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
        val pllCornerMacros = listOf(
            "U R U' L' U R' U' L", "U' L' U R U' L U R'",
            "U F U' B' U F' U' B", "U' B' U F U' B U F'",
            "U L U' R' U L' U' R", "U' R' U L U' R U L'"
        ).map { parseAlgorithm(it) }

        // bfs fonksiyonu ile çözüm aranıyor
        val pllCornersResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllCornerMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds + topCornerIds,
            maxDepth = 6,
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { isCubieSolved(snap, it) } &&
                topCorners.all { pos ->
                    // getCubieIdFromOriginalPos fonksiyonu ile asıl ID bulunuyor
                    val id = getCubieIdFromOriginalPos(pos)
                    snap.stateArray[id * 12] == pos.x &&
                    snap.stateArray[id * 12 + 1] == pos.y &&
                    snap.stateArray[id * 12 + 2] == pos.z
                }
            }
        ) ?: return null
        state = pllCornersResult.first
        allMoves.addAll(pllCornersResult.second)

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
                
                // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
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
        // getCubieIdFromOriginalPos fonksiyonu ile asıl ID bulunuyor
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
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
            sum >= 2 && !isCubieSolved(finalState, pos)
        }
        if (unsolved.isNotEmpty()) {
            return null
        }

        allMoves.addAll(movesList)
        // compressMoves fonksiyonu ile çözülen adımlar sadeleştiriliyor
        return compressMoves(allMoves)
    }

    fun solveAnnotated(startState: RubikCubeState): List<AnnotatedMove>? {
        val startSnapshot = startState.toSnapshot()
        
        // 1. Check if already solved
        if (isCubeSolved(startSnapshot)) {
            return emptyList()
        }
        
        // 2. Try bidirectional BFS for absolute shortest path up to depth 6
        val shortSolution = solveBidirectional(startSnapshot, maxDepth = 6)
        if (shortSolution != null) {
            val phaseName = "En Kısa Yol (Optimal)"
            val phaseDesc = "Küpün durumu analiz edilerek optimal çözüm yolu hesaplandı."
            return shortSolution.map { AnnotatedMove(it, phaseName, phaseDesc) }
        }
        
        // 3. Try to use inverse history if it exists and solves the cube
        if (startState.moveHistory.isNotEmpty()) {
            val inverseHistory = startState.moveHistory.map { current ->
                MoveType.entries.first {
                    it.axis == current.axis &&
                    it.layerValue == current.layerValue &&
                    it.angleSign == -current.angleSign
                }
            }.reversed()
            val optimized = compressMoves(inverseHistory)
            if (verifySolution(startSnapshot, optimized)) {
                val phaseName = "Optimal Karıştırma Çözümü"
                val phaseDesc = "Küpün hareket geçmişi analiz edilerek en kısa çözüm yolu hesaplandı."
                return optimized.map { AnnotatedMove(it, phaseName, phaseDesc) }
            }
        }
        
        // 4. Fallback to full LBL solver
        return solveAnnotated(startSnapshot)
    }

    fun solveAnnotated(startSnapshot: CubeSnapshot): List<AnnotatedMove>? {
        var state = startSnapshot
        val allMoves = mutableListOf<AnnotatedMove>()

        val redEdges = listOf(
            IntVector3(0, -1, 1),
            IntVector3(1, -1, 0),
            IntVector3(0, -1, -1),
            IntVector3(-1, -1, 0)
        )
        val redEdgeIds = redEdges.map { getCubieIdFromOriginalPos(it) }
        val basicMoves = MoveType.entries

        val phase1Name = "Alt Artı Oluşturma"
        val phase1Desc = "Alt katmanda kırmızı renkte artı (cross) simgesi oluşturuluyor. Bu adım, sonraki katmanların doğru konumlandırılması için referans noktası sağlar."

        for (i in redEdges.indices) {
            val goalEdges = redEdges.take(i + 1)
            val activeIds = redEdgeIds.take(i + 1)
            // bfs fonksiyonu ile çözüm aranıyor
            val result = bfs(
                start = state,
                allowedMoves = basicMoves,
                macroMoves = emptyList(),
                activeIds = activeIds,
                maxDepth = 6,
                // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
                isGoal = { snap -> goalEdges.all { isCubieSolved(snap, it) } }
            ) ?: return null
            state = result.first
            result.second.forEach {
                allMoves.add(AnnotatedMove(it, phase1Name, phase1Desc))
            }
        }

        val redCorners = listOf(
            IntVector3(1, -1, 1),
            IntVector3(1, -1, -1),
            IntVector3(-1, -1, -1),
            IntVector3(-1, -1, 1)
        )
        val redCornerIds = redCorners.map { getCubieIdFromOriginalPos(it) }

        val crossPreservingMoves = listOf(
            MoveType.U, MoveType.U_PRIME,
            MoveType.R, MoveType.R_PRIME,
            MoveType.L, MoveType.L_PRIME,
            MoveType.F, MoveType.F_PRIME,
            MoveType.B, MoveType.B_PRIME
        )

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
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
            // bfs fonksiyonu ile çözüm aranıyor
            val result = bfs(
                start = state,
                allowedMoves = crossPreservingMoves,
                macroMoves = cornerMacros,
                activeIds = activeIds,
                maxDepth = 4,
                // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
                isGoal = { snap ->
                    redEdges.all { isCubieSolved(snap, it) } && goalCorners.all { isCubieSolved(snap, it) }
                }
            ) ?: return null
            state = result.first
            result.second.forEach {
                allMoves.add(AnnotatedMove(it, phase2Name, phase2Desc))
            }
        }

        val middleEdges = listOf(
            IntVector3(1, 0, 1),
            IntVector3(1, 0, -1),
            IntVector3(-1, 0, -1),
            IntVector3(-1, 0, 1)
        )
        val middleEdgeIds = middleEdges.map { getCubieIdFromOriginalPos(it) }

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
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
            // bfs fonksiyonu ile çözüm aranıyor
            val result = bfs(
                start = state,
                allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
                macroMoves = middleMacros,
                activeIds = activeIds,
                maxDepth = 4,
                // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
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

        val topEdges = listOf(
            IntVector3(0, 1, 1),
            IntVector3(1, 1, 0),
            IntVector3(0, 1, -1),
            IntVector3(-1, 1, 0)
        )
        val topEdgeIds = topEdges.map { getCubieIdFromOriginalPos(it) }

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
        val ollMacros = listOf(
            "F R U R' U' F'",
            "F U R U' R' F'"
        ).map { parseAlgorithm(it) }

        val phase4Name = "Turuncu Artı Yönelimi (OLL)"
        val phase4Desc = "Üst katmanda (turuncu yüz) artı simgesi oluşturuluyor. OLL (Orientation of Last Layer) adımı ile üst yüzdeki renklerin tamamı yukarı bakar hale getirilir."

        // bfs fonksiyonu ile çözüm aranıyor
        val ollResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = ollMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { pos ->
                    // getCubieIdFromOriginalPos fonksiyonu ile asıl ID bulunuyor
                    val id = getCubieIdFromOriginalPos(pos)
                    snap.stateArray[id * 12 + 7] == 1
                }
            }
        ) ?: return null
        state = ollResult.first
        ollResult.second.forEach {
            allMoves.add(AnnotatedMove(it, phase4Name, phase4Desc))
        }

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
        val pllEdgeMacros = listOf(
            "R U R' U R U2 R'", "R U2 R' U' R U' R'",
            "L' U' L U' L' U2 L", "L' U2 L U L' U L",
            "F U F' U F U2 F'", "F U2 F' U' F U' F'",
            "B' U' B U' B' U2 B", "B' U2 B U B' U B"
        ).map { parseAlgorithm(it) }

        val phase5Name = "Turuncu Artı Konumlandırma"
        val phase5Desc = "Üst katmandaki artı kenarları kendi yan yüz renkleriyle hizalanıyor. Bu sayede üst artı parçaları doğru yerlerine oturur."

        // bfs fonksiyonu ile çözüm aranıyor
        val pllEdgesResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllEdgeMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds,
            maxDepth = 6,
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
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

        val topCorners = listOf(
            IntVector3(1, 1, 1),
            IntVector3(1, 1, -1),
            IntVector3(-1, 1, -1),
            IntVector3(-1, 1, 1)
        )
        val topCornerIds = topCorners.map { getCubieIdFromOriginalPos(it) }

        // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
        val pllCornerMacros = listOf(
            "U R U' L' U R' U' L", "U' L' U R U' L U R'",
            "U F U' B' U F' U' B", "U' B' U F U' B U F'",
            "U L U' R' U L' U' R", "U' R' U L U' R U L'"
        ).map { parseAlgorithm(it) }

        val phase6Name = "Üst Köşe Konumlandırma"
        val phase6Desc = "Üst katmandaki 4 köşenin (renkleri ters dönmüş olsa dahi) kendi doğru köşelerine yerleşmesi sağlanır."

        // bfs fonksiyonu ile çözüm aranıyor
        val pllCornersResult = bfs(
            start = state,
            allowedMoves = listOf(MoveType.U, MoveType.U_PRIME),
            macroMoves = pllCornerMacros,
            activeIds = redEdgeIds + redCornerIds + middleEdgeIds + topEdgeIds + topCornerIds,
            maxDepth = 6,
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
            isGoal = { snap ->
                redEdges.all { isCubieSolved(snap, it) } &&
                redCorners.all { isCubieSolved(snap, it) } &&
                middleEdges.all { isCubieSolved(snap, it) } &&
                topEdges.all { isCubieSolved(snap, it) } &&
                topCorners.all { pos ->
                    // getCubieIdFromOriginalPos fonksiyonu ile asıl ID bulunuyor
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

                // parseAlgorithm fonksiyonu ile string algoritma parse ediliyor
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
        // getCubieIdFromOriginalPos fonksiyonu ile asıl ID bulunuyor
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
            // isCubieSolved fonksiyonu ile küp parçasının çözülüp çözülmediği kontrol ediliyor
            sum >= 2 && !isCubieSolved(finalState, pos)
        }
        if (unsolved.isNotEmpty()) {
            return null
        }

        // optimizeAnnotatedMoves fonksiyonu ile çözülen adımlar sadeleştiriliyor
        return optimizeAnnotatedMoves(allMoves)
    }

    private fun isCubeSolved(snap: CubeSnapshot): Boolean {
        val allCubiePositions = mutableListOf<IntVector3>()
        for (x in -1..1) {
            for (y in -1..1) {
                for (z in -1..1) {
                    allCubiePositions.add(IntVector3(x, y, z))
                }
            }
        }
        return allCubiePositions.filter { pos ->
            val sum = kotlin.math.abs(pos.x) + kotlin.math.abs(pos.y) + kotlin.math.abs(pos.z)
            sum >= 2
        }.all { pos ->
            isCubieSolved(snap, pos)
        }
    }

    private fun verifySolution(start: CubeSnapshot, solution: List<MoveType>): Boolean {
        var state = start
        for (move in solution) {
            state = state.applyMove(move)
        }
        return isCubeSolved(state)
    }

    private fun solveBidirectional(start: CubeSnapshot, maxDepth: Int = 6): List<MoveType>? {
        if (isCubeSolved(start)) return emptyList()

        val allowedMoves = MoveType.entries

        val forwardQueue = ArrayDeque<CubeSnapshot>()
        val forwardParent = mutableMapOf<CubeSnapshot, Pair<CubeSnapshot, MoveType>>()
        forwardQueue.add(start)

        val backwardQueue = ArrayDeque<CubeSnapshot>()
        val backwardParent = mutableMapOf<CubeSnapshot, Pair<CubeSnapshot, MoveType>>()
        
        val solved = RubikCubeState().toSnapshot()
        backwardQueue.add(solved)

        val forwardDepth = mutableMapOf<CubeSnapshot, Int>()
        forwardDepth[start] = 0
        val backwardDepth = mutableMapOf<CubeSnapshot, Int>()
        backwardDepth[solved] = 0

        val maxStates = 40000

        while (forwardQueue.isNotEmpty() || backwardQueue.isNotEmpty()) {
            if (forwardParent.size + backwardParent.size > maxStates) {
                break
            }

            // Expand Forward
            if (forwardQueue.isNotEmpty()) {
                val curr = forwardQueue.removeAt(0)
                val currDepth = forwardDepth[curr] ?: 0

                if (currDepth < (maxDepth + 1) / 2) {
                    for (move in allowedMoves) {
                        val next = curr.applyMove(move)
                        if (next !in forwardDepth) {
                            forwardDepth[next] = currDepth + 1
                            forwardParent[next] = Pair(curr, move)
                            
                            if (next in backwardDepth) {
                                return reconstructPath(next, forwardParent, backwardParent)
                            }
                            
                            forwardQueue.add(next)
                        }
                    }
                }
            }

            // Expand Backward
            if (backwardQueue.isNotEmpty()) {
                val curr = backwardQueue.removeAt(0)
                val currDepth = backwardDepth[curr] ?: 0

                if (currDepth < maxDepth / 2) {
                    for (move in allowedMoves) {
                        val next = curr.applyMove(move)
                        if (next !in backwardDepth) {
                            backwardDepth[next] = currDepth + 1
                            backwardParent[next] = Pair(curr, move)
                            
                            if (next in forwardDepth) {
                                return reconstructPath(next, forwardParent, backwardParent)
                            }
                            
                            backwardQueue.add(next)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun reconstructPath(
        intersection: CubeSnapshot,
        forwardParent: Map<CubeSnapshot, Pair<CubeSnapshot, MoveType>>,
        backwardParent: Map<CubeSnapshot, Pair<CubeSnapshot, MoveType>>
    ): List<MoveType> {
        val forwardPath = mutableListOf<MoveType>()
        var curr = intersection
        while (true) {
            val step = forwardParent[curr] ?: break
            forwardPath.add(0, step.second)
            curr = step.first
        }

        val backwardPath = mutableListOf<MoveType>()
        curr = intersection
        while (true) {
            val step = backwardParent[curr] ?: break
            val move = step.second
            val parent = step.first
            val inverseMove = MoveType.entries.first {
                it.axis == move.axis &&
                it.layerValue == move.layerValue &&
                it.angleSign == -move.angleSign
            }
            backwardPath.add(inverseMove)
            curr = parent
        }

        return compressMoves(forwardPath + backwardPath)
    }
}
