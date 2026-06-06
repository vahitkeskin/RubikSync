package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry
import com.vahitkeskin.rubiksync.utils.SolveSession

class GetSolveSessionsUseCase {
    suspend operator fun invoke(): List<SolveSession> {
        return RubikPersistenceRegistry.persistence?.loadSolveSessions() ?: emptyList()
    }
}
