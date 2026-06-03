package com.vahitkeskin.rubiksync.ui.state

import androidx.compose.runtime.*
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.IntVector3
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.cube.AnnotatedMove
import kotlinx.coroutines.CoroutineScope
import com.vahitkeskin.rubiksync.utils.CubiePersistable
import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RubikAppState(
    val cubeState: RubikCubeState,
    val coroutineScope: CoroutineScope
) {
    // Camera State
    var yaw by mutableStateOf(-0.55f)       // Initial yaw - viewing top-left-front
    var pitch by mutableStateOf(0.40f)      // Initial pitch
    var cameraDistance by mutableStateOf(10.0f)
    var panX by mutableStateOf(0f)
    var panY by mutableStateOf(0f)

    // Dialog visibility states
    var showEditorDialog by mutableStateOf(false)
    var showScannerWizard by mutableStateOf(false)
    var showSplashScreen by mutableStateOf(true)

    // Editor State
    private val defaultFaces = mapOf(
        FaceName.U to Array(3) { Array(3) { CubeColor.ORANGE } },
        FaceName.D to Array(3) { Array(3) { CubeColor.RED } },
        FaceName.L to Array(3) { Array(3) { CubeColor.YELLOW } },
        FaceName.R to Array(3) { Array(3) { CubeColor.WHITE } },
        FaceName.F to Array(3) { Array(3) { CubeColor.GREEN } },
        FaceName.B to Array(3) { Array(3) { CubeColor.BLUE } }
    )

    private var _editorFaces by mutableStateOf(defaultFaces)
    var editorFaces: Map<FaceName, Array<Array<CubeColor>>>
        get() = _editorFaces
        set(value) {
            _editorFaces = value
            saveCurrentState()
        }
    var selectedColor by mutableStateOf(CubeColor.ORANGE)

    // Solution / Playback State
    var activeSolution by mutableStateOf<List<MoveType>?>(null)
    var activeSolutionDetails by mutableStateOf<List<AnnotatedMove>?>(null)
    var currentSolutionStep by mutableStateOf(0)
    var isPlaybackRunning by mutableStateOf(false)

    // Feedback states
    var errorMessage by mutableStateOf<String?>(null)
    var infoMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var isDetecting by mutableStateOf(false)
    var isRecalculating by mutableStateOf(false)

    // Statistics
    var totalMoveCount by mutableStateOf(0)

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
    var scannedGrids by mutableStateOf(mutableMapOf<FaceName, Array<Array<CubeColor>>>())
    var scannedRawRGBs by mutableStateOf(mutableMapOf<FaceName, Array<Array<IntVector3>>>())
    var scannedFilePaths by mutableStateOf(mutableMapOf<FaceName, String>())
    
    var gridScales by mutableStateOf(FaceName.values().associateWith { 0.55f }.toMutableMap())
    var gridOffsetsX by mutableStateOf(FaceName.values().associateWith { 0f }.toMutableMap())
    var gridOffsetsY by mutableStateOf(FaceName.values().associateWith { 0f }.toMutableMap())
    
    val manualMoves = mutableStateListOf<MoveType>()

    fun saveCurrentState() {
        val p = RubikPersistenceRegistry.persistence ?: return
        if (isSolved && (cubeState.moveHistory.isNotEmpty() || manualMoves.isNotEmpty())) {
            cubeState.moveHistory.clear()
            manualMoves.clear()
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
            p.saveCubeState(
                cubies = persistableCubies,
                moveHistory = cubeState.moveHistory.toList(),
                manualMoves = manualMoves.toList(),
                editorFaces = editorFaces
            )
        }
    }

    init {
        cubeState.onStateChanged = { saveCurrentState() }
        
        coroutineScope.launch(Dispatchers.Default) {
            val persistence = RubikPersistenceRegistry.persistence
            if (persistence != null) {
                val camera = persistence.loadCameraSettings()
                if (camera != null) {
                    withContext(Dispatchers.Main) {
                        yaw = camera.yaw
                        pitch = camera.pitch
                        cameraDistance = camera.cameraDistance
                        panX = camera.panX
                        panY = camera.panY
                        cubeState.rotationSpeedMs = camera.rotationSpeedMs
                    }
                }
                
                val saved = persistence.loadCubeState()
                if (saved != null) {
                    withContext(Dispatchers.Main) {
                        saved.cubies.forEach { snap ->
                            val cubie = cubeState.cubies.find { it.id == snap.id }
                            if (cubie != null) {
                                cubie.gridPos = com.vahitkeskin.rubiksync.cube.Vector3(snap.gridX.toFloat(), snap.gridY.toFloat(), snap.gridZ.toFloat())
                                cubie.rightBasis = com.vahitkeskin.rubiksync.cube.Vector3(snap.rightX.toFloat(), snap.rightY.toFloat(), snap.rightZ.toFloat())
                                cubie.upBasis = com.vahitkeskin.rubiksync.cube.Vector3(snap.upX.toFloat(), snap.upY.toFloat(), snap.upZ.toFloat())
                                cubie.forwardBasis = com.vahitkeskin.rubiksync.cube.Vector3(snap.forwardX.toFloat(), snap.forwardY.toFloat(), snap.forwardZ.toFloat())
                            }
                        }
                        
                        cubeState.moveHistory.clear()
                        cubeState.moveHistory.addAll(saved.moveHistory)
                        
                        manualMoves.clear()
                        manualMoves.addAll(saved.manualMoves)
                        
                        _editorFaces = saved.editorFaces
                    }
                }
            }
        }
    }
}

@Composable
fun rememberRubikAppState(
    cubeState: RubikCubeState = remember { RubikCubeState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(cubeState, coroutineScope) {
    RubikAppState(cubeState, coroutineScope)
}
