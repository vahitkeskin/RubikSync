package com.vahitkeskin.rubiksync.data.repository

import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.domain.repository.CubeRepository
import com.vahitkeskin.rubiksync.utils.CubiePersistable
import com.vahitkeskin.rubiksync.utils.LoadedCubeState
import com.vahitkeskin.rubiksync.utils.RubikPersistence

class CubeRepositoryImpl(
    private val persistence: RubikPersistence
) : CubeRepository {

    override suspend fun saveCubeState(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    ) {
        persistence.saveCubeState(cubies, moveHistory, manualMoves, editorFaces)
    }

    override suspend fun loadCubeState(): LoadedCubeState? {
        return persistence.loadCubeState()
    }
}
