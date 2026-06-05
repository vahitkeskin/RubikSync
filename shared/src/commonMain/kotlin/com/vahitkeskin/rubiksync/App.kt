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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import com.vahitkeskin.rubiksync.di.appModule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val appState = rememberRubikAppState()
        val cubeState = appState.cubeState

    // Initialize sound player and bind sound callback
    DisposableEffect(Unit) {
        initCubeSound()
        cubeState.onMoveStarted = {
            if (appState.isSoundEnabled) {
                playCubeRotateSound()
            }
        }
        onDispose {
            cubeState.onMoveStarted = null
            releaseCubeSound()
        }
    }

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
        return@KoinApplication
    }

    // LaunchedEffect to persist camera settings when they change (with a small delay to debounce)
    LaunchedEffect(
        appState.yaw,
        appState.pitch,
        appState.cameraDistance,
        appState.panX,
        appState.panY,
        cubeState.rotationSpeedMs
    ) {
        kotlinx.coroutines.delay(300)
        appState.saveCameraSettings(
            yaw = appState.yaw,
            pitch = appState.pitch,
            cameraDistance = appState.cameraDistance,
            panX = appState.panX,
            panY = appState.panY,
            rotationSpeedMs = cubeState.rotationSpeedMs
        )
    }

    // LaunchedEffect for automatic playback of solver steps
    LaunchedEffect(
        appState.isPlaybackRunning,
        appState.currentSolutionStep,
        appState.activeSolution
    ) {
        if (
            appState.isCubeEditable &&
            appState.isPlaybackRunning &&
            appState.activeSolution != null &&
            appState.currentSolutionStep < appState.activeSolution!!.size
        ) {
            val nextMove = appState.activeSolution!![appState.currentSolutionStep]
            val activeDetail =
                appState.activeSolutionDetails?.getOrNull(appState.currentSolutionStep)
            val phase = activeDetail?.phaseName ?: "Çözüm"
            val mathDetails = getMoveMathDetails(nextMove)
            logMoveDetail(nextMove.label, phase, mathDetails)

            cubeState.executeMove(nextMove)
            appState.incrementSolutionStep()
            appState.incrementTotalMoveCount()
            if (appState.currentSolutionStep >= appState.activeSolution!!.size) {
                appState.updatePlaybackRunning(false)
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
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "splash",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("splash") {
                        SplashScreen(
                            appState = appState,
                            onSplashComplete = {
                                navController.navigate("dashboard") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") {
                        val overlayAlpha by animateFloatAsState(
                            targetValue = if (appState.showcaseStep != 0 && !appState.isShowcaseCompleted) 0.85f else 0f,
                            animationSpec = tween(durationMillis = 1000)
                        )

                        // Shake to scramble feature detection
                        rememberShakeDetector(
                            enabled = appState.isShakeToScrambleEnabled && appState.isCubeEditable && !cubeState.isAnimating
                        ) {
                            appState.clearManualMoves()
                            appState.coroutineScope.launch {
                                cubeState.scramble()
                            }
                        }

                        LaunchedEffect(appState.isShowcaseCompleted) {
                            if (!appState.isShowcaseCompleted && appState.showcaseStep == 0) {
                                kotlinx.coroutines.delay(1500)
                                appState.updateShowcaseStep(1)
                            }
                        }

                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
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
                                        appState = appState,
                                        onNavigateToSettings = {
                                            navController.navigate("settings")
                                        }
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
                                    onDismiss = { appState.updateShowEditorDialog(false) },
                                    onStartScanWizard = {
                                        appState.updateScannerStep(0)
                                        appState.updateScannedGrids(mutableMapOf())
                                        appState.updateScannedRawRGBs(mutableMapOf())
                                        appState.updateScannedFilePaths(mutableMapOf())
                                        appState.updateGridScales(FaceName.values().associateWith { 0.55f })
                                        appState.updateGridOffsetsX(FaceName.values().associateWith { 0f })
                                        appState.updateGridOffsetsY(FaceName.values().associateWith { 0f })
                                        appState.updateErrorMessage(null)
                                        appState.updateInfoMessage(null)
                                        appState.updateShowScannerWizard(true)
                                    }
                                )

                                // 6. Camera Scan Wizard Overlay
                                ScannerWizard(
                                    show = appState.showScannerWizard,
                                    appState = appState,
                                    onDismiss = {
                                        appState.updateShowScannerWizard(false)
                                        appState.updateScannerStep(0)
                                        appState.updateScannedGrids(mutableMapOf())
                                        appState.updateScannedRawRGBs(mutableMapOf())
                                        appState.updateScannedFilePaths(mutableMapOf())
                                    },
                                    onComplete = { completeGrids ->
                                        appState.updateEditorFaces(completeGrids)
                                        appState.updateShowScannerWizard(false)
                                        appState.coroutineScope.launch {
                                            val success = cubeState.setCustomStateAnimated(completeGrids)
                                            if (success) {
                                                appState.updateShowEditorDialog(false)
                                                appState.clearManualMoves()
                                                appState.saveCurrentState()
                                                appState.updateActiveSolution(null)
                                                appState.updateErrorMessage(null)
                                                appState.updateSuccessMessage(appState.strings.successScanComplete)
                                            } else {
                                                appState.updateErrorMessage(appState.strings.invalidCubeDesign)
                                            }
                                        }
                                    }
                                )

                                // 7. Global Feedback Overlays
                                FeedbackOverlay(appState = appState)
                            }

                            // 8. Showcase Spotlight Overlay (drawn outside safearea, aligned with root)
                            if (overlayAlpha > 0f) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Canvas(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(alpha = 0.99f)
                                            .clickable(
                                                onClick = {
                                                    appState.advanceShowcase()
                                                },
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            )
                                    ) {
                                        drawRect(
                                            color = Color(0xFF0F172A).copy(alpha = overlayAlpha)
                                        )
                                        appState.targetBounds?.let { rect ->
                                            drawRoundRect(
                                                color = Color.Transparent,
                                                topLeft = Offset(rect.left, rect.top),
                                                size = Size(rect.width, rect.height),
                                                cornerRadius = CornerRadius(appState.targetCornerRadius.toPx(), appState.targetCornerRadius.toPx()),
                                                blendMode = BlendMode.Clear
                                            )
                                        }
                                    }

                                    // Skip Showcase/Tutorial Button (styled as glassmorphism, top-left status bar aware)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .statusBarsPadding()
                                            .padding(start = 16.dp, top = 12.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.12f))
                                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                appState.updateShowcaseStep(0)
                                                appState.updateShowcaseCompleted(true)
                                                appState.updateEditorShowcaseCompleted(true)
                                                appState.updateScannerShowcaseCompleted(true)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "⏭️",
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = appState.strings.skipShowcase,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    composable(
                        route = "settings",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 450)
                            ) + fadeIn(animationSpec = tween(durationMillis = 450))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 450)
                            ) + fadeOut(animationSpec = tween(durationMillis = 450))
                        }
                    ) {
                        SettingsScreen(
                            appState = appState,
                            isDarkTheme = isDarkTheme,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
}