package com.vahitkeskin.rubiksync.ui.screens

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.CameraCaptureOrPicker
import com.vahitkeskin.rubiksync.loadImageBitmap
import com.vahitkeskin.rubiksync.PixelGrid
import com.vahitkeskin.rubiksync.loadImagePixels
import com.vahitkeskin.rubiksync.cube.*
import com.vahitkeskin.rubiksync.solver.*
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.icons.CloseIcon
import com.vahitkeskin.rubiksync.ui.icons.ArrowBackIcon
import com.vahitkeskin.rubiksync.ui.icons.ArrowForwardIcon
import com.vahitkeskin.rubiksync.ui.icons.CheckIcon

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.components.RubikToolbar

@Composable
fun ScannerScreen(
    appState: RubikAppState,
    onDismiss: () -> Unit,
    onComplete: (Map<FaceName, Array<Array<CubeColor>>>) -> Unit
) {
    val coroutineScope = appState.coroutineScope
    val currentFace = FaceName.values()[appState.scannerStep]
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
    val unscannedFaces = FaceName.values().filter { !appState.scannedFilePaths.containsKey(it) }


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
        var canvasPositionInRoot by remember { mutableStateOf(Offset.Zero) }
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
                        onBackClick = onDismiss,
                        titleFontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Connected step indicator with lines
                    Box {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    val pos = coords.positionInRoot()
                                    val size = coords.size
                                    boundsStep1 = Rect(
                                        pos.x,
                                        pos.y,
                                        pos.x + size.width,
                                        pos.y + size.height
                                    )
                                }
                        ) {
                            FaceName.values().forEachIndexed { index, face ->
                                val isCurrent = (appState.scannerStep == index)
                                val isScanned = appState.scannedFilePaths.containsKey(face)

                                val baseColor = faceColorMap[face] ?: Color.Gray
                                val circleBg =
                                    if (isScanned || isCurrent) baseColor.copy(alpha = 0.9f) else RubikTheme.colors.backgroundTertiary
                                val textColor =
                                    if (face == FaceName.R && (isScanned || isCurrent)) Color.Black else (if (isScanned || isCurrent) Color.White else RubikTheme.colors.textSecondary)

                                if (index > 0) {
                                    // Connecting line
                                    val prevScanned =
                                        appState.scannedFilePaths.containsKey(FaceName.values()[index - 1])

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(
                                                if (prevScanned) RubikTheme.colors.accentGreen.copy(
                                                    alpha = 0.5f
                                                )
                                                else RubikTheme.colors.borderSubtle
                                            )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(circleBg)
                                        .border(
                                            width = if (isCurrent) 2.dp else 0.dp,
                                            color = if (isCurrent) RubikTheme.colors.textPrimary else Color.Transparent,
                                            shape = RoundedCornerShape(15.dp)
                                        )
                                        .clickable {
                                            appState.updateScannerStep(index)
                                            appState.updateErrorMessage(null)
                                            appState.updateInfoMessage(null)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = face.name,
                                        color = textColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        AuraBalloon(
                            text = appState.strings.showcaseScannerIndicator,
                            isVisible = appState.scannerShowcaseStep == 1 && !appState.isScannerShowcaseCompleted,
                            isBelow = true,
                            onDismiss = { appState.advanceScannerShowcase() }
                        )
                    }

                    // Info / Guidance message
                    val displayMessage = appState.infoMessage ?: guidanceMessage
                    val isSuccess = appState.infoMessage != null

                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (isSuccess) {
                                        if (RubikTheme.colors.isDark) {
                                            listOf(AccentGreenShadow, AccentGreenVeryDark)
                                        } else {
                                            listOf(AccentGreenFaintBg, AccentGreenSoftBg)
                                        }
                                    } else {
                                        if (RubikTheme.colors.isDark) {
                                            listOf(DarkBgSecondary, DarkBgTertiary)
                                        } else {
                                            listOf(LightBgPrimary, LightBgTertiary)
                                        }
                                    }
                                )
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (isSuccess) {
                                    if (RubikTheme.colors.isDark) AccentGreenAlpha13 else AccentGreenAlpha20
                                } else {
                                    RubikTheme.colors.borderSubtle
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = isSuccess) { appState.updateInfoMessage(null) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSuccess) "✅ $displayMessage" else "📸 $displayMessage",
                            color = if (isSuccess) {
                                if (RubikTheme.colors.isDark) AccentGreenBright else AccentGreenDark
                            } else {
                                RubikTheme.colors.textPrimary
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis

                        )
                    }
                }

                // 2. Wizard Body
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp)
                ) {
                    if (currentPath == null) {
                        // Not scanned yet - display guide card statically at the top
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

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            if (appState.isRecalculating) {
                                CircularProgressIndicator(
                                    color = RubikTheme.colors.accentBlue,
                                    modifier = Modifier.size(36.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CameraCaptureOrPicker(
                                        faceName = currentFace.name,
                                        takePhotoLabel = appState.strings.takePhotoLabel,
                                        chooseGalleryLabel = appState.strings.chooseGalleryLabel,
                                        selectImageLabel = appState.strings.selectImageLabel,
                                        guidanceText = guidanceMessage,
                                        onImageSelected = { filePath ->
                                            coroutineScope.launch {
                                                appState.updateRecalculating(true)
                                                appState.updateErrorMessage(null)
                                                appState.updateInfoMessage(null)
                                                val detectedFace =
                                                    withContext(Dispatchers.Default) {
                                                        RubikImageProcessor().detectFaceFromImage(
                                                            filePath
                                                        )
                                                    }
                                                appState.updateRecalculating(false)
                                                if (detectedFace != null) {
                                                    val updatedPaths =
                                                        appState.scannedFilePaths.toMutableMap()
                                                    updatedPaths[detectedFace] = filePath
                                                    appState.updateScannedFilePaths(updatedPaths)

                                                    val updatedScales =
                                                        appState.gridScales.toMutableMap()
                                                    updatedScales[detectedFace] = 0.55f
                                                    appState.updateGridScales(updatedScales)

                                                    val updatedOffsetsX =
                                                        appState.gridOffsetsX.toMutableMap()
                                                    updatedOffsetsX[detectedFace] = 0f
                                                    appState.updateGridOffsetsX(updatedOffsetsX)

                                                    val updatedOffsetsY =
                                                        appState.gridOffsetsY.toMutableMap()
                                                    updatedOffsetsY[detectedFace] = 0f
                                                    appState.updateGridOffsetsY(updatedOffsetsY)

                                                    appState.updateScannerStep(detectedFace.ordinal)

                                                    val faceDisplayName =
                                                        faceNameLocalized[detectedFace]
                                                            ?: detectedFace.name
                                                    val centerColorLocalized = when (detectedFace) {
                                                        FaceName.U -> appState.strings.colorOrange
                                                        FaceName.D -> appState.strings.colorRed
                                                        FaceName.L -> appState.strings.colorYellow
                                                        FaceName.R -> appState.strings.colorWhite
                                                        FaceName.F -> appState.strings.colorGreen
                                                        FaceName.B -> appState.strings.colorBlue
                                                    }
                                                    appState.updateInfoMessage(
                                                        appState.strings.faceDetectedMessage
                                                            .replaceFirst(
                                                                "%s",
                                                                centerColorLocalized
                                                            )
                                                            .replaceFirst("%s", faceDisplayName)
                                                    )
                                                } else {
                                                    appState.updateErrorMessage(appState.strings.faceNotDetected)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(0.75f)
                                            .onGloballyPositioned { coords ->
                                                val pos = coords.positionInRoot()
                                                val size = coords.size
                                                boundsStep3 = Rect(
                                                    pos.x,
                                                    pos.y,
                                                    pos.x + size.width,
                                                    pos.y + size.height
                                                )
                                            }
                                    )
                                    AuraBalloon(
                                        text = appState.strings.showcaseScannerCapture,
                                        isVisible = appState.scannerShowcaseStep == 3 && !appState.isScannerShowcaseCompleted,
                                        isBelow = true,
                                        onDismiss = { appState.advanceScannerShowcase() }
                                    )
                                }
                            }
                        }
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

                            // 1. Side-by-Side Photo Preview and Color Grid
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Box: Photo Preview with Grid Overlay
                                BoxWithConstraints(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            RubikTheme.colors.borderSubtle,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .background(RubikTheme.colors.backgroundPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (faceImageBitmap != null) {
                                        Image(
                                            bitmap = faceImageBitmap,
                                            contentDescription = "Yüz Fotoğrafı",
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    // Canvas Grid overlay
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val w = size.width
                                        val h = size.height

                                        val gridW = w * currentScale
                                        val gridH = h * currentScale

                                        val left = (w - gridW) / 2f + currentOffsetX * w
                                        val top = (h - gridH) / 2f + currentOffsetY * h

                                        val stepW = gridW / 3f
                                        val stepH = gridH / 3f

                                        drawRect(
                                            color = Color.Green,
                                            topLeft = androidx.compose.ui.geometry.Offset(
                                                left,
                                                top
                                            ),
                                            size = androidx.compose.ui.geometry.Size(gridW, gridH),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = 2.dp.toPx()
                                            )
                                        )

                                        for (i in 1..2) {
                                            drawLine(
                                                color = Color.Green,
                                                start = androidx.compose.ui.geometry.Offset(
                                                    left + i * stepW,
                                                    top
                                                ),
                                                end = androidx.compose.ui.geometry.Offset(
                                                    left + i * stepW,
                                                    top + gridH
                                                ),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                            drawLine(
                                                color = Color.Green,
                                                start = androidx.compose.ui.geometry.Offset(
                                                    left,
                                                    top + i * stepH
                                                ),
                                                end = androidx.compose.ui.geometry.Offset(
                                                    left + gridW,
                                                    top + i * stepH
                                                ),
                                                strokeWidth = 1.dp.toPx()
                                            )
                                        }

                                        for (r in 0..2) {
                                            for (c in 0..2) {
                                                val cx = left + (c + 0.5f) * stepW
                                                val cy = top + (r + 0.5f) * stepH
                                                drawCircle(
                                                    color = Color.Red,
                                                    radius = 2.dp.toPx(),
                                                    center = androidx.compose.ui.geometry.Offset(
                                                        cx,
                                                        cy
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                // Right Box: Color Preview Grid
                                val displayGrid = appState.scannedGrids[currentFace]
                                val displayRawGrid = appState.scannedRawRGBs[currentFace]

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            1.dp,
                                            RubikTheme.colors.borderSubtle,
                                            RoundedCornerShape(16.dp)
                                        )
                                        .background(RubikTheme.colors.backgroundPrimary)
                                        .onGloballyPositioned { coords ->
                                            val pos = coords.positionInRoot()
                                            val size = coords.size
                                            boundsStep5 = Rect(
                                                pos.x,
                                                pos.y,
                                                pos.x + size.width,
                                                pos.y + size.height
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (displayGrid != null && displayRawGrid != null) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.fillMaxSize().padding(10.dp)
                                        ) {
                                            Text(
                                                text = appState.strings.colorPreview,
                                                color = RubikTheme.colors.textSecondary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))

                                            Column(
                                                modifier = Modifier.padding(2.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                for (row in 0..2) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            4.dp
                                                        )
                                                    ) {
                                                        for (col in 0..2) {
                                                            val cellColor = displayGrid[row][col]
                                                            val isCenter = row == 1 && col == 1

                                                            Box(
                                                                modifier = Modifier
                                                                    .size(28.dp)
                                                                    .clip(RoundedCornerShape(5.dp))
                                                                    .background(Color(cellColor.rgb))
                                                                    .border(
                                                                        width = if (isCenter) 1.5.dp else 0.5.dp,
                                                                        color = if (isCenter) RubikTheme.colors.textPrimary else RubikTheme.colors.borderSubtle,
                                                                        shape = RoundedCornerShape(5.dp)
                                                                    )
                                                                    .clickable {
                                                                        val actualGrid =
                                                                            appState.scannedGrids[currentFace]
                                                                        val actualRawGrid =
                                                                            appState.scannedRawRGBs[currentFace]

                                                                        val colorsList = listOf(
                                                                            CubeColor.ORANGE,
                                                                            CubeColor.RED,
                                                                            CubeColor.YELLOW,
                                                                            CubeColor.WHITE,
                                                                            CubeColor.GREEN,
                                                                            CubeColor.BLUE
                                                                        )
                                                                        val currentIndex =
                                                                            colorsList.indexOf(
                                                                                cellColor
                                                                            )
                                                                        val nextIndex =
                                                                            (currentIndex + 1) % colorsList.size
                                                                        val targetColor =
                                                                            colorsList[nextIndex]

                                                                        val defaultReferences =
                                                                            mapOf(
                                                                                CubeColor.ORANGE to IntVector3(
                                                                                    255,
                                                                                    130,
                                                                                    0
                                                                                ),
                                                                                CubeColor.RED to IntVector3(
                                                                                    220,
                                                                                    20,
                                                                                    20
                                                                                ),
                                                                                CubeColor.YELLOW to IntVector3(
                                                                                    240,
                                                                                    240,
                                                                                    0
                                                                                ),
                                                                                CubeColor.WHITE to IntVector3(
                                                                                    230,
                                                                                    230,
                                                                                    230
                                                                                ),
                                                                                CubeColor.GREEN to IntVector3(
                                                                                    0,
                                                                                    160,
                                                                                    0
                                                                                ),
                                                                                CubeColor.BLUE to IntVector3(
                                                                                    0,
                                                                                    0,
                                                                                    200
                                                                                )
                                                                            )
                                                                        val refRGB =
                                                                            defaultReferences[targetColor] ?: IntVector3(0, 0, 0)

                                                                        if (!isCenter) {
                                                                            val updatedRawGrid =
                                                                                (actualRawGrid
                                                                                    ?: displayRawGrid).map { it.copyOf() }
                                                                                    .toTypedArray()
                                                                            updatedRawGrid[row][col] =
                                                                                refRGB

                                                                            val updatedRaw =
                                                                                appState.scannedRawRGBs.toMutableMap()
                                                                            updatedRaw[currentFace] =
                                                                                updatedRawGrid
                                                                            appState.updateScannedRawRGBs(
                                                                                updatedRaw
                                                                            )
                                                                        }

                                                                        val updatedGrid =
                                                                            (actualGrid
                                                                                ?: displayGrid).map { it.copyOf() }
                                                                                .toTypedArray()
                                                                        updatedGrid[row][col] =
                                                                            targetColor
                                                                        val updatedGrids =
                                                                            appState.scannedGrids.toMutableMap()
                                                                        updatedGrids[currentFace] =
                                                                            updatedGrid
                                                                        appState.updateScannedGrids(
                                                                            updatedGrids
                                                                        )
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                if (isCenter) {
                                                                    Text("🔒", fontSize = 9.sp)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                            CircularProgressIndicator(
                                            color = RubikTheme.colors.accentBlue,
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    val viewport = viewportBounds
                                    val isPreviewVisible = appState.scannerShowcaseStep == 5 &&
                                            !appState.isScannerShowcaseCompleted &&
                                            viewport != null &&
                                            scannerTargetBounds != null &&
                                            scannerTargetBounds.bottom >= viewport.top + 10f &&
                                            scannerTargetBounds.top <= viewport.bottom - 10f &&
                                            !scannerScrollState.isScrollInProgress

                                    AuraBalloon(
                                        text = appState.strings.showcaseScannerPreview,
                                        isVisible = isPreviewVisible,
                                        isBelow = false,
                                        onDismiss = { appState.advanceScannerShowcase() }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Alignment sliders with values
                            Box {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp)
                                        .onGloballyPositioned { coords ->
                                            val pos = coords.positionInRoot()
                                            val size = coords.size
                                            boundsStep4 = Rect(
                                                pos.x,
                                                pos.y,
                                                pos.x + size.width,
                                                pos.y + size.height
                                            )
                                        },
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = appState.strings.alignGridDesc,
                                        color = RubikTheme.colors.textSecondary,
                                        fontSize = 9.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Scale
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = appState.strings.sizeLabel,
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(40.dp),
                                            maxLines = 1
                                        )
                                        Slider(
                                            value = currentScale,
                                            onValueChange = { s ->
                                                val updated = appState.gridScales.toMutableMap()
                                                updated[currentFace] = s
                                                appState.updateGridScales(updated)
                                            },
                                            valueRange = 0.3f..0.9f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = RubikTheme.colors.accentBlue,
                                                inactiveTrackColor = RubikTheme.colors.speedTrack,
                                                thumbColor = if (RubikTheme.colors.isDark) Color.White else RubikTheme.colors.accentBlue
                                            )
                                        )
                                        Text(
                                            text = "${(currentScale * 100).toInt()}%",
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 9.sp,
                                            modifier = Modifier.width(32.dp),
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }

                                    // Offset X
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = appState.strings.horizontalLabel,
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(40.dp),
                                            maxLines = 1
                                        )
                                        Slider(
                                            value = currentOffsetX,
                                            onValueChange = { x ->
                                                val updated = appState.gridOffsetsX.toMutableMap()
                                                updated[currentFace] = x
                                                appState.updateGridOffsetsX(updated)
                                            },
                                            valueRange = -0.3f..0.3f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = RubikTheme.colors.accentBlue,
                                                inactiveTrackColor = RubikTheme.colors.speedTrack,
                                                thumbColor = if (RubikTheme.colors.isDark) Color.White else RubikTheme.colors.accentBlue
                                            )
                                        )
                                        Text(
                                            text = "${(currentOffsetX * 100).toInt()}",
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 9.sp,
                                            modifier = Modifier.width(32.dp),
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }

                                    // Offset Y
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = appState.strings.verticalLabel,
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(40.dp),
                                            maxLines = 1
                                        )
                                        Slider(
                                            value = currentOffsetY,
                                            onValueChange = { y ->
                                                val updated = appState.gridOffsetsY.toMutableMap()
                                                updated[currentFace] = y
                                                appState.updateGridOffsetsY(updated)
                                            },
                                            valueRange = -0.3f..0.3f,
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = RubikTheme.colors.accentBlue,
                                                inactiveTrackColor = RubikTheme.colors.speedTrack,
                                                thumbColor = if (RubikTheme.colors.isDark) Color.White else RubikTheme.colors.accentBlue
                                            )
                                        )
                                        Text(
                                            text = "${(currentOffsetY * 100).toInt()}",
                                            color = RubikTheme.colors.textSecondary,
                                            fontSize = 9.sp,
                                            modifier = Modifier.width(32.dp),
                                            textAlign = TextAlign.End,
                                            maxLines = 1
                                        )
                                    }
                                }
                                val viewport = viewportBounds
                                val isSlidersVisible = appState.scannerShowcaseStep == 4 &&
                                        !appState.isScannerShowcaseCompleted &&
                                        viewport != null &&
                                        scannerTargetBounds != null &&
                                        scannerTargetBounds.bottom >= viewport.top + 10f &&
                                        scannerTargetBounds.top <= viewport.bottom - 10f &&
                                        !scannerScrollState.isScrollInProgress

                                AuraBalloon(
                                    text = appState.strings.showcaseScannerSliders,
                                    isVisible = isSlidersVisible,
                                    isBelow = false,
                                    onDismiss = { appState.advanceScannerShowcase() }
                                )
                            }

                            // Re-capture button
                            Button(
                                onClick = {
                                    val updatedPaths = appState.scannedFilePaths.toMutableMap()
                                    updatedPaths.remove(currentFace)
                                    appState.updateScannedFilePaths(updatedPaths)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RubikTheme.colors.backgroundSecondary,
                                    contentColor = RubikTheme.colors.textSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                                border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(
                                    text = appState.strings.retakePhoto,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 3. Bottom Navigation — compact buttons with icons
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                boundsStep6 =
                                    Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                            },
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RubikTheme.colors.backgroundSecondary,
                                contentColor = RubikTheme.colors.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = CloseIcon,
                                    contentDescription = "Cancel",
                                    modifier = Modifier.size(14.dp),
                                    tint = RubikTheme.colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = appState.strings.cancelButton,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = {
                                appState.updateErrorMessage(null)
                                appState.updateInfoMessage(null)
                                if (appState.scannerStep > 0) appState.updateScannerStep(appState.scannerStep - 1)
                            },
                            enabled = appState.scannerStep > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RubikTheme.colors.backgroundSecondary,
                                contentColor = RubikTheme.colors.textPrimary,
                                disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                                disabledContentColor = RubikTheme.colors.buttonDisabledText
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = ArrowBackIcon,
                                    contentDescription = "Back",
                                    modifier = Modifier.size(14.dp),
                                    tint = if (appState.scannerStep > 0) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = appState.strings.backButton,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        val hasCurrentScan = appState.scannedFilePaths.containsKey(currentFace)
                        if (appState.scannerStep < 5) {
                            Button(
                                onClick = {
                                    appState.updateErrorMessage(null)
                                    appState.updateInfoMessage(null)
                                    var foundNext = false
                                    for (i in 1..5) {
                                        val nextIdx = (appState.scannerStep + i) % 6
                                        val nextFace = FaceName.values()[nextIdx]
                                        if (!appState.scannedFilePaths.containsKey(nextFace)) {
                                            appState.updateScannerStep(nextIdx)
                                            foundNext = true
                                            break
                                        }
                                    }
                                    if (!foundNext) {
                                        appState.updateScannerStep(
                                            (appState.scannerStep + 1).coerceAtMost(
                                                5
                                            )
                                        )
                                    }
                                },
                                enabled = hasCurrentScan,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasCurrentScan) RubikTheme.colors.accentBlue else RubikTheme.colors.backgroundSecondary,
                                    contentColor = if (hasCurrentScan) Color.White else RubikTheme.colors.textSecondary,
                                    disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                                    disabledContentColor = RubikTheme.colors.buttonDisabledText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                border = if (hasCurrentScan) null else BorderStroke(
                                    1.dp,
                                    RubikTheme.colors.buttonBorder
                                ),
                                modifier = Modifier.weight(1.1f).height(42.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = appState.strings.nextButton,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = ArrowForwardIcon,
                                        contentDescription = "Next",
                                        modifier = Modifier.size(14.dp),
                                        tint = if (hasCurrentScan) Color.White else RubikTheme.colors.textSecondary
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val completeGrids =
                                        mutableMapOf<FaceName, Array<Array<CubeColor>>>()
                                    var isValid = true
                                    for (face in FaceName.values()) {
                                        val gridVal = appState.scannedGrids[face]
                                        val hasPath = appState.scannedFilePaths.containsKey(face)
                                        if (gridVal != null && hasPath) {
                                            completeGrids[face] = gridVal
                                        } else {
                                            isValid = false
                                        }
                                    }
                                    if (isValid) {
                                        onComplete(completeGrids)
                                        appState.updateSuccessMessage(appState.strings.successScanComplete)
                                    } else {
                                        appState.updateErrorMessage(appState.strings.errorScanAllFaces)
                                    }
                                },
                                enabled = appState.scannedFilePaths.size == 6,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (appState.scannedFilePaths.size == 6) RubikTheme.colors.accentOrange else RubikTheme.colors.backgroundSecondary,
                                    contentColor = if (appState.scannedFilePaths.size == 6) Color.White else RubikTheme.colors.textSecondary,
                                    disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                                    disabledContentColor = RubikTheme.colors.buttonDisabledText
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                border = if (appState.scannedFilePaths.size == 6) null else BorderStroke(
                                    1.dp,
                                    RubikTheme.colors.buttonBorder
                                ),
                                modifier = Modifier.weight(1.1f).height(42.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = CheckIcon,
                                        contentDescription = "Set",
                                        modifier = Modifier.size(14.dp),
                                        tint = if (appState.scannedFilePaths.size == 6) Color.White else RubikTheme.colors.textSecondary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = appState.strings.setButton,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    AuraBalloon(
                        text = appState.strings.showcaseScannerAction,
                        isVisible = appState.scannerShowcaseStep == 6 && !appState.isScannerShowcaseCompleted,
                        isBelow = false,
                        onDismiss = { appState.advanceScannerShowcase() }
                    )
                }
            }

            val isShowcaseActive = appState.scannerShowcaseStep != 0 && !appState.isScannerShowcaseCompleted
            val overlayAlpha by animateFloatAsState(
                targetValue = if (isShowcaseActive) 0.85f else 0f,
                animationSpec = tween(durationMillis = 1000)
            )

            val buttonScaleAndAlpha by animateFloatAsState(
                targetValue = if (isShowcaseActive) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 800,
                    easing = FastOutSlowInEasing
                )
            )

            if (overlayAlpha > 0f) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coords ->
                                canvasPositionInRoot = coords.positionInRoot()
                            }
                            .graphicsLayer(alpha = 0.99f)
                            .clickable(
                                onClick = { appState.advanceScannerShowcase() },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {
                        drawRect(color = Slate900.copy(alpha = overlayAlpha))
                        scannerTargetBounds?.let { rect ->
                            val localLeft = rect.left - canvasPositionInRoot.x
                            val localTop = rect.top - canvasPositionInRoot.y
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = Offset(localLeft, localTop),
                                size = Size(rect.width, rect.height),
                                cornerRadius = CornerRadius(
                                    scannerTargetCornerRadius.toPx(),
                                    scannerTargetCornerRadius.toPx()
                                ),
                                blendMode = BlendMode.Clear
                            )
                        }
                    }

                    // Skip Showcase/Tutorial Button (styled as a premium, slate button aligned to top-right corner)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(end = 16.dp, top = 12.dp)
                            .graphicsLayer {
                                scaleX = buttonScaleAndAlpha
                                scaleY = buttonScaleAndAlpha
                                alpha = buttonScaleAndAlpha
                            }
                            .clip(RoundedCornerShape(20.dp))
                            .background(Slate800) // Solid Slate 800
                            .border(1.dp, Slate600, RoundedCornerShape(20.dp)) // Solid Slate 600 border
                            .clickable(enabled = isShowcaseActive) {
                                appState.updateScannerShowcaseStep(0)
                                appState.updateScannerShowcaseCompleted(true)
                            }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = appState.strings.skipShowcase,
                            color = Slate100, // Slate 100
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
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
            onDismiss = {},
            onComplete = {}
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
            onDismiss = {},
            onComplete = {}
        )
    }
}