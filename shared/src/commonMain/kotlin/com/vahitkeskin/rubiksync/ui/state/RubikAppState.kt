package com.vahitkeskin.rubiksync.ui.state

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.solver.IntVector3
import com.vahitkeskin.rubiksync.solver.AnnotatedMove
import kotlinx.coroutines.CoroutineScope
import com.vahitkeskin.rubiksync.utils.CubiePersistable
import com.vahitkeskin.rubiksync.utils.parseDetectedState
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.vahitkeskin.rubiksync.ui.strings.AppLanguage
import com.vahitkeskin.rubiksync.ui.strings.AppStrings
import com.vahitkeskin.rubiksync.ui.strings.AppStringsMap
import com.vahitkeskin.rubiksync.ui.strings.EnStrings
import com.vahitkeskin.rubiksync.getSystemLanguageCode
import com.vahitkeskin.rubiksync.domain.usecase.*
import org.koin.compose.koinInject

class RubikAppState(
    val cubeState: RubikCubeState,
    val coroutineScope: CoroutineScope,
    private val getCubeStateUseCase: GetCubeStateUseCase,
    private val saveCubeStateUseCase: SaveCubeStateUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val saveThemeUseCase: SaveThemeUseCase,
    private val saveLanguageUseCase: SaveLanguageUseCase,
    private val saveSoundEnabledUseCase: SaveSoundEnabledUseCase,
    private val saveCubeEditableUseCase: SaveCubeEditableUseCase,
    private val saveShowcaseCompletedUseCase: SaveShowcaseCompletedUseCase,
    private val saveEditorShowcaseCompletedUseCase: SaveEditorShowcaseCompletedUseCase,
    private val saveScannerShowcaseCompletedUseCase: SaveScannerShowcaseCompletedUseCase,
    private val getCameraSettingsUseCase: GetCameraSettingsUseCase,
    private val saveCameraSettingsUseCase: SaveCameraSettingsUseCase,
    private val saveShakeToScrambleUseCase: SaveShakeToScrambleUseCase,
    private val saveSolveSessionUseCase: SaveSolveSessionUseCase,
    private val getSolveSessionsUseCase: GetSolveSessionsUseCase
) {
    var solveStartTime by mutableStateOf<Long?>(null)
        private set
    
    var currentSolveDuration by mutableStateOf(0L)
        private set
    
    var solveSessions by mutableStateOf<List<com.vahitkeskin.rubiksync.utils.SolveSession>>(emptyList())
        private set
    
    // Shake to Scramble State
    var isShakeToScrambleEnabled by mutableStateOf(true)
        private set

    // Camera State
    var yaw by mutableStateOf(-0.55f)
        private set
    var pitch by mutableStateOf(0.40f)
        private set
    var cameraDistance by mutableStateOf(10.0f)
        private set
    var panX by mutableStateOf(0f)
        private set
    var panY by mutableStateOf(0f)
        private set

    // Dialog visibility states
    var showEditorDialog by mutableStateOf(false)
        private set
    var showScannerWizard by mutableStateOf(false)
        private set
    var showSplashScreen by mutableStateOf(true)
        private set
    var showSettingsScreen by mutableStateOf(false)
        private set

    // Theme mode state
    var themeMode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    // Theme loading state
    var isThemeLoaded by mutableStateOf(false)
        private set

    // App language state
    var appLanguage by mutableStateOf(
        run {
            val sysCode = getSystemLanguageCode().lowercase()
            AppLanguage.values().find { it.code == sysCode } ?: AppLanguage.EN
        }
    )
        private set

    // Localized strings
    val strings: AppStrings
        get() = AppStringsMap[appLanguage] ?: EnStrings

    // Editor State
    private val defaultFaces = mapOf(
        FaceName.U to Array(3) { Array(3) { CubeColor.ORANGE } },
        FaceName.D to Array(3) { Array(3) { CubeColor.RED } },
        FaceName.L to Array(3) { Array(3) { CubeColor.YELLOW } },
        FaceName.R to Array(3) { Array(3) { CubeColor.WHITE } },
        FaceName.F to Array(3) { Array(3) { CubeColor.GREEN } },
        FaceName.B to Array(3) { Array(3) { CubeColor.BLUE } }
    )

    var editorFaces by mutableStateOf(defaultFaces)
        private set
    var selectedColor by mutableStateOf(CubeColor.ORANGE)
        private set

    // Solution / Playback State
    var activeSolution by mutableStateOf<List<MoveType>?>(null)
        private set

    // List of annotated steps computed by the solver with phase descriptions
    var activeSolutionDetails by mutableStateOf<List<AnnotatedMove>?>(null)
        private set
    var currentSolutionStep by mutableStateOf(0)
        private set
    var isPlaybackRunning by mutableStateOf(false)
        private set

    // Feedback states
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var infoMessage by mutableStateOf<String?>(null)
        private set
    var successMessage by mutableStateOf<String?>(null)
        private set
    var isDetecting by mutableStateOf(false)
        private set
    var isRecalculating by mutableStateOf(false)
        private set

    // Statistics
    var totalMoveCount by mutableStateOf(0)
        private set

    /** When false, the 3D cube can only be orbited/zoomed — no layer turns or panel edits. */
    var isCubeEditable by mutableStateOf(true)
        private set

    var isSoundEnabled by mutableStateOf(true)
        private set

    // Showcase / Onboarding state
    var isShowcaseCompleted by mutableStateOf(false)
        private set
    var showcaseStep by mutableStateOf(0)
        private set
    var targetBounds by mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)
        private set
    var targetCornerRadius by mutableStateOf(16.dp)
        private set

    // Main Cube bounds in root coordinate system for PIP transition
    var mainCubeBounds by mutableStateOf<androidx.compose.ui.geometry.Rect?>(null)
        private set

    var isEditorShowcaseCompleted by mutableStateOf(false)
        private set
    var editorShowcaseStep by mutableStateOf(0)
        private set

    var isScannerShowcaseCompleted by mutableStateOf(false)
        private set
    var scannerShowcaseStep by mutableStateOf(0)
        private set

    // User-triggered active tooltip ID (locks, sounds, shake, etc.)
    var activeTooltipId by mutableStateOf<String?>(null)
        private set


    // Computed: is the cube solved?
    val isSolved: Boolean
        get() {
            return cubeState.cubies.all { cubie ->
                val sum = kotlin.math.abs(cubie.originalPos.x.roundToInt()) +
                          kotlin.math.abs(cubie.originalPos.y.roundToInt()) +
                          kotlin.math.abs(cubie.originalPos.z.roundToInt())
                if (sum < 2) {
                    true
                } else {
                    cubie.gridPos == cubie.originalPos &&
                            cubie.rightBasis.x > 0.9f && cubie.upBasis.y > 0.9f && cubie.forwardBasis.z > 0.9f
                }
            }
        }

    // Computed: is the app in its initial state?
    val isInitialState: Boolean
        get() {
            val isCubeInitial = cubeState.moveHistory.isEmpty() && isSolved
            val isCameraInitial = kotlin.math.abs(yaw - (-0.55f)) < 0.01f &&
                    kotlin.math.abs(pitch - 0.40f) < 0.01f &&
                    kotlin.math.abs(cameraDistance - 10.0f) < 0.01f &&
                    kotlin.math.abs(panX) < 0.01f &&
                    kotlin.math.abs(panY) < 0.01f
            return isCubeInitial && isCameraInitial
        }

    // Scanner Wizard State
    var scannerStep by mutableStateOf(0)
        private set
    var scannedGrids by mutableStateOf(mutableMapOf<FaceName, Array<Array<CubeColor>>>())
        private set

    // 3D vector representation of the scanned raw RGB values for cube stickers
    var scannedRawRGBs by mutableStateOf(mutableMapOf<FaceName, Array<Array<IntVector3>>>())
        private set
    var scannedFilePaths by mutableStateOf(mutableMapOf<FaceName, String>())
        private set

    var gridScales by mutableStateOf(FaceName.values().associateWith { 0.55f }.toMutableMap())
        private set
    var gridOffsetsX by mutableStateOf(FaceName.values().associateWith { 0f }.toMutableMap())
        private set
    var gridOffsetsY by mutableStateOf(FaceName.values().associateWith { 0f }.toMutableMap())
        private set

    private val _manualMoves = mutableStateListOf<MoveType>()
    val manualMoves: List<MoveType> get() = _manualMoves

    // --- Public State Mutators ---

    fun loadStats() {
        coroutineScope.launch(Dispatchers.Default) {
            val sessions = getSolveSessionsUseCase()
            withContext(Dispatchers.Main) {
                solveSessions = sessions
            }
        }
    }

    fun updateYaw(value: Float) { yaw = value }
    fun updatePitch(value: Float) { pitch = value }
    fun updateCameraDistance(value: Float) { cameraDistance = value }
    fun updatePanX(value: Float) { panX = value }
    fun updatePanY(value: Float) { panY = value }

    fun updateCameraOrbit(dy: Float, dp: Float) {
        yaw = (yaw + dy) % (2f * kotlin.math.PI.toFloat())
        pitch = (pitch + dp).coerceIn(-1.4f, 1.4f)
    }

    fun updateCameraZoom(dz: Float) { cameraDistance = (cameraDistance + dz).coerceIn(4f, 12f) }

    fun updateCameraPan(dx: Float, dy: Float) {
        panX += dx
        panY += dy
    }

    fun updateShowEditorDialog(show: Boolean) { showEditorDialog = show }
    fun updateShowScannerWizard(show: Boolean) { showScannerWizard = show }
    fun updateShowSplashScreen(show: Boolean) { showSplashScreen = show }
    fun updateShowSettingsScreen(show: Boolean) { showSettingsScreen = show }

    fun updateEditorFaces(faces: Map<FaceName, Array<Array<CubeColor>>>) {
        editorFaces = faces
        saveCurrentState()
    }

    /**
     * Updates the color of a specific grid cell on a specific face in the editor.
     * This ensures encapsulated state mutation instead of direct array index access
     * from external UI modules.
     */
    fun updateEditorFaceCell(face: FaceName, row: Int, col: Int, color: CubeColor) {
        val updated = editorFaces.toMutableMap()
        val grid = (updated[face] ?: Array(3) { Array(3) { CubeColor.INTERNAL } })
            .map { it.copyOf() }
            .toTypedArray()
        grid[row][col] = color
        updated[face] = grid
        updateEditorFaces(updated)
    }

    /**
     * Resets all editor faces to their default colors (solved state configuration).
     */
    fun resetEditorFaces() {
        updateEditorFaces(defaultFaces)
    }

    /**
     * Attempts to import editor face colors from a JSON-formatted string representation.
     * Parses the input and applies it if valid; otherwise triggers error feedback.
     */
    fun importEditorFacesFromJson(json: String): Boolean {
        val parsed = parseDetectedState(json)
        return if (parsed != null) {
            updateEditorFaces(parsed)
            updateSuccessMessage(strings.jsonImportSuccess)
            true
        } else {
            updateErrorMessage(strings.jsonImportError)
            false
        }
    }

    /**
     * Applies the current editor face configuration to the active cube model state,
     * validating the layout and triggering appropriate success or error alerts.
     */
    fun applyEditorState(onSuccess: () -> Unit) {
        if (cubeState.isAnimating) return
        coroutineScope.launch {
            val success = cubeState.setCustomStateAnimated(editorFaces)
            if (success) {
                clearManualMoves()
                saveCurrentState()
                updateActiveSolution(null)
                updateErrorMessage(null)
                updateSuccessMessage(strings.cubeStateApplied)
                onSuccess()
            } else {
                updateErrorMessage(strings.invalidCubeDesign)
            }
        }
    }

    fun updateSelectedColor(color: CubeColor) { selectedColor = color }
    fun updateActiveSolution(solution: List<MoveType>?) {
        activeSolution = solution
        currentSolutionStep = 0
    }
    fun updateActiveSolutionDetails(details: List<AnnotatedMove>?) { activeSolutionDetails = details }
    fun updateCurrentSolutionStep(step: Int) { currentSolutionStep = step }
    fun incrementSolutionStep() { currentSolutionStep++ }
    fun updatePlaybackRunning(running: Boolean) { isPlaybackRunning = running }
    fun updateErrorMessage(message: String?) { errorMessage = message }
    fun updateInfoMessage(message: String?) { infoMessage = message }
    fun updateSuccessMessage(message: String?) { successMessage = message }
    fun updateDetecting(detecting: Boolean) { isDetecting = detecting }
    fun updateRecalculating(recalculating: Boolean) { isRecalculating = recalculating }
    fun updateTotalMoveCount(count: Int) { totalMoveCount = count }
    fun incrementTotalMoveCount() { totalMoveCount++ }

    fun updateShowcaseStep(step: Int) { showcaseStep = step }
    fun updateTargetBounds(bounds: androidx.compose.ui.geometry.Rect?) { targetBounds = bounds }
    fun updateTargetCornerRadius(radius: Dp) { targetCornerRadius = radius }
    fun updateTargetVisuals(bounds: androidx.compose.ui.geometry.Rect?, radius: Dp) {
        targetBounds = bounds
        targetCornerRadius = radius
    }

    fun updateMainCubeBounds(bounds: androidx.compose.ui.geometry.Rect?) {
        mainCubeBounds = bounds
    }

    fun updateScannerStep(step: Int) { scannerStep = step }
    fun updateScannedGrids(grids: Map<FaceName, Array<Array<CubeColor>>>) { scannedGrids = grids.toMutableMap() }
    fun updateScannedRawRGBs(rgbs: Map<FaceName, Array<Array<IntVector3>>>) { scannedRawRGBs = rgbs.toMutableMap() }
    fun updateScannedFilePaths(paths: Map<FaceName, String>) { scannedFilePaths = paths.toMutableMap() }
    fun updateGridScales(scales: Map<FaceName, Float>) { gridScales = scales.toMutableMap() }
    fun updateGridOffsetsX(offsets: Map<FaceName, Float>) { gridOffsetsX = offsets.toMutableMap() }
    fun updateGridOffsetsY(offsets: Map<FaceName, Float>) { gridOffsetsY = offsets.toMutableMap() }

    fun addManualMove(move: MoveType) { _manualMoves.add(move) }
    fun removeLastManualMove(): Boolean {
        if (_manualMoves.isNotEmpty()) {
            _manualMoves.removeAt(_manualMoves.size - 1)
            return true
        }
        return false
    }
    fun clearManualMoves() { _manualMoves.clear() }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        coroutineScope.launch(Dispatchers.Default) { saveThemeUseCase(mode.name) }
    }

    fun updateLanguage(lang: AppLanguage) {
        appLanguage = lang
        coroutineScope.launch(Dispatchers.Default) { saveLanguageUseCase(lang.code) }
    }

    fun updateCubeEditable(enabled: Boolean) {
        isCubeEditable = enabled
        if (!enabled) isPlaybackRunning = false
        coroutineScope.launch(Dispatchers.Default) { saveCubeEditableUseCase(enabled) }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        coroutineScope.launch(Dispatchers.Default) { saveSoundEnabledUseCase(enabled) }
    }

    fun updateShakeToScramble(enabled: Boolean) {
        isShakeToScrambleEnabled = enabled
        coroutineScope.launch(Dispatchers.Default) { saveShakeToScrambleUseCase(enabled) }
    }

    fun updateShowcaseCompleted(completed: Boolean) {
        isShowcaseCompleted = completed
        coroutineScope.launch(Dispatchers.Default) { saveShowcaseCompletedUseCase(completed) }
    }

    fun advanceShowcase() {
        if (isShowcaseCompleted) return
        val currentStep = showcaseStep
        coroutineScope.launch {
            if (currentStep in 1..11) showcaseStep = -currentStep
            targetBounds = null
            kotlinx.coroutines.delay(1050)
            if (currentStep in 1..10) showcaseStep = currentStep + 1
            else if (currentStep == 11) {
                showcaseStep = 0
                updateShowcaseCompleted(true)
            }
        }
    }

    fun updateEditorShowcaseStep(step: Int) { editorShowcaseStep = step }
    fun updateEditorShowcaseCompleted(completed: Boolean) {
        isEditorShowcaseCompleted = completed
        coroutineScope.launch(Dispatchers.Default) {
            saveEditorShowcaseCompletedUseCase(completed)
        }
    }
    fun showTooltip(id: String) { activeTooltipId = id }
    fun dismissTooltip(id: String) { if (activeTooltipId == id) activeTooltipId = null }
    fun clearActiveTooltip() { activeTooltipId = null }

    fun advanceEditorShowcase(totalSteps: Int = 5) {
        if (isEditorShowcaseCompleted) return
        val currentStep = editorShowcaseStep
        coroutineScope.launch {
            if (currentStep in 1..totalSteps) editorShowcaseStep = -currentStep
            kotlinx.coroutines.delay(1050)
            if (currentStep in 1 until totalSteps) editorShowcaseStep = currentStep + 1
            else if (currentStep == totalSteps) {
                editorShowcaseStep = 0
                updateEditorShowcaseCompleted(true)
            }
        }
    }

    fun updateScannerShowcaseStep(step: Int) { scannerShowcaseStep = step }
    fun updateScannerShowcaseCompleted(completed: Boolean) {
        isScannerShowcaseCompleted = completed
        coroutineScope.launch(Dispatchers.Default) {
            saveScannerShowcaseCompletedUseCase(completed)
        }
    }
    fun advanceScannerShowcase(totalSteps: Int = 6) {
        if (isScannerShowcaseCompleted) return
        val currentStep = scannerShowcaseStep
        coroutineScope.launch {
            if (currentStep in 1..totalSteps) scannerShowcaseStep = -currentStep
            kotlinx.coroutines.delay(1050)
            if (currentStep in 1 until totalSteps) scannerShowcaseStep = currentStep + 1
            else if (currentStep == totalSteps) {
                scannerShowcaseStep = 0
                updateScannerShowcaseCompleted(true)
            }
        }
    }

    fun saveCurrentState() {
        if (isSolved && (cubeState.moveHistory.isNotEmpty() || manualMoves.isNotEmpty())) {
            cubeState.moveHistory.clear()
            _manualMoves.clear()
            totalMoveCount = 0
        }
        coroutineScope.launch(Dispatchers.Default) {
            val persistableCubies = cubeState.cubies.map { c ->
                CubiePersistable(
                    id = c.id,
                    gridX = c.gridPos.x.roundToInt(),
                    gridY = c.gridPos.y.roundToInt(),
                    gridZ = c.gridPos.z.roundToInt(),
                    rightX = c.rightBasis.x.roundToInt(),
                    rightY = c.rightBasis.y.roundToInt(),
                    rightZ = c.rightBasis.z.roundToInt(),
                    upX = c.upBasis.x.roundToInt(),
                    upY = c.upBasis.y.roundToInt(),
                    upZ = c.upBasis.z.roundToInt(),
                    forwardX = c.forwardBasis.x.roundToInt(),
                    forwardY = c.forwardBasis.y.roundToInt(),
                    forwardZ = c.forwardBasis.z.roundToInt()
                )
            }
            saveCubeStateUseCase(
                cubies = persistableCubies,
                moveHistory = cubeState.moveHistory.toList(),
                manualMoves = manualMoves.toList(),
                editorFaces = editorFaces
            )
        }
    }

    fun saveCameraSettings(yaw: Float, pitch: Float, cameraDistance: Float, panX: Float, panY: Float, rotationSpeedMs: Float) {
        coroutineScope.launch(Dispatchers.Default) {
            saveCameraSettingsUseCase(yaw, pitch, cameraDistance, panX, panY, rotationSpeedMs)
        }
    }

    init {
        cubeState.onStateChanged = {
            val startTime = solveStartTime
            if (isSolved && startTime != null) {
                val duration = com.vahitkeskin.rubiksync.currentTimeMillis() - startTime
                solveStartTime = null
                currentSolveDuration = duration
                coroutineScope.launch(Dispatchers.Default) {
                    saveSolveSessionUseCase(duration, totalMoveCount, com.vahitkeskin.rubiksync.currentTimeMillis())
                    loadStats()
                }
            }
            saveCurrentState()
        }

        loadStats()

        coroutineScope.launch(Dispatchers.Default) {
            try {
                val settings = getSettingsUseCase()
                val camera = getCameraSettingsUseCase()

                withContext(Dispatchers.Main) {
                    if (camera != null) cubeState.rotationSpeedMs = camera.rotationSpeedMs
                    if (settings.themeMode != null) themeMode = try { ThemeMode.valueOf(settings.themeMode) } catch (_: Exception) { ThemeMode.SYSTEM }
                    if (settings.languageCode != null) {
                        val sysCode = getSystemLanguageCode().lowercase()
                        val defaultLang = AppLanguage.values().find { it.code == sysCode } ?: AppLanguage.EN
                        appLanguage = AppLanguage.values().find { it.code == settings.languageCode } ?: defaultLang
                    }
                    if (settings.isCubeEditable != null) isCubeEditable = settings.isCubeEditable
                    if (settings.isSoundEnabled != null) isSoundEnabled = settings.isSoundEnabled
                    if (settings.isShakeToScrambleEnabled != null) isShakeToScrambleEnabled = settings.isShakeToScrambleEnabled
                    if (settings.isShowcaseCompleted != null) isShowcaseCompleted = settings.isShowcaseCompleted
                    if (settings.isEditorShowcaseCompleted != null) isEditorShowcaseCompleted = settings.isEditorShowcaseCompleted
                    if (settings.isScannerShowcaseCompleted != null) isScannerShowcaseCompleted = settings.isScannerShowcaseCompleted
                }

                val saved = getCubeStateUseCase()
                if (saved != null) {
                    withContext(Dispatchers.Main) {
                        editorFaces = saved.editorFaces
                    }
                }
            } catch (e: Exception) { e.printStackTrace() } finally {
                withContext(Dispatchers.Main) { isThemeLoaded = true }
            }
        }
    }

    private var timerJob: kotlinx.coroutines.Job? = null

    private var lastToggleTime = 0L

    fun toggleTimer() {
        val now = com.vahitkeskin.rubiksync.currentTimeMillis()
        if (now - lastToggleTime < 300) return
        lastToggleTime = now

        if (solveStartTime != null) {
            // Stop
            solveStartTime = null
            timerJob?.cancel()
            timerJob = null
        } else {
            // Start
            solveStartTime = com.vahitkeskin.rubiksync.currentTimeMillis() - currentSolveDuration
            startTimer()
        }
    }

    fun resetTimer() {
        solveStartTime = null
        currentSolveDuration = 0L
        timerJob?.cancel()
        timerJob = null
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = coroutineScope.launch(Dispatchers.Main) {
            while (true) {
                val startTime = solveStartTime ?: break
                currentSolveDuration = com.vahitkeskin.rubiksync.currentTimeMillis() - startTime
                kotlinx.coroutines.delay(10)
            }
        }
    }
}

