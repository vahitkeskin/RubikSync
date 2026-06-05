package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository
import com.vahitkeskin.rubiksync.utils.CameraSettings

class GetCameraSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): CameraSettings? {
        return repository.loadCameraSettings()
    }
}
