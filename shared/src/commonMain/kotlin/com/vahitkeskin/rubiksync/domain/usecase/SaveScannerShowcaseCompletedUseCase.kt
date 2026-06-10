package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository

class SaveScannerShowcaseCompletedUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(completed: Boolean) {
        repository.saveScannerShowcaseCompleted(completed)
    }
}
