package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository

class SaveScrambleSoundTooltipShownUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(shown: Boolean) {
        repository.saveScrambleSoundTooltipShown(shown)
    }
}
