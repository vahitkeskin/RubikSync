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
    private val getCameraSettingsUseCase: GetCameraSettingsUseCase,
    private val saveCameraSettingsUseCase: SaveCameraSettingsUseCase
) {
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

    var isEditorShowcaseCompleted by mutableStateOf(false)
        private set
    var editorShowcaseStep by mutableStateOf(0)
        private set

    var isScannerShowcaseCompleted by mutableStateOf(false)
        private set
    var scannerShowcaseStep by mutableStateOf(0)
        private set

    // Computed: is the cube solved?
    val isSolved: Boolean
        get() {
            return cubeState.cubies.all { cubie ->
                cubie.gridPos == cubie.originalPos &&
                        cubie.rightBasis.x > 0.9f && cubie.upBasis.y > 0.9f && cubie.forwardBasis.z > 0.9f
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

    fun updateYaw(value: Float) {
        yaw = value
    }

    fun updatePitch(value: Float) {
        pitch = value
    }

    fun updateCameraDistance(value: Float) {
        cameraDistance = value
    }

    fun updatePanX(value: Float) {
        panX = value
    }

    fun updatePanY(value: Float) {
        panY = value
    }

    fun updateCameraOrbit(dy: Float, dp: Float) {
        yaw = (yaw + dy) % (2f * kotlin.math.PI.toFloat())
        pitch = (pitch + dp).coerceIn(-1.4f, 1.4f)
    }

    fun updateCameraZoom(dz: Float) {
        cameraDistance = (cameraDistance + dz).coerceIn(4f, 12f)
    }

    fun updateCameraPan(dx: Float, dy: Float) {
        panX += dx
        panY += dy
    }

    fun updateShowEditorDialog(show: Boolean) {
        showEditorDialog = show
    }

    fun updateShowScannerWizard(show: Boolean) {
        showScannerWizard = show
    }

    fun updateShowSplashScreen(show: Boolean) {
        showSplashScreen = show
    }

    fun updateShowSettingsScreen(show: Boolean) {
        showSettingsScreen = show
    }

    fun updateEditorFaces(faces: Map<FaceName, Array<Array<CubeColor>>>) {
        editorFaces = faces
        saveCurrentState()
    }

    fun updateSelectedColor(color: CubeColor) {
        selectedColor = color
    }

    fun updateActiveSolution(solution: List<MoveType>?) {
        activeSolution = solution
    }

    fun updateActiveSolutionDetails(details: List<AnnotatedMove>?) {
        activeSolutionDetails = details
    }

    fun updateCurrentSolutionStep(step: Int) {
        currentSolutionStep = step
    }

    fun incrementSolutionStep() {
        currentSolutionStep++
    }

    fun updatePlaybackRunning(running: Boolean) {
        isPlaybackRunning = running
    }

    fun updateErrorMessage(message: String?) {
        errorMessage = message
    }

    fun updateInfoMessage(message: String?) {
        infoMessage = message
    }

    fun updateSuccessMessage(message: String?) {
        successMessage = message
    }

    fun updateDetecting(detecting: Boolean) {
        isDetecting = detecting
    }

    fun updateRecalculating(recalculating: Boolean) {
        isRecalculating = recalculating
    }

    fun updateTotalMoveCount(count: Int) {
        totalMoveCount = count
    }

    fun incrementTotalMoveCount() {
        totalMoveCount++
    }

    fun updateShowcaseStep(step: Int) {
        showcaseStep = step
    }

    fun updateTargetBounds(bounds: androidx.compose.ui.geometry.Rect?) {
        targetBounds = bounds
    }

    fun updateTargetCornerRadius(radius: Dp) {
        targetCornerRadius = radius
    }

    fun updateTargetVisuals(bounds: androidx.compose.ui.geometry.Rect?, radius: Dp) {
        targetBounds = bounds
        targetCornerRadius = radius
    }

    fun updateScannerStep(step: Int) {
        scannerStep = step
    }

    fun updateScannedGrids(grids: Map<FaceName, Array<Array<CubeColor>>>) {
        scannedGrids = grids.toMutableMap()
    }

    fun updateScannedRawRGBs(rgbs: Map<FaceName, Array<Array<IntVector3>>>) {
        scannedRawRGBs = rgbs.toMutableMap()
    }

    fun updateScannedFilePaths(paths: Map<FaceName, String>) {
        scannedFilePaths = paths.toMutableMap()
    }

    fun updateGridScales(scales: Map<FaceName, Float>) {
        gridScales = scales.toMutableMap()
    }

    fun updateGridOffsetsX(offsets: Map<FaceName, Float>) {
        gridOffsetsX = offsets.toMutableMap()
    }

    fun updateGridOffsetsY(offsets: Map<FaceName, Float>) {
        gridOffsetsY = offsets.toMutableMap()
    }

    fun addManualMove(move: MoveType) {
        _manualMoves.add(move)
    }

    fun removeLastManualMove(): Boolean {
        if (_manualMoves.isNotEmpty()) {
            _manualMoves.removeAt(_manualMoves.size - 1)
            return true
        }
        return false
    }

    fun clearManualMoves() {
        _manualMoves.clear()
    }

    fun updateThemeMode(mode: ThemeMode) {
        themeMode = mode
        coroutineScope.launch(Dispatchers.Default) {
            saveThemeUseCase(mode.name)
        }
    }

    fun updateLanguage(lang: AppLanguage) {
        appLanguage = lang
        coroutineScope.launch(Dispatchers.Default) {
            saveLanguageUseCase(lang.code)
        }
    }

    fun updateCubeEditable(enabled: Boolean) {
        isCubeEditable = enabled
        if (!enabled) {
            isPlaybackRunning = false
        }
        coroutineScope.launch(Dispatchers.Default) {
            saveCubeEditableUseCase(enabled)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        isSoundEnabled = enabled
        coroutineScope.launch(Dispatchers.Default) {
            saveSoundEnabledUseCase(enabled)
        }
    }

    fun updateShowcaseCompleted(completed: Boolean) {
        isShowcaseCompleted = completed
        coroutineScope.launch(Dispatchers.Default) {
            saveShowcaseCompletedUseCase(completed)
        }
    }

    fun advanceShowcase() {
        if (isShowcaseCompleted) return
        val currentStep = showcaseStep
        coroutineScope.launch {
            if (currentStep in 1..10) {
                showcaseStep = -currentStep
            }
            targetBounds = null
            kotlinx.coroutines.delay(1050)
            if (currentStep in 1..9) {
                showcaseStep = currentStep + 1
            } else if (currentStep == 10) {
                showcaseStep = 0
                updateShowcaseCompleted(true)
            }
        }
    }

    fun updateEditorShowcaseStep(step: Int) {
        editorShowcaseStep = step
    }

    fun updateEditorShowcaseCompleted(completed: Boolean) {
        isEditorShowcaseCompleted = completed
    }

    fun advanceEditorShowcase(totalSteps: Int = 5) {
        if (isEditorShowcaseCompleted) return
        val currentStep = editorShowcaseStep
        coroutineScope.launch {
            if (currentStep in 1..totalSteps) {
                editorShowcaseStep = -currentStep
            }
            kotlinx.coroutines.delay(200)
            if (currentStep in 1 until totalSteps) {
                editorShowcaseStep = currentStep + 1
            } else if (currentStep == totalSteps) {
                editorShowcaseStep = 0
                isEditorShowcaseCompleted = true
            }
        }
    }

    fun updateScannerShowcaseStep(step: Int) {
        scannerShowcaseStep = step
    }

    fun updateScannerShowcaseCompleted(completed: Boolean) {
        isScannerShowcaseCompleted = completed
    }

    fun advanceScannerShowcase(totalSteps: Int = 6) {
        if (isScannerShowcaseCompleted) return
        val currentStep = scannerShowcaseStep
        coroutineScope.launch {
            if (currentStep in 1..totalSteps) {
                scannerShowcaseStep = -currentStep
            }
            kotlinx.coroutines.delay(200)
            if (currentStep in 1 until totalSteps) {
                scannerShowcaseStep = currentStep + 1
            } else if (currentStep == totalSteps) {
                scannerShowcaseStep = 0
                isScannerShowcaseCompleted = true
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

    fun saveCameraSettings(
        yaw: Float,
        pitch: Float,
        cameraDistance: Float,
        panX: Float,
        panY: Float,
        rotationSpeedMs: Float
    ) {
        coroutineScope.launch(Dispatchers.Default) {
            saveCameraSettingsUseCase(yaw, pitch, cameraDistance, panX, panY, rotationSpeedMs)
        }
    }

    init {
        cubeState.onStateChanged = { saveCurrentState() }

        coroutineScope.launch(Dispatchers.Default) {
            try {
                val settings = getSettingsUseCase()
                val camera = getCameraSettingsUseCase()

                withContext(Dispatchers.Main) {
                    if (camera != null) {
                        cubeState.rotationSpeedMs = camera.rotationSpeedMs
                    }

                    if (settings.themeMode != null) {
                        val mode = try {
                            ThemeMode.valueOf(settings.themeMode)
                        } catch (_: Exception) {
                            ThemeMode.SYSTEM
                        }
                        themeMode = mode
                    }

                    if (settings.languageCode != null) {
                        val sysCode = getSystemLanguageCode().lowercase()
                        val defaultLang =
                            AppLanguage.values().find { it.code == sysCode } ?: AppLanguage.EN
                        val lang = AppLanguage.values().find { it.code == settings.languageCode } ?: defaultLang
                        appLanguage = lang
                    }

                    if (settings.isCubeEditable != null) {
                        isCubeEditable = settings.isCubeEditable
                    }

                    if (settings.isSoundEnabled != null) {
                        isSoundEnabled = settings.isSoundEnabled
                    }

                    if (settings.isShowcaseCompleted != null) {
                        isShowcaseCompleted = settings.isShowcaseCompleted
                    }
                }

                val saved = getCubeStateUseCase()
                if (saved != null) {
                    withContext(Dispatchers.Main) {
                        editorFaces = saved.editorFaces
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    isThemeLoaded = true
                }
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
    getCameraSettingsUseCase: GetCameraSettingsUseCase = koinInject(),
    saveCameraSettingsUseCase: SaveCameraSettingsUseCase = koinInject()
) = remember(
    cubeState,
    coroutineScope,
    getCubeStateUseCase,
    saveCubeStateUseCase,
    getSettingsUseCase,
    saveThemeUseCase,
    saveLanguageUseCase,
    saveSoundEnabledUseCase,
    saveCubeEditableUseCase,
    saveShowcaseCompletedUseCase,
    getCameraSettingsUseCase,
    saveCameraSettingsUseCase
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
        getCameraSettingsUseCase = getCameraSettingsUseCase,
        saveCameraSettingsUseCase = saveCameraSettingsUseCase
    )
}
