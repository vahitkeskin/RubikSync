package com.vahitkeskin.rubiksync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Register Room & DataStore persistence
        com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry.persistence =
            com.vahitkeskin.rubiksync.persistence.AndroidRubikPersistence(this)

        // Set platform app context for sound playback
        com.vahitkeskin.rubiksync.setAppContext(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}