package com.vahitkeskin.rubiksync.ui.screens.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.PixelGrid
import com.vahitkeskin.rubiksync.cube.CubeRotationGuide
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.RubikImageProcessor
import com.vahitkeskin.rubiksync.loadImageBitmap
import com.vahitkeskin.rubiksync.loadImagePixels
import com.vahitkeskin.rubiksync.ui.components.balloon.AuraBalloon
import com.vahitkeskin.rubiksync.ui.components.RubikToolbar
import com.vahitkeskin.rubiksync.ui.screens.scanner.components.ConnectedStepIndicators
import com.vahitkeskin.rubiksync.ui.screens.scanner.components.GridAlignControls
import com.vahitkeskin.rubiksync.ui.screens.scanner.components.ScanGuidanceCard
import com.vahitkeskin.rubiksync.ui.screens.scanner.components.ScannedWorkspace
import com.vahitkeskin.rubiksync.ui.screens.scanner.components.ScannerBottomBar
import com.vahitkeskin.rubiksync.ui.screens.scanner.components.ScannerShowcaseOverlay
import com.vahitkeskin.rubiksync.ui.screens.scanner.components.UnscannedWorkspace
import com.vahitkeskin.rubiksync.ui.state.AccentBlueMedium
import com.vahitkeskin.rubiksync.ui.state.AccentGreenMaterial
import com.vahitkeskin.rubiksync.ui.state.AccentOrangeDark
import com.vahitkeskin.rubiksync.ui.state.AccentRedMaterial
import com.vahitkeskin.rubiksync.ui.state.AccentYellowMaterial
import com.vahitkeskin.rubiksync.ui.state.LightCardBg
import com.vahitkeskin.rubiksync.ui.state.PreviewRubikTheme
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.state.rememberPreviewRubikAppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.rubiksync.ui.navigation.Screen

/**
 * ScannerScreen Composable
 *
 * Implements a premium, beautiful scanning interface utilizing the camera
 * or photo gallery to capture individual faces of a Rubik's Cube.
 */
