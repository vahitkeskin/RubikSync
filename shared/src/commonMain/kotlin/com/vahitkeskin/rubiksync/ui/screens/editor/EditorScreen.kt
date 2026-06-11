package com.vahitkeskin.rubiksync.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.components.RubikToolbar
import com.vahitkeskin.rubiksync.ui.state.*
import com.vahitkeskin.rubiksync.ui.screens.editor.components.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.rubiksync.ui.navigation.Screen

/**
 * EditorScreen Composable
 *
 * Implements a clean, premium editor interface that allows users to manually set
 * facelet colors on their Rubik's cube or scan using the camera wizard.
 */
@Composable
fun EditorScreen(
    appState: RubikAppState,
    navController: NavController
) {
    var activeFace by remember { mutableStateOf(FaceName.F) }
    var showJsonImportDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RubikTheme.colors.backgroundPrimary)
            .safeDrawingPadding()
    ) {
        var boundsStep1 by remember { mutableStateOf<Rect?>(null) }
        var boundsStep2 by remember { mutableStateOf<Rect?>(null) }
        var boundsStep3 by remember { mutableStateOf<Rect?>(null) }
        var boundsStep4 by remember { mutableStateOf<Rect?>(null) }
        var boundsStep5 by remember { mutableStateOf<Rect?>(null) }

        val editorTargetBounds = remember(
            appState.editorShowcaseStep,
            boundsStep1,
            boundsStep2,
            boundsStep3,
            boundsStep4,
            boundsStep5
        ) {
            when (appState.editorShowcaseStep) {
                1 -> boundsStep1
                2 -> boundsStep2
                3 -> boundsStep3
                4 -> boundsStep4
                5 -> boundsStep5
                else -> null
            }
        }

        val editorTargetCornerRadius = when (appState.editorShowcaseStep) {
            1 -> 10.dp
            2 -> 8.dp
            3 -> 12.dp
            4 -> 12.dp
            5 -> 14.dp
            else -> 12.dp
        }

        LaunchedEffect(Unit) {
            if (!appState.isEditorShowcaseCompleted && appState.editorShowcaseStep == 0) {
                appState.updateEditorShowcaseStep(1)
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header / Toolbar Row
                RubikToolbar(
                    title = appState.strings.editorTitle,
                    subtitle = appState.strings.editorSubtitle,
                    onBackClick = { navController.popBackStack() },
                    titleFontSize = 17.sp,
                    rightContent = {
                        Button(
                            onClick = { showJsonImportDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RubikTheme.colors.backgroundSecondary,
                                contentColor = RubikTheme.colors.textSecondary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("📋 JSON", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                )

                // 1. Scan Assistant Entry Point
                ScanCard(
                    appState = appState,
                    onStartScanWizard = {
                        appState.updateScannerStep(0)
                        appState.updateScannedGrids(mutableMapOf())
                        appState.updateScannedRawRGBs(mutableMapOf())
                        appState.updateScannedFilePaths(mutableMapOf())
                        appState.updateGridScales(FaceName.entries.associateWith { 0.55f })
                        appState.updateGridOffsetsX(FaceName.entries.associateWith { 0f })
                        appState.updateGridOffsetsY(FaceName.entries.associateWith { 0f })
                        appState.updateErrorMessage(null)
                        appState.updateInfoMessage(null)
                        navController.navigate(Screen.Scanner.route)
                    },
                    onPositioned = { boundsStep5 = it }
                )

                // 2. Dynamic Face Selector Tabs
                FaceSelectorTabs(
                    appState = appState,
                    activeFace = activeFace,
                    onFaceSelect = { activeFace = it },
                    onPositioned = { boundsStep1 = it }
                )

                // 3. Grid Workspace (Face Grid & Small Net Map side-by-side)
                PainterWorkspace(
                    appState = appState,
                    activeFace = activeFace,
                    onFaceSelect = { activeFace = it },
                    onPositioned = { boundsStep2 = it }
                )

                // 4. Color Palette Picker
                ColorPalette(
                    appState = appState,
                    onPositioned = { boundsStep3 = it }
                )

                // 5. Action Controls (Cancel, Clear, Apply)
                BottomActionButtons(
                    appState = appState,
                    onDismiss = { navController.popBackStack() },
                    onPositioned = { boundsStep4 = it }
                )
            }

            // JSON Import Overlay Dialog
            if (showJsonImportDialog) {
                JsonImportDialog(
                    appState = appState,
                    onDismissRequest = { showJsonImportDialog = false }
                )
            }

            // Interactive Showcase / Tutorial Overlay
            ShowcaseOverlay(
                appState = appState,
                editorTargetBounds = editorTargetBounds,
                editorTargetCornerRadius = editorTargetCornerRadius
            )
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
private fun EditorDialogDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val appState = rememberPreviewRubikAppState()
        EditorScreen(
            appState = appState,
            navController = rememberNavController()
        )
    }
}
