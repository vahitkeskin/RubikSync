package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.domain.repository.CubeRepository
import com.vahitkeskin.rubiksync.utils.CubiePersistable

class SaveCubeStateUseCase(
    private val repository: CubeRepository
) {
    suspend operator fun invoke(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    ) {
        repository.saveCubeState(cubies, moveHistory, manualMoves, editorFaces)
    }
}
