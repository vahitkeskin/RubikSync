package com.vahitkeskin.rubiksync

import android.os.Bundle
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.persistence.AndroidRubikPersistence
import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Register Room & DataStore persistence
        RubikPersistenceRegistry.persistence = AndroidRubikPersistence(this)

        // Set platform app context for sound playback
        setAppContext(this)

        setContent {
            App()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Enter Picture-in-Picture mode if a solve session is active and device supports it
        if (com.vahitkeskin.rubiksync.ui.state.PipManager.isSolvingActive) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val params = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(android.util.Rational(1, 1)) // Perfect square ratio for the cube
                    .build()
                enterPictureInPictureMode(params)
            }
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        // Update the global state manager
        com.vahitkeskin.rubiksync.ui.state.PipManager.isInAndroidPipMode = isInPictureInPictureMode
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}