package com.vahitkeskin.rubiksync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.persistence.AndroidRubikPersistence
import com.vahitkeskin.rubiksync.utils.RubikPersistenceRegistry

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}