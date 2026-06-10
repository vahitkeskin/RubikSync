package com.vahitkeskin.rubiksync.di

import com.vahitkeskin.rubiksync.data.repository.CubeRepositoryImpl
import com.vahitkeskin.rubiksync.data.repository.SettingsRepositoryImpl
import com.vahitkeskin.rubiksync.domain.repository.CubeRepository
import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository
import com.vahitkeskin.rubiksync.domain.usecase.*
import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry
import org.koin.dsl.module

val appModule = module {
    // Provide persistence dynamically via the registry
    single { 
        RubikPersistenceRegistry.persistence 
            ?: throw IllegalStateException("RubikPersistence has not been initialized in RubikPersistenceRegistry") 
    }

    // Provide repositories
    single<CubeRepository> { CubeRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }

    // Provide Use Cases
    factory { GetCubeStateUseCase(get()) }
    factory { SaveCubeStateUseCase(get()) }
    factory { GetSettingsUseCase(get()) }
    factory { SaveThemeUseCase(get()) }
    factory { SaveLanguageUseCase(get()) }
    factory { SaveSoundEnabledUseCase(get()) }
    factory { SaveCubeEditableUseCase(get()) }
    factory { SaveShowcaseCompletedUseCase(get()) }
    factory { SaveEditorShowcaseCompletedUseCase(get()) }
    factory { SaveScannerShowcaseCompletedUseCase(get()) }
    factory { SaveShakeToScrambleUseCase(get()) }
    factory { GetCameraSettingsUseCase(get()) }
    factory { SaveCameraSettingsUseCase(get()) }
    factory { SaveSolveSessionUseCase() }
    factory { GetSolveSessionsUseCase() }
}
