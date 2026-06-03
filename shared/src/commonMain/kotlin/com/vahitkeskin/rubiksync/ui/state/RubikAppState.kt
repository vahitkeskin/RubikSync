package com.vahitkeskin.rubiksync.ui.state

import androidx.compose.runtime.*
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.IntVector3
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import kotlinx.coroutines.CoroutineScope

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

    // Editor State
    var editorFaces by mutableStateOf(
        mapOf(
            FaceName.U to Array(3) { Array(3) { CubeColor.ORANGE } },
            FaceName.D to Array(3) { Array(3) { CubeColor.RED } },
            FaceName.L to Array(3) { Array(3) { CubeColor.YELLOW } },
            FaceName.R to Array(3) { Array(3) { CubeColor.WHITE } },
            FaceName.F to Array(3) { Array(3) { CubeColor.GREEN } },
            FaceName.B to Array(3) { Array(3) { CubeColor.BLUE } }
        )
    )
    var selectedColor by mutableStateOf(CubeColor.ORANGE)

    // Solution / Playback State
    var activeSolution by mutableStateOf<List<MoveType>?>(null)
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

    // Scanner Wizard State
    var scannerStep by mutableStateOf(0)
    var scannedGrids by mutableStateOf(mutableMapOf<FaceName, Array<Array<CubeColor>>>())
    var scannedRawRGBs by mutableStateOf(mutableMapOf<FaceName, Array<Array<IntVector3>>>())
    var scannedFilePaths by mutableStateOf(mutableMapOf<FaceName, String>())
    
    var gridScales by mutableStateOf(FaceName.values().associateWith { 0.55f }.toMutableMap())
    var gridOffsetsX by mutableStateOf(FaceName.values().associateWith { 0f }.toMutableMap())
    var gridOffsetsY by mutableStateOf(FaceName.values().associateWith { 0f }.toMutableMap())
}

@Composable
fun rememberRubikAppState(
    cubeState: RubikCubeState = remember { RubikCubeState() },
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(cubeState, coroutineScope) {
    RubikAppState(cubeState, coroutineScope)
}
