package com.vahitkeskin.rubiksync

import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.domain.repository.CubeRepository
import com.vahitkeskin.rubiksync.domain.repository.SettingsRepository
import com.vahitkeskin.rubiksync.domain.usecase.*
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.ThemeMode
import com.vahitkeskin.rubiksync.ui.strings.AppLanguage
import com.vahitkeskin.rubiksync.ui.strings.AppStringsMap
import com.vahitkeskin.rubiksync.ui.strings.EnStrings
import com.vahitkeskin.rubiksync.utils.CameraSettings
import com.vahitkeskin.rubiksync.utils.CubiePersistable
import com.vahitkeskin.rubiksync.utils.LoadedCubeState
import com.vahitkeskin.rubiksync.utils.SolveSession
import com.vahitkeskin.rubiksync.solver.RubikSolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * World-class, highly documented Kotlin Multiplatform Unit Test Suite for RubikSync.
 *
 * This suite verifies the core business logic, state mutations, and algorithm processing
 * for all user-facing features (scrambling, solving, resetting, themes, languages, and timers).
 *
 * --- HOW TO RUN SPECIFIC TESTS / ACTIONS ---
 *
 * You can execute individual test functions directly using the Gradle '--tests' filter flag:
 *
 * 1. Scramble Action Test:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testScrambleAction"
 *
 * 2. Solve Action Test:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testSolveAction"
 *
 * 3. Reset Action Test:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testResetAction"
 *
 * 4. Language Change Action Test:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testChangeLanguageAction"
 *
 * 5. Theme Change Action Test:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testChangeThemeAction"
 *
 * 6. Sound Action Test:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testSoundSettingsAction"
 *
 * 7. Timer Action Test:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUnitTest.testTimerAction"
 *
 * 8. Run All Tests:
 *    ./gradlew :shared:jvmTest
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RubikUnitTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- In-Memory Repository Fakes ---
    private class FakeCubeRepository : CubeRepository {
        var savedState: LoadedCubeState? = null
        override suspend fun saveCubeState(
            cubies: List<CubiePersistable>,
            moveHistory: List<MoveType>,
            manualMoves: List<MoveType>,
            editorFaces: Map<FaceName, Array<Array<CubeColor>>>
        ) {
            savedState = LoadedCubeState(cubies, moveHistory, manualMoves, editorFaces)
        }
        override suspend fun loadCubeState(): LoadedCubeState? = savedState
    }

    private class FakeSettingsRepository : SettingsRepository {
        var themeMode: String? = "SYSTEM"
        var languageCode: String? = "en"
        var cubeEditable: Boolean? = true
        var soundEnabled: Boolean? = true
        var showcaseCompleted: Boolean? = false
        var editorShowcaseCompleted: Boolean? = false
        var scannerShowcaseCompleted: Boolean? = false
        var shakeToScramble: Boolean? = true
        var tooltipShown: Boolean? = false
        var cameraSettings: CameraSettings? = null

        override suspend fun saveThemeMode(mode: String) { themeMode = mode }
        override suspend fun loadThemeMode(): String? = themeMode
        override suspend fun saveLanguage(langCode: String) { languageCode = langCode }
        override suspend fun loadLanguage(): String? = languageCode
        override suspend fun saveCubeEditable(enabled: Boolean) { cubeEditable = enabled }
        override suspend fun loadCubeEditable(): Boolean? = cubeEditable
        override suspend fun saveSoundEnabled(enabled: Boolean) { soundEnabled = enabled }
        override suspend fun loadSoundEnabled(): Boolean? = soundEnabled
        override suspend fun saveShowcaseCompleted(completed: Boolean) { showcaseCompleted = completed }
        override suspend fun loadShowcaseCompleted(): Boolean? = showcaseCompleted
        override suspend fun saveEditorShowcaseCompleted(completed: Boolean) { editorShowcaseCompleted = completed }
        override suspend fun loadEditorShowcaseCompleted(): Boolean? = editorShowcaseCompleted
        override suspend fun saveScannerShowcaseCompleted(completed: Boolean) { scannerShowcaseCompleted = completed }
        override suspend fun loadScannerShowcaseCompleted(): Boolean? = scannerShowcaseCompleted
        override suspend fun saveShakeToScramble(enabled: Boolean) { shakeToScramble = enabled }
        override suspend fun loadShakeToScramble(): Boolean? = shakeToScramble
        override suspend fun saveScrambleSoundTooltipShown(shown: Boolean) { tooltipShown = shown }
        override suspend fun loadScrambleSoundTooltipShown(): Boolean? = tooltipShown
        override suspend fun saveCameraSettings(
            yaw: Float, pitch: Float, cameraDistance: Float, panX: Float, panY: Float, rotationSpeedMs: Float
        ) {
            cameraSettings = CameraSettings(yaw, pitch, cameraDistance, panX, panY, rotationSpeedMs)
        }
        override suspend fun loadCameraSettings(): CameraSettings? = cameraSettings
    }

    private fun createTestAppState(
        cubeState: RubikCubeState,
        coroutineScope: CoroutineScope,
        settingsRepo: FakeSettingsRepository = FakeSettingsRepository(),
        cubeRepo: FakeCubeRepository = FakeCubeRepository()
    ): RubikAppState {
        return RubikAppState(
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
            saveScrambleSoundTooltipShownUseCase = SaveScrambleSoundTooltipShownUseCase(settingsRepo),
            saveSolveSessionUseCase = SaveSolveSessionUseCase(),
            getSolveSessionsUseCase = GetSolveSessionsUseCase()
        )
    }

    private suspend fun waitUntilLoaded(appState: RubikAppState) {
        while (!appState.isThemeLoaded) {
            kotlinx.coroutines.delay(5)
        }
    }

    private suspend fun scrambleCubeWithoutAnimation(cubeState: RubikCubeState, turns: Int = 10) {
        val moves = MoveType.entries
        var lastMove: MoveType? = null
        repeat(turns) {
            var move = moves.random()
            while (lastMove != null && move.axis == lastMove.axis && move.layerValue == lastMove.layerValue && move.angleSign == -lastMove.angleSign) {
                move = moves.random()
            }
            cubeState.executeMove(move, skipAnimation = true)
            lastMove = move
        }
    }

    @Test
    fun testInitialState() = runTest {
        val cubeState = RubikCubeState()
        val appState = createTestAppState(cubeState, this)
        waitUntilLoaded(appState)
        
        assertTrue(appState.isSolved, "Initially created cube must be solved")
        assertTrue(appState.isInitialState, "AppState should report initial default state")
    }

    @Test
    fun testScrambleAction() = runTest {
        val cubeState = RubikCubeState()
        val appState = createTestAppState(cubeState, this)
        waitUntilLoaded(appState)
        
        scrambleCubeWithoutAnimation(cubeState, turns = 10)
        
        assertFalse(appState.isSolved, "Cube must not be solved after a scramble action")
        assertFalse(appState.isInitialState, "AppState should no longer be in initial state after scramble")
        assertTrue(cubeState.moveHistory.isNotEmpty(), "Move history must contain scrambled movements")
    }

    @Test
    fun testResetAction() = runTest {
        val cubeState = RubikCubeState()
        val appState = createTestAppState(cubeState, this)
        waitUntilLoaded(appState)
        
        scrambleCubeWithoutAnimation(cubeState, turns = 5)
        assertFalse(appState.isSolved)
        
        cubeState.reset()
        
        assertTrue(appState.isSolved, "Cube must be solved after a reset action")
        assertTrue(cubeState.moveHistory.isEmpty(), "Move history must be cleared after reset")
    }

    @Test
    fun testSolveAction() = runTest {
        val cubeState = RubikCubeState()
        val appState = createTestAppState(cubeState, this)
        waitUntilLoaded(appState)
        
        cubeState.executeMove(MoveType.U, skipAnimation = true)
        cubeState.executeMove(MoveType.R, skipAnimation = true)
        cubeState.executeMove(MoveType.F, skipAnimation = true)
        assertFalse(appState.isSolved, "Cube must be unsolved after manual moves")

        val solver = RubikSolver()
        val solutions = solver.solveAnnotated(cubeState)
        assertNotNull(solutions, "Solver must return a valid solution sequence")
        
        solutions.forEach { annotated ->
            cubeState.executeMove(annotated.move, skipAnimation = true)
        }

        assertTrue(appState.isSolved, "Cube must be successfully solved after executing solver solution")
    }

    @Test
    fun testChangeLanguageAction() = runTest {
        val cubeState = RubikCubeState()
        val appState = createTestAppState(cubeState, this)
        waitUntilLoaded(appState)
        
        appState.updateLanguage(AppLanguage.TR)
        assertEquals(AppLanguage.TR, appState.appLanguage, "Language should be TR")
        assertEquals(AppStringsMap[AppLanguage.TR]?.appTitle, appState.strings.appTitle, "Strings should resolve to Turkish locale")
        
        appState.updateLanguage(AppLanguage.EN)
        assertEquals(AppLanguage.EN, appState.appLanguage, "Language should be EN")
        assertEquals(AppStringsMap[AppLanguage.EN]?.appTitle, appState.strings.appTitle, "Strings should resolve to English locale")
    }

    @Test
    fun testChangeThemeAction() = runTest {
        val cubeState = RubikCubeState()
        val settingsRepo = FakeSettingsRepository()
        val appState = createTestAppState(cubeState, this, settingsRepo)
        waitUntilLoaded(appState)
        
        appState.updateThemeMode(ThemeMode.DARK)
        assertEquals(ThemeMode.DARK, appState.themeMode, "Theme mode must be updated to DARK")
        
        appState.updateThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, appState.themeMode, "Theme mode must be updated to LIGHT")
    }

    @Test
    fun testSoundSettingsAction() = runTest {
        val cubeState = RubikCubeState()
        val appState = createTestAppState(cubeState, this)
        waitUntilLoaded(appState)
        
        appState.updateSoundEnabled(false)
        assertFalse(appState.isSoundEnabled, "Sound should be disabled")
        
        appState.updateSoundEnabled(true)
        assertTrue(appState.isSoundEnabled, "Sound should be enabled")
    }

    @Test
    fun testTimerAction() = runTest {
        val cubeState = RubikCubeState()
        val appState = createTestAppState(cubeState, this)
        waitUntilLoaded(appState)
        
        assertEquals(0L, appState.currentSolveDuration)
        
        appState.toggleTimer()
        assertNotNull(appState.solveStartTime, "Solve start time must be initialized when timer starts")
        
        appState.resetTimer()
        assertEquals(0L, appState.currentSolveDuration, "Timer duration must reset to 0")
        assertTrue(appState.solveStartTime == null, "Timer should be stopped (solveStartTime is null)")
    }
}
