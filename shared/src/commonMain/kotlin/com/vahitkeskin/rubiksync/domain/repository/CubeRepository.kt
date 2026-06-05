package com.vahitkeskin.rubiksync.domain.repository

import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.utils.CubiePersistable
import com.vahitkeskin.rubiksync.utils.LoadedCubeState

interface CubeRepository {
    suspend fun saveCubeState(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    )
    suspend fun loadCubeState(): LoadedCubeState?
}