@Composable
fun rememberRubikAppState(
    cubeState: RubikCubeState = remember { RubikCubeState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    getCubeStateUseCase: GetCubeStateUseCase = koinInject(),
    saveCubeStateUseCase: SaveCubeStateUseCase = koinInject(),
    getSettingsUseCase: GetSettingsUseCase = koinInject(),
    saveThemeUseCase: SaveThemeUseCase = koinInject(),
    saveLanguageUseCase: SaveLanguageUseCase = koinInject(),
    saveSoundEnabledUseCase: SaveSoundEnabledUseCase = koinInject(),
    saveCubeEditableUseCase: SaveCubeEditableUseCase = koinInject(),
    saveShowcaseCompletedUseCase: SaveShowcaseCompletedUseCase = koinInject(),
    saveEditorShowcaseCompletedUseCase: SaveEditorShowcaseCompletedUseCase = koinInject(),
    saveScannerShowcaseCompletedUseCase: SaveScannerShowcaseCompletedUseCase = koinInject(),
    getCameraSettingsUseCase: GetCameraSettingsUseCase = koinInject(),
    saveCameraSettingsUseCase: SaveCameraSettingsUseCase = koinInject(),
    saveShakeToScrambleUseCase: SaveShakeToScrambleUseCase = koinInject(),
    saveSolveSessionUseCase: SaveSolveSessionUseCase = koinInject(),
    getSolveSessionsUseCase: GetSolveSessionsUseCase = koinInject()
) = remember(
    cubeState, coroutineScope, getCubeStateUseCase, saveCubeStateUseCase, getSettingsUseCase,
    saveThemeUseCase, saveLanguageUseCase, saveSoundEnabledUseCase, saveCubeEditableUseCase,
    saveShowcaseCompletedUseCase, saveEditorShowcaseCompletedUseCase, saveScannerShowcaseCompletedUseCase,
    getCameraSettingsUseCase, saveCameraSettingsUseCase, saveShakeToScrambleUseCase,
    saveSolveSessionUseCase, getSolveSessionsUseCase
) {
    RubikAppState(
        cubeState = cubeState,
        coroutineScope = coroutineScope,
        getCubeStateUseCase = getCubeStateUseCase,
        saveCubeStateUseCase = saveCubeStateUseCase,
        getSettingsUseCase = getSettingsUseCase,
        saveThemeUseCase = saveThemeUseCase,
        saveLanguageUseCase = saveLanguageUseCase,
        saveSoundEnabledUseCase = saveSoundEnabledUseCase,
        saveCubeEditableUseCase = saveCubeEditableUseCase,
        saveShowcaseCompletedUseCase = saveShowcaseCompletedUseCase,
        saveEditorShowcaseCompletedUseCase = saveEditorShowcaseCompletedUseCase,
        saveScannerShowcaseCompletedUseCase = saveScannerShowcaseCompletedUseCase,
        getCameraSettingsUseCase = getCameraSettingsUseCase,
        saveCameraSettingsUseCase = saveCameraSettingsUseCase,
        saveShakeToScrambleUseCase = saveShakeToScrambleUseCase,
        saveSolveSessionUseCase = saveSolveSessionUseCase,
        getSolveSessionsUseCase = getSolveSessionsUseCase
    )
}
