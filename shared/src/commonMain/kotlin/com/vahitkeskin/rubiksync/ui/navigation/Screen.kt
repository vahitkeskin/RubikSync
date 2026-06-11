package com.vahitkeskin.rubiksync.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Dashboard : Screen("dashboard")
    object Settings : Screen("settings")
    object Readme : Screen("readme")
    object Editor : Screen("editor")
    object Scanner : Screen("scanner")
}
