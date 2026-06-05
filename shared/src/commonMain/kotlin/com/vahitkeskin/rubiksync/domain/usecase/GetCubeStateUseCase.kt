package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.CubeRepository
import com.vahitkeskin.rubiksync.utils.LoadedCubeState

class GetCubeStateUseCase(
    private val repository: CubeRepository
) {
    suspend operator fun invoke(): LoadedCubeState? {
        return repository.loadCubeState()
    }
}
