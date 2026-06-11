package com.vahitkeskin.rubiksync

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.vahitkeskin.rubiksync.ui.components.FloatingMiniCube
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.getMoveMathDetails
import com.vahitkeskin.rubiksync.di.appModule
import com.vahitkeskin.rubiksync.ui.controlpanel.ControlPanel
import com.vahitkeskin.rubiksync.ui.dashboard.DashboardHeader
import com.vahitkeskin.rubiksync.ui.components.FeedbackOverlay
import com.vahitkeskin.rubiksync.ui.cube.InteractiveCubeCanvas
import com.vahitkeskin.rubiksync.ui.controlpanel.PlaybackController
import com.vahitkeskin.rubiksync.ui.screens.settings.SettingsScreen
import com.vahitkeskin.rubiksync.ui.screens.splash.SplashScreen
import com.vahitkeskin.rubiksync.ui.screens.editor.EditorScreen
import com.vahitkeskin.rubiksync.ui.screens.scanner.ScannerScreen
import com.vahitkeskin.rubiksync.ui.state.AccentBlue
import com.vahitkeskin.rubiksync.ui.state.AccentBlueBright
import com.vahitkeskin.rubiksync.ui.state.AccentGreen
import com.vahitkeskin.rubiksync.ui.state.AccentGreenSuccess
import com.vahitkeskin.rubiksync.ui.state.AccentOrange
import com.vahitkeskin.rubiksync.ui.state.DarkBgPrimary
import com.vahitkeskin.rubiksync.ui.state.DarkBgQuaternary
import com.vahitkeskin.rubiksync.ui.state.DarkBgSecondary
import com.vahitkeskin.rubiksync.ui.state.DarkBgTertiary
import com.vahitkeskin.rubiksync.ui.state.DarkCardBorder
import com.vahitkeskin.rubiksync.ui.state.DarkGradientBg1
import com.vahitkeskin.rubiksync.ui.state.DarkRubikColors
import com.vahitkeskin.rubiksync.ui.state.DarkThemePrimary
import com.vahitkeskin.rubiksync.ui.state.LightBgPrimary
import com.vahitkeskin.rubiksync.ui.state.LightBgSecondary
import com.vahitkeskin.rubiksync.ui.state.LightBgTertiary
import com.vahitkeskin.rubiksync.ui.state.LightBorderPrimary
import com.vahitkeskin.rubiksync.ui.state.LightRubikColors
import com.vahitkeskin.rubiksync.ui.state.ProvideRubikColors
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.state.ThemeMode
import com.vahitkeskin.rubiksync.ui.state.White
import com.vahitkeskin.rubiksync.ui.state.initializePreviewPersistence
import com.vahitkeskin.rubiksync.ui.state.rememberRubikAppState
import com.vahitkeskin.rubiksync.ui.state.Slate900
import com.vahitkeskin.rubiksync.ui.state.Slate800
import com.vahitkeskin.rubiksync.ui.state.Slate600
import com.vahitkeskin.rubiksync.ui.state.Slate100
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication

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
        val activeSolution = appState.activeSolution
        if (
            appState.isCubeEditable &&
            appState.isPlaybackRunning &&
            activeSolution != null &&
            appState.currentSolutionStep < activeSolution.size
        ) {
            val nextMove = activeSolution[appState.currentSolutionStep]
            val activeDetail =
                appState.activeSolutionDetails?.getOrNull(appState.currentSolutionStep)
            val phase = activeDetail?.phaseName ?: "Çözüm"
            val mathDetails = getMoveMathDetails(nextMove)
            logMoveDetail(nextMove.label, phase, mathDetails)

            cubeState.executeMove(nextMove)
            appState.incrementSolutionStep()
            appState.incrementTotalMoveCount()
            if (appState.currentSolutionStep >= activeSolution.size) {
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

                LaunchedEffect(appState.showEditorDialog) {
                    if (appState.showEditorDialog) {
                        navController.navigate("editor")
                        appState.updateShowEditorDialog(false)
                    }
                }

                LaunchedEffect(appState.showScannerWizard) {
                    if (appState.showScannerWizard) {
                        navController.navigate("scanner")
                        appState.updateShowScannerWizard(false)
                    }
                }

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

                        val isShowcaseActive = appState.showcaseStep != 0 && !appState.isShowcaseCompleted
                        val buttonScaleAndAlpha by animateFloatAsState(
                            targetValue = if (isShowcaseActive) 1f else 0f,
                            animationSpec = tween(
                                durationMillis = 800,
                                easing = FastOutSlowInEasing
                            )
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
                                kotlinx.coroutines.delay(1000)
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
                                            color = Slate900.copy(alpha = overlayAlpha)
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

                                    // Skip Showcase/Tutorial Button (polite, solid background, top-left status bar aware)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .statusBarsPadding()
                                            .padding(start = 16.dp, top = 12.dp)
                                            .graphicsLayer {
                                                scaleX = buttonScaleAndAlpha
                                                scaleY = buttonScaleAndAlpha
                                                alpha = buttonScaleAndAlpha
                                            }
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Slate800) // Solid Slate 800
                                            .border(1.dp, Slate600, RoundedCornerShape(20.dp)) // Solid Slate 600 border
                                            .clickable(enabled = isShowcaseActive) {
                                                appState.updateShowcaseStep(0)
                                                appState.updateShowcaseCompleted(true)
                                                appState.updateEditorShowcaseCompleted(true)
                                                appState.updateScannerShowcaseCompleted(true)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = appState.strings.skipShowcase,
                                            color = Slate100, // Slate 100
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
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
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeIn(animationSpec = tween(durationMillis = 700))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeOut(animationSpec = tween(durationMillis = 700))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeIn(animationSpec = tween(durationMillis = 700))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeOut(animationSpec = tween(durationMillis = 700))
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

                    composable(
                        route = "editor",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeIn(animationSpec = tween(durationMillis = 700))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeOut(animationSpec = tween(durationMillis = 700))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeIn(animationSpec = tween(durationMillis = 700))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeOut(animationSpec = tween(durationMillis = 700))
                        }
                    ) {
                        EditorScreen(
                            appState = appState,
                            onDismiss = { navController.popBackStack() },
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
                                navController.navigate("scanner")
                            }
                        )
                    }

                    composable(
                        route = "scanner",
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeIn(animationSpec = tween(durationMillis = 700))
                        },
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeOut(animationSpec = tween(durationMillis = 700))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeIn(animationSpec = tween(durationMillis = 700))
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(durationMillis = 700)
                            ) + fadeOut(animationSpec = tween(durationMillis = 700))
                        }
                    ) {
                        ScannerScreen(
                            appState = appState,
                            onDismiss = {
                                navController.popBackStack()
                            },
                            onComplete = { completeGrids ->
                                appState.updateEditorFaces(completeGrids)
                                navController.popBackStack("dashboard", inclusive = false)
                                appState.coroutineScope.launch {
                                    val success = cubeState.setCustomStateAnimated(completeGrids)
                                    if (success) {
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
                    }
                }

                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                FloatingMiniCube(
                    appState = appState,
                    currentRoute = currentRoute
                )
            }
        }
    }
}
}

@Preview
@Composable
fun AppCommonPreview() {
    initializePreviewPersistence()
    App()
}