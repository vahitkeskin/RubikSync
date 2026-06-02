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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Dashboard (Title & History)
                DashboardHeader(cubeState = cubeState)

                // 2. Main 3D Canvas (occupies remaining height)
                InteractiveCubeCanvas(
                    appState = appState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                // 3. Playback controller (shown directly below the 3D canvas only when solution is active)
                if (appState.activeSolution != null) {
                    PlaybackController(
                        appState = appState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                } else {
                    // 4. Control Panel (Shown at the bottom when solver is not active)
                    ControlPanel(
                        appState = appState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

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