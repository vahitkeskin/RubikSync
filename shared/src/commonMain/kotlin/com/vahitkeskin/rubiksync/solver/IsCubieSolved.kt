package com.vahitkeskin.rubiksync.solver



fun isCubieSolved(snap: CubeSnapshot, origPos: IntVector3): Boolean {
    val id = getCubieIdFromOriginalPos(origPos)
    val offset = id * 12
    return snap.stateArray[offset] == origPos.x &&
            snap.stateArray[offset + 1] == origPos.y &&
            snap.stateArray[offset + 2] == origPos.z &&
            snap.stateArray[offset + 3] == 1 && snap.stateArray[offset + 4] == 0 && snap.stateArray[offset + 5] == 0 &&
            snap.stateArray[offset + 6] == 0 && snap.stateArray[offset + 7] == 1 && snap.stateArray[offset + 8] == 0 &&
            snap.stateArray[offset + 9] == 0 && snap.stateArray[offset + 10] == 0 && snap.stateArray[offset + 11] == 1
}
