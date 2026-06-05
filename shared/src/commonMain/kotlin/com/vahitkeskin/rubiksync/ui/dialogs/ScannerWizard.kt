package com.vahitkeskin.rubiksync.ui.dialogs

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
import com.vahitkeskin.rubiksync.cube.*
import com.vahitkeskin.rubiksync.solver.*
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerWizard(
    show: Boolean,
    appState: RubikAppState,
    onDismiss: () -> Unit,
    onComplete: (Map<FaceName, Array<Array<CubeColor>>>) -> Unit
) {
    if (!show) return

    val coroutineScope = appState.coroutineScope
    val currentFace = FaceName.values()[appState.scannerStep]

    val scannerScrollState = rememberScrollState()

    LaunchedEffect(appState.scannerShowcaseStep) {
        when (appState.scannerShowcaseStep) {
            4 -> scannerScrollState.animateScrollTo(scannerScrollState.maxValue)
            5 -> scannerScrollState.animateScrollTo(0)
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

    // Reactively process image when parameters change
    val currentFaceForEffect = currentFace
    val currentFilePathForEffect = appState.scannedFilePaths[currentFace]
    val currentScaleForEffect = currentScale
    val currentOffsetXForEffect = currentOffsetX
    val currentOffsetYForEffect = currentOffsetY

    LaunchedEffect(
        currentFaceForEffect,
        currentFilePathForEffect,
        currentScaleForEffect,
        currentOffsetXForEffect,
        currentOffsetYForEffect
    ) {
        if (currentFilePathForEffect != null) {
            appState.updateRecalculating(true)
            val parsedRaw = withContext(Dispatchers.Default) {
                RubikImageProcessor().processFaceImageRaw(
                    filePath = currentFilePathForEffect,
                    face = currentFaceForEffect,
                    scale = currentScaleForEffect,
                    offsetX = currentOffsetXForEffect,
                    offsetY = currentOffsetYForEffect
                )
            }
            appState.updateRecalculating(false)
            if (parsedRaw != null) {
                val updatedRaw = appState.scannedRawRGBs.toMutableMap()
                updatedRaw[currentFaceForEffect] = parsedRaw
                appState.updateScannedRawRGBs(updatedRaw)

                appState.updateScannedGrids(RubikImageProcessor().classifyAll(appState.scannedRawRGBs))
            } else {
                appState.updateErrorMessage(appState.strings.errorPhotoResolution)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RubikTheme.colors.backgroundPrimary,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentBlue, AccentGreen)
                            )
                        )
                )
            }
        },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        var scannerTargetBounds by remember { mutableStateOf<Rect?>(null) }
        var scannerTargetCornerRadius by remember { mutableStateOf(12.dp) }
        var canvasPositionInRoot by remember { mutableStateOf(Offset.Zero) }

        LaunchedEffect(show) {
            if (show && !appState.isScannerShowcaseCompleted && appState.scannerShowcaseStep == 0) {
                appState.updateScannerShowcaseStep(1)
            }
        }

        LaunchedEffect(appState.scannerShowcaseStep) {
            if (appState.scannerShowcaseStep < 0) {
                scannerTargetBounds = null
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Geri butonu
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(RubikTheme.colors.backgroundSecondary)
                                .border(
                                    0.5.dp,
                                    RubikTheme.colors.cardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = ArrowBackIcon,
                                contentDescription = "Geri",
                                tint = RubikTheme.colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appState.strings.scannerTitle,
                                color = RubikTheme.colors.textPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )

                            Text(
                                text = "${appState.scannedFilePaths.size}/6${appState.strings.facesScanned}",
                                color = RubikTheme.colors.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Connected step indicator with lines
                    Box {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coords ->
                                    if (appState.scannerShowcaseStep == 1 && !appState.isScannerShowcaseCompleted) {
                                        val pos = coords.positionInRoot()
                                        val size = coords.size
                                        scannerTargetBounds = Rect(
                                            pos.x,
                                            pos.y,
                                            pos.x + size.width,
                                            pos.y + size.height
                                        )
                                        scannerTargetCornerRadius = 16.dp
                                    }
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
                    Box {
                        CubeRotationGuide(
                            appState = appState,
                            currentFace = currentFace,
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .onGloballyPositioned { coords ->
                                    if (appState.scannerShowcaseStep == 2 && !appState.isScannerShowcaseCompleted) {
                                        val pos = coords.positionInRoot()
                                        val size = coords.size
                                        scannerTargetBounds = Rect(
                                            pos.x,
                                            pos.y,
                                            pos.x + size.width,
                                            pos.y + size.height
                                        )
                                        scannerTargetCornerRadius = 12.dp
                                    }
                                }
                        )
                        AuraBalloon(
                            text = appState.strings.showcaseScannerGuide,
                            isVisible = appState.scannerShowcaseStep == 2 && !appState.isScannerShowcaseCompleted,
                            isBelow = true,
                            onDismiss = { appState.advanceScannerShowcase() }
                        )
                    }

                    val currentPath = appState.scannedFilePaths[currentFace]
                    val isShowcaseSlidersOrPreview =
                        appState.scannerShowcaseStep == 4 || appState.scannerShowcaseStep == 5

                    if (currentPath == null && !isShowcaseSlidersOrPreview) {
                        // Not scanned yet
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
                                                if (appState.scannerShowcaseStep == 3 && !appState.isScannerShowcaseCompleted) {
                                                    val pos = coords.positionInRoot()
                                                    val size = coords.size
                                                    scannerTargetBounds = Rect(
                                                        pos.x,
                                                        pos.y,
                                                        pos.x + size.width,
                                                        pos.y + size.height
                                                    )
                                                    scannerTargetCornerRadius = 16.dp
                                                }
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
                        // Image exists — interactive alignment
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(scannerScrollState)
                        ) {
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
                                    ?: if (appState.scannerShowcaseStep != 0) {
                                        val currentFaceColor = when (currentFace) {
                                            FaceName.U -> CubeColor.ORANGE
                                            FaceName.D -> CubeColor.RED
                                            FaceName.L -> CubeColor.YELLOW
                                            FaceName.R -> CubeColor.WHITE
                                            FaceName.F -> CubeColor.GREEN
                                            FaceName.B -> CubeColor.BLUE
                                        }
                                        arrayOf(
                                            arrayOf(
                                                CubeColor.ORANGE,
                                                CubeColor.GREEN,
                                                CubeColor.YELLOW
                                            ),
                                            arrayOf(
                                                CubeColor.RED,
                                                currentFaceColor,
                                                CubeColor.WHITE
                                            ),
                                            arrayOf(
                                                CubeColor.BLUE,
                                                CubeColor.ORANGE,
                                                CubeColor.GREEN
                                            )
                                        )
                                    } else null

                                val displayRawGrid = appState.scannedRawRGBs[currentFace]
                                    ?: if (appState.scannerShowcaseStep != 0) {
                                        val currentFaceColor = when (currentFace) {
                                            FaceName.U -> CubeColor.ORANGE
                                            FaceName.D -> CubeColor.RED
                                            FaceName.L -> CubeColor.YELLOW
                                            FaceName.R -> CubeColor.WHITE
                                            FaceName.F -> CubeColor.GREEN
                                            FaceName.B -> CubeColor.BLUE
                                        }
                                        val currentFaceRGB = when (currentFaceColor) {
                                            CubeColor.ORANGE -> IntVector3(255, 130, 0)
                                            CubeColor.RED -> IntVector3(220, 20, 20)
                                            CubeColor.YELLOW -> IntVector3(240, 240, 0)
                                            CubeColor.WHITE -> IntVector3(230, 230, 230)
                                            CubeColor.GREEN -> IntVector3(0, 160, 0)
                                            CubeColor.BLUE -> IntVector3(0, 0, 200)
                                            CubeColor.INTERNAL -> IntVector3(21, 27, 38)
                                        }
                                        arrayOf(
                                            arrayOf(
                                                IntVector3(255, 130, 0),
                                                IntVector3(0, 160, 0),
                                                IntVector3(240, 240, 0)
                                            ),
                                            arrayOf(
                                                IntVector3(220, 20, 20),
                                                currentFaceRGB,
                                                IntVector3(230, 230, 230)
                                            ),
                                            arrayOf(
                                                IntVector3(0, 0, 200),
                                                IntVector3(255, 130, 0),
                                                IntVector3(0, 160, 0)
                                            )
                                        )
                                    } else null

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
                                            if (appState.scannerShowcaseStep == 5 && !appState.isScannerShowcaseCompleted) {
                                                val pos = coords.positionInRoot()
                                                val size = coords.size
                                                scannerTargetBounds = Rect(
                                                    pos.x,
                                                    pos.y,
                                                    pos.x + size.width,
                                                    pos.y + size.height
                                                )
                                                scannerTargetCornerRadius = 16.dp
                                            }
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
                                                                            defaultReferences[targetColor]!!

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
                                    AuraBalloon(
                                        text = appState.strings.showcaseScannerPreview,
                                        isVisible = appState.scannerShowcaseStep == 5 && !appState.isScannerShowcaseCompleted,
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
                                            if (appState.scannerShowcaseStep == 4 && !appState.isScannerShowcaseCompleted) {
                                                val pos = coords.positionInRoot()
                                                val size = coords.size
                                                scannerTargetBounds = Rect(
                                                    pos.x,
                                                    pos.y,
                                                    pos.x + size.width,
                                                    pos.y + size.height
                                                )
                                                scannerTargetCornerRadius = 12.dp
                                            }
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
                                AuraBalloon(
                                    text = appState.strings.showcaseScannerSliders,
                                    isVisible = appState.scannerShowcaseStep == 4 && !appState.isScannerShowcaseCompleted,
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
                                if (appState.scannerShowcaseStep == 6 && !appState.isScannerShowcaseCompleted) {
                                    val pos = coords.positionInRoot()
                                    val size = coords.size
                                    scannerTargetBounds =
                                        Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height)
                                    scannerTargetCornerRadius = 12.dp
                                }
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

            val overlayAlpha by animateFloatAsState(
                targetValue = if (appState.scannerShowcaseStep != 0 && !appState.isScannerShowcaseCompleted) 0.85f else 0f,
                animationSpec = tween(durationMillis = 1000)
            )

            if (overlayAlpha > 0f) {
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
                    drawRect(color = Color(0xFF0F172A).copy(alpha = overlayAlpha))
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
            }
        }
    }
}

@Preview
@Composable
fun ScannerWizardDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val appState = rememberPreviewRubikAppState()
        ScannerWizard(
            show = true,
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
        ScannerWizard(
            show = true,
            appState = appState,
            onDismiss = {},
            onComplete = {}
        )
    }
}