package com.vahitkeskin.rubiksync.solver

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
        stateArray.copyInto(arr, idx, offset, offset + 12)
        idx += 12
    }
    return ActiveState(arr)
}
