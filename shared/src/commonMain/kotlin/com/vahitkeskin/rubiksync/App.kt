package com.vahitkeskin.rubiksync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vahitkeskin.rubiksync.cube.*
import com.vahitkeskin.rubiksync.ui.components.*
import com.vahitkeskin.rubiksync.ui.dialogs.*
import com.vahitkeskin.rubiksync.ui.state.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val appState = rememberRubikAppState()
    val cubeState = appState.cubeState

    // LaunchedEffect for automatic playback of solver steps
    LaunchedEffect(appState.isPlaybackRunning, appState.currentSolutionStep, appState.activeSolution) {
        if (appState.isPlaybackRunning && appState.activeSolution != null && appState.currentSolutionStep < appState.activeSolution!!.size) {
            val nextMove = appState.activeSolution!![appState.currentSolutionStep]
            cubeState.executeMove(nextMove)
            appState.currentSolutionStep++
            if (appState.currentSolutionStep >= appState.activeSolution!!.size) {
                appState.isPlaybackRunning = false
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF8A00),
            background = Color(0xFF0F1520),
            surface = Color(0xFF1E2633)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A2639), // Dark steel blue center
                            Color(0xFF0A0D14)  // Absolute deep space outer edges
                        )
                    )
                )
                .safeContentPadding()
        ) {
            // 1. Main 3D Canvas
            InteractiveCubeCanvas(appState = appState)

            // 2. Top Dashboard (Title & History)
            DashboardHeader(cubeState = cubeState)

            // 3. Control Panel (Bottom-Centered Drawer)
            ControlPanel(
                appState = appState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            )

            // 4. Playback overlay (placed above the Control Panel)
            PlaybackController(
                appState = appState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 200.dp, start = 24.dp, end = 24.dp)
            )

            // 5. Manual Color net Editor Dialog Overlay
            EditorDialog(
                show = appState.showEditorDialog,
                appState = appState,
                onDismiss = { appState.showEditorDialog = false },
                onStartScanWizard = {
                    appState.scannerStep = 0
                    appState.scannedGrids = mutableMapOf()
                    appState.scannedRawRGBs = mutableMapOf()
                    appState.scannedFilePaths = mutableMapOf()
                    appState.gridScales = FaceName.values().associateWith { 0.55f }.toMutableMap()
                    appState.gridOffsetsX = FaceName.values().associateWith { 0f }.toMutableMap()
                    appState.gridOffsetsY = FaceName.values().associateWith { 0f }.toMutableMap()
                    appState.errorMessage = null
                    appState.infoMessage = null
                    appState.showScannerWizard = true
                }
            )

            // 6. Camera Scan Wizard Overlay
            ScannerWizard(
                show = appState.showScannerWizard,
                appState = appState,
                onDismiss = {
                    appState.showScannerWizard = false
                    appState.scannerStep = 0
                    appState.scannedGrids = mutableMapOf()
                    appState.scannedRawRGBs = mutableMapOf()
                    appState.scannedFilePaths = mutableMapOf()
                },
                onComplete = { completeGrids ->
                    appState.editorFaces = completeGrids
                    appState.showScannerWizard = false
                    appState.errorMessage = null
                    appState.infoMessage = null
                }
            )

            // 7. Global Feedback Overlays
            FeedbackOverlay(appState = appState)
        }
    }
}

// Utility to parse native/script color layouts if needed
fun parseDetectedState(jsonStr: String): Map<FaceName, Array<Array<CubeColor>>>? {
    try {
        val faces = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
        
        // Clean JSON formatting
        val cleaned = jsonStr.replace("{", "").replace("}", "").replace("\"", "").replace(" ", "")
        val entries = cleaned.split(",")
        
        val faceMap = mapOf(
            "U" to FaceName.U, "D" to FaceName.D, "L" to FaceName.L,
            "R" to FaceName.R, "F" to FaceName.F, "B" to FaceName.B
        )
        
        val colorMap = mapOf(
            "O" to CubeColor.ORANGE, "R" to CubeColor.RED, "Y" to CubeColor.YELLOW,
            "W" to CubeColor.WHITE, "G" to CubeColor.GREEN, "B" to CubeColor.BLUE
        )
        
        for (entry in entries) {
            val parts = entry.split(":")
            if (parts.size != 2) continue
            val key = parts[0]
            val value = parts[1]
            
            val face = faceMap[key] ?: continue
            val grid = Array(3) { Array(3) { CubeColor.INTERNAL } }
            
            if (value.length != 9) continue
            for (i in 0 until 9) {
                val r = i / 3
                val c = i % 3
                val char = value[i].toString()
                grid[r][c] = colorMap[char] ?: CubeColor.WHITE
            }
            faces[face] = grid
        }
        return faces
    } catch (e: Exception) {
        return null
    }
}