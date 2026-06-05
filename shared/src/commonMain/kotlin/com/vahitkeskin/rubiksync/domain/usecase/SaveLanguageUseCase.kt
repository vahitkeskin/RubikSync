package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository

class SaveLanguageUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(langCode: String) {
        repository.saveLanguage(langCode)
    }
}
