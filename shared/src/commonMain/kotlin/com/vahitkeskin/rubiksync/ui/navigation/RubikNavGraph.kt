package com.vahitkeskin.rubiksync.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.vahitkeskin.rubiksync.ui.screens.DashboardScreen
import com.vahitkeskin.rubiksync.ui.screens.editor.EditorScreen
import com.vahitkeskin.rubiksync.ui.screens.readme.ReadmeScreen
import com.vahitkeskin.rubiksync.ui.screens.scanner.ScannerScreen
import com.vahitkeskin.rubiksync.ui.screens.settings.SettingsScreen
import com.vahitkeskin.rubiksync.ui.screens.splash.SplashScreen
import com.vahitkeskin.rubiksync.ui.state.RubikAppState

/**
 * RubikNavGraph extracts the entire navigation routing logic.
 * Keeping App.kt clean and focused on initialization and global overlays.
 */
@Composable
fun RubikNavGraph(
    navController: NavHostController,
    appState: RubikAppState,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(
            route = Screen.Splash.route,
            content = {
                SplashScreen(
                    appState = appState,
                    navController = navController
                )
            }
        )

        composable(
            route = Screen.Dashboard.route,
            content = {
                DashboardScreen(
                    appState = appState,
                    cubeState = appState.cubeState,
                    navController = navController
                )
            }
        )

        composable(
            route = Screen.Settings.route,
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
            },
            content = {
                SettingsScreen(
                    appState = appState,
                    isDarkTheme = isDarkTheme,
                    navController = navController
                )
            }
        )

        composable(
            route = Screen.Readme.route,
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
            },
            content = {
                ReadmeScreen(
                    appState = appState,
                    isDarkTheme = isDarkTheme,
                    navController = navController
                )
            }
        )

        composable(
            route = Screen.Editor.route,
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
            },
            content = {
                EditorScreen(
                    appState = appState,
                    navController = navController
                )
            }
        )

        composable(
            route = Screen.Scanner.route,
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
            },
            content = {
                ScannerScreen(
                    appState = appState,
                    navController = navController
                )
            }
        )
    }
}
