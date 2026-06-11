package com.vahitkeskin.rubiksync.data.repository

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository
import com.vahitkeskin.rubiksync.utils.CameraSettings
import com.vahitkeskin.rubiksync.utils.RubikPersistence

class SettingsRepositoryImpl(
    private val persistence: RubikPersistence
) : SettingsRepository {

    override suspend fun saveThemeMode(mode: String) {
        persistence.saveThemeMode(mode)
    }

    override suspend fun loadThemeMode(): String? {
        return persistence.loadThemeMode()
    }

    override suspend fun saveLanguage(langCode: String) {
        persistence.saveLanguage(langCode)
    }

    override suspend fun loadLanguage(): String? {
        return persistence.loadLanguage()
    }

    override suspend fun saveCubeEditable(enabled: Boolean) {
        persistence.saveCubeEditable(enabled)
    }

    override suspend fun loadCubeEditable(): Boolean? {
        return persistence.loadCubeEditable()
    }

    override suspend fun saveSoundEnabled(enabled: Boolean) {
        persistence.saveSoundEnabled(enabled)
    }

    override suspend fun loadSoundEnabled(): Boolean? {
        return persistence.loadSoundEnabled()
    }

    override suspend fun saveShowcaseCompleted(completed: Boolean) {
        persistence.saveShowcaseCompleted(completed)
    }

    override suspend fun loadShowcaseCompleted(): Boolean? {
        return persistence.loadShowcaseCompleted()
    }

    override suspend fun saveEditorShowcaseCompleted(completed: Boolean) {
        persistence.saveEditorShowcaseCompleted(completed)
    }

    override suspend fun loadEditorShowcaseCompleted(): Boolean? {
        return persistence.loadEditorShowcaseCompleted()
    }

    override suspend fun saveScannerShowcaseCompleted(completed: Boolean) {
        persistence.saveScannerShowcaseCompleted(completed)
    }

    override suspend fun loadScannerShowcaseCompleted(): Boolean? {
        return persistence.loadScannerShowcaseCompleted()
    }

    override suspend fun saveShakeToScramble(enabled: Boolean) {
        persistence.saveShakeToScramble(enabled)
    }

    override suspend fun loadShakeToScramble(): Boolean? {
        return persistence.loadShakeToScramble()
    }

    override suspend fun saveScrambleSoundTooltipShown(shown: Boolean) {
        persistence.saveScrambleSoundTooltipShown(shown)
    }

    override suspend fun loadScrambleSoundTooltipShown(): Boolean? {
        return persistence.loadScrambleSoundTooltipShown()
    }

    override suspend fun saveCameraSettings(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    ) {
        persistence.saveCameraSettings(yaw, pitch, cameraDistance, panX, panY, rotationSpeedMs)
    }

    override suspend fun loadCameraSettings(): CameraSettings? {
        return persistence.loadCameraSettings()
    }
}
