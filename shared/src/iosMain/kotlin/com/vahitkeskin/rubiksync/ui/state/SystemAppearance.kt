package com.vahitkeskin.rubiksync.ui.state

import androidx.compose.runtime.Composable

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    // iOS system appearance is usually handled via UIViewController preferredStatusBarStyle
    // or info.plist, so we can leave it empty for Compose iOS unless specifically needed.
}
