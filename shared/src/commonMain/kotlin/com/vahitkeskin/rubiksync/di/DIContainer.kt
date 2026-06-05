package com.vahitkeskin.rubiksync.di

import com.vahitkeskin.rubiksync.data.repository.CubeRepositoryImpl
import com.vahitkeskin.rubiksync.data.repository.SettingsRepositoryImpl
import com.vahitkeskin.rubiksync.domain.repository.CubeRepository
import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository
import com.vahitkeskin.rubiksync.domain.usecase.*
import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry

object DIContainer {
    private val persistence get() = RubikPersistenceRegistry.persistence 
        ?: throw IllegalStateException("RubikPersistence has not been initialized in RubikPersistenceRegistry")

    val cubeRepository: CubeRepository by lazy { CubeRepositoryImpl(persistence) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepositoryImpl(persistence) }

    val getCubeStateUseCase by lazy { GetCubeStateUseCase(cubeRepository) }
    val saveCubeStateUseCase by lazy { SaveCubeStateUseCase(cubeRepository) }

    val getSettingsUseCase by lazy { GetSettingsUseCase(settingsRepository) }
    val saveThemeUseCase by lazy { SaveThemeUseCase(settingsRepository) }
    val saveLanguageUseCase by lazy { SaveLanguageUseCase(settingsRepository) }
    val saveSoundEnabledUseCase by lazy { SaveSoundEnabledUseCase(settingsRepository) }
    val saveCubeEditableUseCase by lazy { SaveCubeEditableUseCase(settingsRepository) }
    val saveShowcaseCompletedUseCase by lazy { SaveShowcaseCompletedUseCase(settingsRepository) }
    val getCameraSettingsUseCase by lazy { GetCameraSettingsUseCase(settingsRepository) }
    val saveCameraSettingsUseCase by lazy { SaveCameraSettingsUseCase(settingsRepository) }
}
