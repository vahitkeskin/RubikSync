package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry

class SaveSolveSessionUseCase {
    suspend operator fun invoke(durationMillis: Long, moveCount: Int, timestamp: Long) {
        RubikPersistenceRegistry.persistence?.saveSolveSession(durationMillis, moveCount, timestamp)
    }
}
