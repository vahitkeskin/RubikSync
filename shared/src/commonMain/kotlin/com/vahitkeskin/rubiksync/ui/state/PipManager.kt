package com.vahitkeskin.rubiksync.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

object PipManager {
    /** True if a solve session is active and not yet finished */
    var isSolvingActive by mutableStateOf(false)

    /** True if the Android MainActivity is currently in native picture-in-picture mode */
    var isInAndroidPipMode by mutableStateOf(false)
}
