package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository

class SaveCubeSkinUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(skin: String) {
        repository.saveCubeSkin(skin)
    }
}
