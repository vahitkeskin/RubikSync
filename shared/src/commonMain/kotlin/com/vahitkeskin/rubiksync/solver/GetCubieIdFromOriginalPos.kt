package com.vahitkeskin.rubiksync.solver



fun getCubieIdFromOriginalPos(pos: IntVector3): Int {
    val x = pos.x + 1
    val y = pos.y + 1
    val z = pos.z + 1
    return x * 9 + y * 3 + z
}
