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
import androidx.compose.foundation.layout.height
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
import com.vahitkeskin.rubiksync.cube.CubeRenderer
import com.vahitkeskin.rubiksync.cube.getMoveMathDetails
import com.vahitkeskin.rubiksync.di.appModule
import com.vahitkeskin.rubiksync.ui.screens.settings.SettingsScreen
import com.vahitkeskin.rubiksync.ui.screens.splash.SplashScreen
import com.vahitkeskin.rubiksync.ui.screens.editor.EditorScreen
import com.vahitkeskin.rubiksync.ui.screens.scanner.ScannerScreen
import com.vahitkeskin.rubiksync.ui.screens.readme.ReadmeScreen
import com.vahitkeskin.rubiksync.ui.screens.DashboardScreen
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
import com.vahitkeskin.rubiksync.ui.navigation.RubikNavGraph
import com.vahitkeskin.rubiksync.ui.navigation.Screen
import com.vahitkeskin.rubiksync.ui.state.PipManager
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
                val isSolving =
                    appState.activeSolution != null && appState.currentSolutionStep < appState.activeSolution!!.size
                LaunchedEffect(isSolving) {
                    com.vahitkeskin.rubiksync.ui.state.PipManager.isSolvingActive = isSolving
                }

                val navController = rememberNavController()

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(colors = backgroundGradient))
                            .graphicsLayer {
                                alpha = if (com.vahitkeskin.rubiksync.ui.state.PipManager.isInAndroidPipMode) 0f else 1f
                            }
                    ) {

                        LaunchedEffect(appState.showEditorDialog) {
                            if (appState.showEditorDialog) {
                                navController.navigate(Screen.Editor.route)
                                appState.updateShowEditorDialog(false)
                            }
                        }

                        LaunchedEffect(appState.showScannerWizard) {
                            if (appState.showScannerWizard) {
                                navController.navigate(Screen.Scanner.route)
                                appState.updateShowScannerWizard(false)
                            }
                        }

                        RubikNavGraph(
                            navController = navController,
                            appState = appState,
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.fillMaxSize()
                        )

                        val currentBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = currentBackStackEntry?.destination?.route

                        FloatingMiniCube(
                            appState = appState,
                            currentRoute = currentRoute
                        )
                    }

                    if (PipManager.isInAndroidPipMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(colors = backgroundGradient))
                                .padding(8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val renderer = CubeRenderer(
                                    state = appState.cubeState,
                                    yaw = appState.yaw,
                                    pitch = appState.pitch,
                                    cameraDistance = appState.cameraDistance + 2f,
                                    panX = appState.panX,
                                    panY = appState.panY,
                                    isDark = isDarkTheme
                                )
                                renderer.draw(this, size.width, size.height)
                            }

                            val totalSteps = appState.activeSolution?.size ?: 1
                            val currentStep = appState.currentSolutionStep
                            val progress = currentStep.toFloat() / totalSteps.toFloat()

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$currentStep/$totalSteps",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(AccentOrange, AccentBlue)
                                            )
                                        )
                                )
                            }
                        }
                    }
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