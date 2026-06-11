package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType

fun optimizeAnnotatedMoves(moves: List<AnnotatedMove>): List<AnnotatedMove> {
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
                    val inverse = MoveType.entries.first {
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
