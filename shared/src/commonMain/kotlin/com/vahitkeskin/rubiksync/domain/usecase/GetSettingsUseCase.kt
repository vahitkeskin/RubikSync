package com.vahitkeskin.rubiksync.domain.usecase

import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository

data class RubikSettings(
    val themeMode: String?,
    val languageCode: String?,
    val isCubeEditable: Boolean?,
    val isSoundEnabled: Boolean?,
    val isShowcaseCompleted: Boolean?,
    val isShakeToScrambleEnabled: Boolean?
)

class GetSettingsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): RubikSettings {
        val themeMode = repository.loadThemeMode()
        val languageCode = repository.loadLanguage()
        val isCubeEditable = repository.loadCubeEditable()
        val isSoundEnabled = repository.loadSoundEnabled()
        val isShowcaseCompleted = repository.loadShowcaseCompleted()
        val isShakeToScrambleEnabled = repository.loadShakeToScramble()
        
        return RubikSettings(
            themeMode = themeMode,
            languageCode = languageCode,
            isCubeEditable = isCubeEditable,
            isSoundEnabled = isSoundEnabled,
            isShowcaseCompleted = isShowcaseCompleted,
            isShakeToScrambleEnabled = isShakeToScrambleEnabled
        )
    }
}
