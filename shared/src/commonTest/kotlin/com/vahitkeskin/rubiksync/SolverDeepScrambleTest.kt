package com.vahitkeskin.rubiksync

import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.solver.CubeSnapshot
import com.vahitkeskin.rubiksync.solver.RubikSolver
import com.vahitkeskin.rubiksync.solver.toSnapshot
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regression coverage for the full Layer-By-Layer (LBL) solver on deep scrambles.
 *
 * The existing [RubikUnitTest.testSolveAction] only exercises a 3-move scramble, which is
 * handled entirely by the bidirectional BFS shortcut. Real scrambles need the LBL fallback,
 * so this suite scrambles 25 moves and asserts that both [RubikSolver.solve] and
 * [RubikSolver.solveAnnotated] return solutions that actually solve the cube.
 */
class SolverDeepScrambleTest {

    private suspend fun scramble(state: RubikCubeState, turns: Int, rnd: Random) {
        val moves = MoveType.entries
        var last: MoveType? = null
        repeat(turns) {
            var m = moves[rnd.nextInt(moves.size)]
            while (last != null && m.axis == last!!.axis && m.layerValue == last!!.layerValue && m.angleSign == -last!!.angleSign) {
                m = moves[rnd.nextInt(moves.size)]
            }
            state.executeMove(m, skipAnimation = true)
            last = m
        }
    }

    /** A cube is solved when every edge and corner (|x|+|y|+|z| >= 2) is home with identity orientation. */
    private fun isSolved(start: CubeSnapshot, solution: List<MoveType>): Boolean {
        var snap = start
        for (mv in solution) snap = snap.applyMove(mv)
        for (x in -1..1) for (y in -1..1) for (z in -1..1) {
            if (kotlin.math.abs(x) + kotlin.math.abs(y) + kotlin.math.abs(z) < 2) continue
            val o = ((x + 1) * 9 + (y + 1) * 3 + (z + 1)) * 12
            val a = snap.stateArray
            if (a[o] != x || a[o + 1] != y || a[o + 2] != z ||
                a[o + 3] != 1 || a[o + 4] != 0 || a[o + 5] != 0 ||
                a[o + 6] != 0 || a[o + 7] != 1 || a[o + 8] != 0 ||
                a[o + 9] != 0 || a[o + 10] != 0 || a[o + 11] != 1) return false
        }
        return true
    }

    @Test
    fun lblSolvesDeepScrambles() = kotlinx.coroutines.test.runTest {
        val solver = RubikSolver()
        repeat(25) { seed ->
            val rnd = Random(seed.toLong())
            val state = RubikCubeState()
            scramble(state, turns = 25, rnd = rnd)
            val scrambled = state.toSnapshot()

            val solution = solver.solve(scrambled)
            assertNotNull(solution, "solve() returned null for seed=$seed")
            assertTrue(isSolved(scrambled, solution), "solve() solution did not solve the cube for seed=$seed")

            val annotated = solver.solveAnnotated(scrambled)
            assertNotNull(annotated, "solveAnnotated() returned null for seed=$seed")
            assertTrue(isSolved(scrambled, annotated.map { it.move }), "solveAnnotated() solution did not solve the cube for seed=$seed")
        }
    }
}
