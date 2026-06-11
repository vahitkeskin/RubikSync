package com.vahitkeskin.rubiksync.solver

import com.vahitkeskin.rubiksync.cube.MoveType

fun optimizeMoves(moves: List<MoveType>): List<MoveType> {
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
                    val inverse = MoveType.entries.first {
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