@Composable
fun ScannerScreen(
    appState: RubikAppState,
    navController: NavController
) {
    val currentFace = FaceName.entries[appState.scannerStep]
    val currentPath = appState.scannedFilePaths[currentFace]

    var isGuideExpanded by remember(currentFace) { mutableStateOf(currentPath == null) }

    LaunchedEffect(currentPath) {
        if (currentPath != null) {
            isGuideExpanded = false
        }
    }

    val scannerScrollState = rememberScrollState()

    LaunchedEffect(appState.scannerShowcaseStep) {
        when (appState.scannerShowcaseStep) {
            4 -> scannerScrollState.animateScrollTo(scannerScrollState.maxValue)
            5 -> scannerScrollState.animateScrollTo(0)
        }
    }

    // Auto-advance showcase steps that require a scanned image if no image is present
    LaunchedEffect(appState.scannerShowcaseStep, currentPath) {
        if (!appState.isScannerShowcaseCompleted) {
            val step = appState.scannerShowcaseStep
            if (currentPath == null && (step == 4 || step == 5)) {
                appState.advanceScannerShowcase()
            }
        }
    }

    val faceColorMap = mapOf(
        FaceName.U to AccentOrangeDark,
        FaceName.D to AccentRedMaterial,
        FaceName.L to AccentYellowMaterial,
        FaceName.R to LightCardBg,
        FaceName.F to AccentGreenMaterial,
        FaceName.B to AccentBlueMedium
    )

    val faceNameLocalized = mapOf(
        FaceName.U to appState.strings.faceU,
        FaceName.D to appState.strings.faceD,
        FaceName.L to appState.strings.faceL,
        FaceName.R to appState.strings.faceR,
        FaceName.F to appState.strings.faceF,
        FaceName.B to appState.strings.faceB
    )

    val centerColorLocalized = when (currentFace) {
        FaceName.U -> appState.strings.colorOrange
        FaceName.D -> appState.strings.colorRed
        FaceName.L -> appState.strings.colorYellow
        FaceName.R -> appState.strings.colorWhite
        FaceName.F -> appState.strings.colorGreen
        FaceName.B -> appState.strings.colorBlue
    }

    val faceDisplayName = faceNameLocalized[currentFace] ?: currentFace.name

    val isCurrentFaceScanned = appState.scannedFilePaths.containsKey(currentFace)
    val unscannedFaces = FaceName.entries.filter { !appState.scannedFilePaths.containsKey(it) }

    val remainingList = unscannedFaces.joinToString(", ") { face ->
        val name = faceNameLocalized[face] ?: face.name
        val color = when (face) {
            FaceName.U -> appState.strings.colorOrange
            FaceName.D -> appState.strings.colorRed
            FaceName.L -> appState.strings.colorYellow
            FaceName.R -> appState.strings.colorWhite
            FaceName.F -> appState.strings.colorGreen
            FaceName.B -> appState.strings.colorBlue
        }
        "$name ($color)"
    }

    val currentGuide = when (currentFace) {
        FaceName.U -> appState.strings.guideU
        FaceName.D -> appState.strings.guideD
        FaceName.L -> appState.strings.guideL
        FaceName.R -> appState.strings.guideR
        FaceName.F -> appState.strings.guideF
        FaceName.B -> appState.strings.guideB
    }

    val guidanceMessage = if (unscannedFaces.isEmpty()) {
        appState.strings.scanGuidanceAllScanned
    } else if (isCurrentFaceScanned) {
        appState.strings.scanGuidanceRemaining.replaceFirst("%s", remainingList)
    } else {
        val baseMsg = appState.strings.scanGuidanceFace
            .replaceFirst("%s", faceDisplayName)
            .replaceFirst("%s", centerColorLocalized)
        "$baseMsg ($currentGuide)"
    }

    val faceImageBitmap = remember(currentFace, appState.scannedFilePaths[currentFace]) {
        val path = appState.scannedFilePaths[currentFace]
        if (path != null) {
            loadImageBitmap(path)
        } else {
            null
        }
    }

    val currentScale = appState.gridScales[currentFace] ?: 0.55f
    val currentOffsetX = appState.gridOffsetsX[currentFace] ?: 0f
    val currentOffsetY = appState.gridOffsetsY[currentFace] ?: 0f

    var cachedPixelGrid by remember { mutableStateOf<PixelGrid?>(null) }
    val currentFilePath = appState.scannedFilePaths[currentFace]

    // Cache PixelGrid in background when image path or face changes to prevent disk I/O on slider drag
    LaunchedEffect(currentFace, currentFilePath) {
        if (currentFilePath != null) {
            withContext(Dispatchers.Default) {
                cachedPixelGrid = loadImagePixels(currentFilePath)
            }
        } else {
            cachedPixelGrid = null
        }
    }

    // Process image parameters in real-time without blocking main thread
    LaunchedEffect(
        currentFace,
        cachedPixelGrid,
        currentScale,
        currentOffsetX,
        currentOffsetY
    ) {
        val grid = cachedPixelGrid
        if (grid != null) {
            appState.updateRecalculating(true)
            withContext(Dispatchers.Default) {
                val parsedRaw = RubikImageProcessor().processFaceImageRaw(
                    grid = grid,
                    scale = currentScale,
                    offsetX = currentOffsetX,
                    offsetY = currentOffsetY
                )

                if (isActive) {
                    val updatedRaw = appState.scannedRawRGBs.toMutableMap()
                    updatedRaw[currentFace] = parsedRaw

                    val updatedGrids = RubikImageProcessor().classifyAll(updatedRaw)

                    if (isActive) {
                        withContext(Dispatchers.Main) {
                            appState.updateScannedRawRGBs(updatedRaw)
                            appState.updateScannedGrids(updatedGrids)
                            appState.updateRecalculating(false)
                        }
                    }
                }
            }
        }
    }

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
        var boundsStep6 by remember { mutableStateOf<Rect?>(null) }
        var viewportBounds by remember { mutableStateOf<Rect?>(null) }

        val scannerTargetBounds = remember(
            appState.scannerShowcaseStep,
            boundsStep1,
            boundsStep2,
            boundsStep3,
            boundsStep4,
            boundsStep5,
            boundsStep6
        ) {
            when (appState.scannerShowcaseStep) {
                1 -> boundsStep1
                2 -> boundsStep2
                3 -> boundsStep3
                4 -> boundsStep4
                5 -> boundsStep5
                6 -> boundsStep6
                else -> null
            }
        }

        val scannerTargetCornerRadius = when (appState.scannerShowcaseStep) {
            1 -> 16.dp
            2 -> 12.dp
            3 -> 16.dp
            4 -> 12.dp
            5 -> 16.dp
            6 -> 12.dp
            else -> 12.dp
        }

        LaunchedEffect(Unit) {
            if (!appState.isScannerShowcaseCompleted && appState.scannerShowcaseStep == 0) {
                appState.updateScannerShowcaseStep(1)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Header & Connected Step Indicators
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RubikToolbar(
                        title = appState.strings.scannerTitle,
                        subtitle = "${appState.scannedFilePaths.size}/6${appState.strings.facesScanned}",
                        onBackClick = { navController.popBackStack() },
                        titleFontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ConnectedStepIndicators(
                        appState = appState,
                        faceColorMap = faceColorMap,
                        onPositioned = { boundsStep1 = it }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    ScanGuidanceCard(
                        appState = appState,
                        guidanceMessage = guidanceMessage
                    )
                }

                // 2. Wizard Body
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                ) {
                    if (currentPath == null) {
                        UnscannedWorkspace(
                            appState = appState,
                            currentFace = currentFace,
                            isGuideExpanded = isGuideExpanded,
                            onGuideExpandedChange = { isGuideExpanded = it },
                            guidanceMessage = guidanceMessage,
                            faceNameLocalized = faceNameLocalized,
                            onGuidePositioned = { boundsStep2 = it },
                            onCapturePositioned = { boundsStep3 = it }
                        )
                    } else {
                        // Image exists — interactive alignment layout with scrollable guide card
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .onGloballyPositioned { coords ->
                                    val pos = coords.positionInRoot()
                                    val size = coords.size
                                    viewportBounds = Rect(
                                        pos.x,
                                        pos.y,
                                        pos.x + size.width,
                                        pos.y + size.height
                                    )
                                }
                                .verticalScroll(scannerScrollState)
                        ) {
                            Box {
                                CubeRotationGuide(
                                    appState = appState,
                                    currentFace = currentFace,
                                    isExpanded = isGuideExpanded,
                                    onExpandedChange = { isGuideExpanded = it },
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .onGloballyPositioned { coords ->
                                            val pos = coords.positionInRoot()
                                            val size = coords.size
                                            boundsStep2 = Rect(
                                                pos.x,
                                                pos.y,
                                                pos.x + size.width,
                                                pos.y + size.height
                                            )
                                        }
                                )
                                AuraBalloon(
                                    text = appState.strings.showcaseScannerGuide,
                                    isVisible = appState.scannerShowcaseStep == 2 && !appState.isScannerShowcaseCompleted,
                                    isBelow = true,
                                    onDismiss = { appState.advanceScannerShowcase() }
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            ScannedWorkspace(
                                appState = appState,
                                currentFace = currentFace,
                                faceImageBitmap = faceImageBitmap,
                                currentScale = currentScale,
                                currentOffsetX = currentOffsetX,
                                currentOffsetY = currentOffsetY,
                                viewportBounds = viewportBounds,
                                scannerTargetBounds = scannerTargetBounds,
                                isScrollInProgress = scannerScrollState.isScrollInProgress,
                                onPreviewPositioned = { boundsStep5 = it }
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            GridAlignControls(
                                appState = appState,
                                currentFace = currentFace,
                                currentScale = currentScale,
                                currentOffsetX = currentOffsetX,
                                currentOffsetY = currentOffsetY,
                                viewportBounds = viewportBounds,
                                scannerTargetBounds = scannerTargetBounds,
                                isScrollInProgress = scannerScrollState.isScrollInProgress,
                                onPositioned = { boundsStep4 = it }
                            )
                        }
                    }
                }

                // 3. Bottom Navigation — compact buttons with icons
                ScannerBottomBar(
                    appState = appState,
                    currentFace = currentFace,
                    onDismiss = { navController.popBackStack() },
                    onComplete = { completeGrids ->
                        appState.updateEditorFaces(completeGrids)
                        navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                        appState.coroutineScope.launch {
                            val success =
                                appState.cubeState.setCustomStateAnimated(completeGrids)
                            if (success) {
                                appState.clearManualMoves()
                                appState.saveCurrentState()
                                appState.updateActiveSolution(null)
                                appState.updateErrorMessage(null)
                                appState.updateSuccessMessage(appState.strings.successScanComplete)
                            } else {
                                appState.updateErrorMessage(appState.strings.invalidCubeDesign)
                            }
                        }
                    },
                    onPositioned = { boundsStep6 = it }
                )
            }

            ScannerShowcaseOverlay(
                appState = appState,
                scannerTargetBounds = scannerTargetBounds,
                scannerTargetCornerRadius = scannerTargetCornerRadius
            )
        }
    }
}

@Preview
@Composable
fun ScannerWizardDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val appState = rememberPreviewRubikAppState()
        ScannerScreen(
            appState = appState,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
fun ScannerWizardLightPreview() {
    PreviewRubikTheme(isDark = false) {
        val appState = rememberPreviewRubikAppState()
        ScannerScreen(
            appState = appState,
            navController = rememberNavController()
        )
    }
}