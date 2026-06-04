package com.vahitkeskin.rubiksync

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val appState = rememberRubikAppState()
    val cubeState = appState.cubeState

    // Tema moduna göre karanlık mı açık mı karar ver
    val systemIsDark = isSystemInDarkTheme()
    val isDarkTheme = when (appState.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemIsDark
    }

    // Arka plan gradient renkleri
    val backgroundGradient = if (isDarkTheme) {
        listOf(DarkGradientBg1, DarkBgPrimary, DarkBgSecondary, DarkBgPrimary)
    } else {
        listOf(LightBgSecondary, LightBgPrimary, LightBgTertiary, LightBgPrimary)
    }

    // Tema yüklenene kadar boş bir gradyan ekranı göstererek Splash'in doğru temada açılmasını garanti et
    if (!appState.isThemeLoaded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = backgroundGradient))
        )
        return
    }

    // LaunchedEffect to persist camera settings when they change (with a small delay to debounce)
    LaunchedEffect(appState.yaw, appState.pitch, appState.cameraDistance, appState.panX, appState.panY, cubeState.rotationSpeedMs) {
        kotlinx.coroutines.delay(300)
        RubikPersistenceRegistry.persistence?.saveCameraSettings(
            yaw = appState.yaw,
            pitch = appState.pitch,
            cameraDistance = appState.cameraDistance,
            panX = appState.panX,
            panY = appState.panY,
            rotationSpeedMs = cubeState.rotationSpeedMs
        )
    }

    // LaunchedEffect for automatic playback of solver steps
    LaunchedEffect(appState.isPlaybackRunning, appState.currentSolutionStep, appState.activeSolution) {
        if (appState.isPlaybackRunning && appState.activeSolution != null && appState.currentSolutionStep < appState.activeSolution!!.size) {
            val nextMove = appState.activeSolution!![appState.currentSolutionStep]
            val activeDetail = appState.activeSolutionDetails?.getOrNull(appState.currentSolutionStep)
            val phase = activeDetail?.phaseName ?: "Çözüm"
            val mathDetails = getMoveMathDetails(nextMove)
            logMoveDetail(nextMove.label, phase, mathDetails)

            cubeState.executeMove(nextMove)
            appState.currentSolutionStep++
            appState.totalMoveCount++
            if (appState.currentSolutionStep >= appState.activeSolution!!.size) {
                appState.isPlaybackRunning = false
            }
        }
    }

    // Dinamik renk şeması
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
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
        lightColorScheme(
            primary = AccentOrange,
            onPrimary = Color.White,
            secondary = AccentBlueBright,
            onSecondary = Color.White,
            tertiary = AccentGreenSuccess,
            background = LightBgPrimary,
            surface = White,
            surfaceVariant = LightBgTertiary,
            onBackground = DarkThemePrimary,
            onSurface = DarkThemePrimary,
            outline = LightBorderPrimary
        )
    }

    val rubikColors = if (isDarkTheme) DarkRubikColors else LightRubikColors

    ProvideRubikColors(colors = rubikColors) {
        MaterialTheme(colorScheme = colorScheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(colors = backgroundGradient))
            ) {
                Crossfade(
                    targetState = appState.showSplashScreen,
                    animationSpec = tween(600)
                ) { showSplash ->
                    if (showSplash) {
                        SplashScreen(appState = appState)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .safeDrawingPadding()
                        ) {
                            // Subtle ambient glow behind the cube area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.65f)
                                    .align(Alignment.Center)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                RubikTheme.colors.glowOrange,
                                                RubikTheme.colors.glowBlue,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // 1. Top Dashboard (Title & Stats)
                            DashboardHeader(
                                cubeState = cubeState,
                                appState = appState
                            )

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
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            } else {
                                // 4. Control Panel (Shown at the bottom when solver is not active)
                                ControlPanel(
                                    appState = appState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
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

            // 8. Ayarlar Ekranı Overlay
            AnimatedVisibility(
                visible = appState.showSettingsScreen,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 450)
                ) + fadeIn(animationSpec = tween(durationMillis = 450)),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(durationMillis = 450)
                ) + fadeOut(animationSpec = tween(durationMillis = 450))
            ) {
                SettingsScreen(
                    appState = appState,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}
}