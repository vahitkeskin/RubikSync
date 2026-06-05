package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository

class SaveThemeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: String) {
        repository.saveThemeMode(mode)
    }
}
