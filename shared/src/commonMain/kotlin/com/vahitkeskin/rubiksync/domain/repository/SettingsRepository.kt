package com.vahitkeskin.rubiksync.domain.repository

import com.vahitkeskin.rubiksync.utils.CameraSettings

interface SettingsRepository {
    suspend fun saveThemeMode(mode: String)
    suspend fun loadThemeMode(): String?

    suspend fun saveLanguage(langCode: String)
    suspend fun loadLanguage(): String?

    suspend fun saveCubeEditable(enabled: Boolean)
    suspend fun loadCubeEditable(): Boolean?

    suspend fun saveSoundEnabled(enabled: Boolean)
    suspend fun loadSoundEnabled(): Boolean?

    suspend fun saveShowcaseCompleted(completed: Boolean)
    suspend fun loadShowcaseCompleted(): Boolean?

    suspend fun saveEditorShowcaseCompleted(completed: Boolean)
    suspend fun loadEditorShowcaseCompleted(): Boolean?

    suspend fun saveScannerShowcaseCompleted(completed: Boolean)
    suspend fun loadScannerShowcaseCompleted(): Boolean?

    suspend fun saveShakeToScramble(enabled: Boolean)
    suspend fun loadShakeToScramble(): Boolean?

    suspend fun saveScrambleSoundTooltipShown(shown: Boolean)
    suspend fun loadScrambleSoundTooltipShown(): Boolean?

    suspend fun saveCameraSettings(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    )
    suspend fun loadCameraSettings(): CameraSettings?
}
