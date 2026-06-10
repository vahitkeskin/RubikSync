package com.vahitkeskin.rubiksync.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.domain.repository.CubeRepository
import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository
import com.vahitkeskin.rubiksync.domain.usecase.*
import com.vahitkeskin.rubiksync.utils.CameraSettings
import com.vahitkeskin.rubiksync.utils.CubiePersistable
import com.vahitkeskin.rubiksync.utils.LoadedCubeState
import com.vahitkeskin.rubiksync.utils.RubikPersistence
import kotlinx.coroutines.CoroutineScope

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private class FakeRubikPersistence : RubikPersistence {
    override suspend fun saveCubeState(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    ) {}
    override suspend fun loadCubeState(): LoadedCubeState? = null
    
    override suspend fun saveCameraSettings(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    ) {}
    override suspend fun loadCameraSettings(): CameraSettings? = null

    override suspend fun saveThemeMode(mode: String) {}
    override suspend fun loadThemeMode(): String? = "SYSTEM"

    override suspend fun saveLanguage(langCode: String) {}
    override suspend fun loadLanguage(): String? = "en"

    override suspend fun saveCubeEditable(enabled: Boolean) {}
    override suspend fun loadCubeEditable(): Boolean? = true

    override suspend fun saveSoundEnabled(enabled: Boolean) {}
    override suspend fun loadSoundEnabled(): Boolean? = true

    override suspend fun saveShowcaseCompleted(completed: Boolean) {}
    override suspend fun loadShowcaseCompleted(): Boolean? = true

    override suspend fun saveEditorShowcaseCompleted(completed: Boolean) {}
    override suspend fun loadEditorShowcaseCompleted(): Boolean? = true

    override suspend fun saveScannerShowcaseCompleted(completed: Boolean) {}
    override suspend fun loadScannerShowcaseCompleted(): Boolean? = true

    override suspend fun saveShakeToScramble(enabled: Boolean) {}
    override suspend fun loadShakeToScramble(): Boolean? = true
    override suspend fun saveSolveSession(durationMillis: Long, moveCount: Int, timestamp: Long) {}
    override suspend fun loadSolveSessions(): List<com.vahitkeskin.rubiksync.utils.SolveSession> = emptyList()
}

fun initializePreviewPersistence() {
    if (com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry.persistence == null) {
        com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry.persistence = FakeRubikPersistence()
    }
}


private class FakeCubeRepository : CubeRepository {
    override suspend fun saveCubeState(
        cubies: List<CubiePersistable>,
        moveHistory: List<MoveType>,
        manualMoves: List<MoveType>,
        editorFaces: Map<FaceName, Array<Array<CubeColor>>>
    ) {}
    override suspend fun loadCubeState(): LoadedCubeState? = null
}

private class FakeSettingsRepository : SettingsRepository {
    override suspend fun saveThemeMode(mode: String) {}
    override suspend fun loadThemeMode(): String? = "SYSTEM"

    override suspend fun saveLanguage(langCode: String) {}
    override suspend fun loadLanguage(): String? = "en"

    override suspend fun saveCubeEditable(enabled: Boolean) {}
    override suspend fun loadCubeEditable(): Boolean? = true

    override suspend fun saveSoundEnabled(enabled: Boolean) {}
    override suspend fun loadSoundEnabled(): Boolean? = true

    override suspend fun saveShowcaseCompleted(completed: Boolean) {}
    override suspend fun loadShowcaseCompleted(): Boolean? = true

    override suspend fun saveEditorShowcaseCompleted(completed: Boolean) {}
    override suspend fun loadEditorShowcaseCompleted(): Boolean? = true

    override suspend fun saveScannerShowcaseCompleted(completed: Boolean) {}
    override suspend fun loadScannerShowcaseCompleted(): Boolean? = true

    override suspend fun saveShakeToScramble(enabled: Boolean) {}
    override suspend fun loadShakeToScramble(): Boolean? = true

