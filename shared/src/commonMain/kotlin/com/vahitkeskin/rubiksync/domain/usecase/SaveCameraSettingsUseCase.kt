package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository

class SaveCameraSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    ) {
        repository.saveCameraSettings(yaw, pitch, cameraDistance, panX, panY, rotationSpeedMs)
    }
}
