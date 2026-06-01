package com.vahitkeskin.rubiksync

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.res.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "RubikSync",
        icon = painterResource("app_icon.png")
    ) {
        App()
    }
}