    override suspend fun saveCameraSettings(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    ) {}
    override suspend fun loadCameraSettings(): CameraSettings? = null
}

@Composable
fun rememberPreviewRubikAppState(
    cubeState: RubikCubeState = remember { RubikCubeState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): RubikAppState {
    val cubeRepo = remember { FakeCubeRepository() }
    val settingsRepo = remember { FakeSettingsRepository() }
    
    return remember(cubeState, coroutineScope) {
        RubikAppState(
            cubeState = cubeState,
            coroutineScope = coroutineScope,
            getCubeStateUseCase = GetCubeStateUseCase(cubeRepo),
            saveCubeStateUseCase = SaveCubeStateUseCase(cubeRepo),
            getSettingsUseCase = GetSettingsUseCase(settingsRepo),
            saveThemeUseCase = SaveThemeUseCase(settingsRepo),
            saveLanguageUseCase = SaveLanguageUseCase(settingsRepo),
            saveSoundEnabledUseCase = SaveSoundEnabledUseCase(settingsRepo),
            saveCubeEditableUseCase = SaveCubeEditableUseCase(settingsRepo),
            saveShowcaseCompletedUseCase = SaveShowcaseCompletedUseCase(settingsRepo),
            saveEditorShowcaseCompletedUseCase = SaveEditorShowcaseCompletedUseCase(settingsRepo),
            saveScannerShowcaseCompletedUseCase = SaveScannerShowcaseCompletedUseCase(settingsRepo),
            getCameraSettingsUseCase = GetCameraSettingsUseCase(settingsRepo),
            saveCameraSettingsUseCase = SaveCameraSettingsUseCase(settingsRepo),
            saveShakeToScrambleUseCase = SaveShakeToScrambleUseCase(settingsRepo),
            saveSolveSessionUseCase = SaveSolveSessionUseCase(),
            getSolveSessionsUseCase = GetSolveSessionsUseCase()
        ).apply {
            // Mark theme as loaded to bypass loading screens
            updateShowSplashScreen(false)
        }
    }
}

@Composable
fun PreviewRubikTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (isDark) DarkRubikColors else LightRubikColors
    val colorScheme = if (isDark) {
        androidx.compose.material3.darkColorScheme(
            primary = AccentOrange,
            onPrimary = Color.White,
            secondary = AccentBlue,
            onSecondary = Color.White,
            tertiary = AccentGreen,
            background = DarkBgPrimary,
            surface = DarkBgTertiary,
            surfaceVariant = DarkBgQuaternary,
            onBackground = Color.White,
            onSurface = Color.White,
            outline = DarkCardBorder
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = AccentOrange,
            onPrimary = Color.White,
            secondary = AccentBlueBright,
            onSecondary = Color.White,
            tertiary = AccentGreenSuccess,
            background = LightBgPrimary,
            surface = Color.White,
            surfaceVariant = LightBgTertiary,
            onBackground = DarkThemePrimary,
            onSurface = DarkThemePrimary,
            outline = LightBorderPrimary
        )
    }
    ProvideRubikColors(colors = colors) {
        androidx.compose.material3.MaterialTheme(colorScheme = colorScheme) {
            content()
        }
    }
}

/**
 * A beautifully designed container that showcases a Composable component
 * under both Light and Dark themes side-by-side in a single preview representation.
 */
@Composable
fun ThemeDualPreview(
    modifier: Modifier = Modifier,
    label: String? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate900) // Sleek dark backdrop slate color
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (label != null) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Light Theme Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightBgPrimary)
                    .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "☀️ LIGHT THEME",
                    color = Slate500,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                PreviewRubikTheme(isDark = false) {
                    content()
                }
            }

            // Dark Theme Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkBgPrimary)
                    .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🌙 DARK THEME",
                    color = Slate400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                PreviewRubikTheme(isDark = true) {
                    content()
                }
            }
        }
    }
}
