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

import com.vahitkeskin.rubiksync.ui.state.RubikTheme

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

    LaunchedEffect(currentFaceForEffect, currentFilePathForEffect, currentScaleForEffect, currentOffsetXForEffect, currentOffsetYForEffect) {
        if (currentFilePathForEffect != null) {
            appState.isRecalculating = true
            val parsedRaw = withContext(Dispatchers.Default) {
                RubikImageProcessor().processFaceImageRaw(
                    filePath = currentFilePathForEffect,
                    face = currentFaceForEffect,
                    scale = currentScaleForEffect,
                    offsetX = currentOffsetXForEffect,
                    offsetY = currentOffsetYForEffect
                )
            }
            appState.isRecalculating = false
            if (parsedRaw != null) {
                val updatedRaw = appState.scannedRawRGBs.toMutableMap()
                updatedRaw[currentFaceForEffect] = parsedRaw
                appState.scannedRawRGBs = updatedRaw

                appState.scannedGrids = RubikImageProcessor().classifyAll(appState.scannedRawRGBs).toMutableMap()
            } else {
                appState.errorMessage = appState.strings.errorPhotoResolution
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header & Connected Step Indicators
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = appState.strings.scannerTitle,
                    color = RubikTheme.colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = "${appState.scannedGrids.size}/6${appState.strings.facesScanned}",
                    color = RubikTheme.colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Connected step indicator with lines
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FaceName.values().forEachIndexed { index, face ->
                        val isCurrent = (appState.scannerStep == index)
                        val isScanned = appState.scannedGrids.containsKey(face)

                        val baseColor = faceColorMap[face] ?: Color.Gray
                        val circleBg = if (isScanned || isCurrent) baseColor.copy(alpha = 0.9f) else RubikTheme.colors.backgroundTertiary
                        val textColor = if (face == FaceName.R && (isScanned || isCurrent)) Color.Black else (if (isScanned || isCurrent) Color.White else RubikTheme.colors.textSecondary)

                        if (index > 0) {
                            // Connecting line
                            val prevScanned = appState.scannedGrids.containsKey(FaceName.values()[index - 1])
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        if (prevScanned) RubikTheme.colors.accentGreen.copy(alpha = 0.5f)
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
                                    appState.scannerStep = index
                                    appState.errorMessage = null
                                    appState.infoMessage = null
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

                // Info message
                if (appState.infoMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (RubikTheme.colors.isDark) {
                                        listOf(AccentGreenShadow, AccentGreenVeryDark)
                                    } else {
                                        listOf(AccentGreenFaintBg, AccentGreenSoftBg)
                                    }
                                )
                            )
                            .border(0.5.dp, if (RubikTheme.colors.isDark) AccentGreenAlpha13 else AccentGreenAlpha20, RoundedCornerShape(10.dp))
                            .clickable { appState.infoMessage = null }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ ${appState.infoMessage ?: ""}",
                            color = if (RubikTheme.colors.isDark) AccentGreenBright else AccentGreenDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 2. Wizard Body
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f).padding(vertical = 8.dp)
            ) {
                CubeRotationGuide(
                    appState = appState,
                    currentFace = currentFace,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                val currentPath = appState.scannedFilePaths[currentFace]
                if (currentPath == null) {
                    // Not scanned yet
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                                .background(RubikTheme.colors.backgroundSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (appState.isRecalculating) {
                                CircularProgressIndicator(
                                    color = RubikTheme.colors.accentBlue,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "📸",
                                        fontSize = 24.sp
                                    )
                                    Text(
                                        text = appState.strings.noImage,
                                        color = RubikTheme.colors.textSecondary,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (!appState.isRecalculating) {
                            CameraCaptureOrPicker(
                                faceName = currentFace.name,
                                onImageSelected = { filePath ->
                                    coroutineScope.launch {
                                        appState.isRecalculating = true
                                        appState.errorMessage = null
                                        appState.infoMessage = null
                                        val detectedFace = withContext(Dispatchers.Default) {
                                            RubikImageProcessor().detectFaceFromImage(filePath)
                                        }
                                        appState.isRecalculating = false
                                        if (detectedFace != null) {
                                            val updatedPaths = appState.scannedFilePaths.toMutableMap()
                                            updatedPaths[detectedFace] = filePath
                                            appState.scannedFilePaths = updatedPaths

                                            val updatedScales = appState.gridScales.toMutableMap()
                                            updatedScales[detectedFace] = 0.55f
                                            appState.gridScales = updatedScales

                                            val updatedOffsetsX = appState.gridOffsetsX.toMutableMap()
                                            updatedOffsetsX[detectedFace] = 0f
                                            appState.gridOffsetsX = updatedOffsetsX

                                            val updatedOffsetsY = appState.gridOffsetsY.toMutableMap()
                                            updatedOffsetsY[detectedFace] = 0f
                                            appState.gridOffsetsY = updatedOffsetsY

                                            appState.scannerStep = detectedFace.ordinal

                                            val faceDisplayName = faceNameLocalized[detectedFace] ?: detectedFace.name
                                            val centerColorLocalized = when (detectedFace) {
                                                FaceName.U -> appState.strings.colorOrange
                                                FaceName.D -> appState.strings.colorRed
                                                FaceName.L -> appState.strings.colorYellow
                                                FaceName.R -> appState.strings.colorWhite
                                                FaceName.F -> appState.strings.colorGreen
                                                FaceName.B -> appState.strings.colorBlue
                                            }
                                            appState.infoMessage = appState.strings.faceDetectedMessage
                                                .replaceFirst("%s", centerColorLocalized)
                                                .replaceFirst("%s", faceDisplayName)
                                        } else {
                                            appState.errorMessage = appState.strings.faceNotDetected
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.75f)
                            )
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
                            .verticalScroll(rememberScrollState())
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
                                    .border(1.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
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
                                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                        size = androidx.compose.ui.geometry.Size(gridW, gridH),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )

                                    for (i in 1..2) {
                                        drawLine(
                                            color = Color.Green,
                                            start = androidx.compose.ui.geometry.Offset(left + i * stepW, top),
                                            end = androidx.compose.ui.geometry.Offset(left + i * stepW, top + gridH),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                        drawLine(
                                            color = Color.Green,
                                            start = androidx.compose.ui.geometry.Offset(left, top + i * stepH),
                                            end = androidx.compose.ui.geometry.Offset(left + gridW, top + i * stepH),
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
                                                center = androidx.compose.ui.geometry.Offset(cx, cy)
                                            )
                                        }
                                    }
                                }
                            }

                            // Right Box: Color Preview Grid
                            val grid = appState.scannedGrids[currentFace]
                            val rawGrid = appState.scannedRawRGBs[currentFace]

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                                    .background(RubikTheme.colors.backgroundPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (grid != null && rawGrid != null) {
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
                                                 Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                     for (col in 0..2) {
                                                         val cellColor = grid[row][col]
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
                                                                     val colorsList = listOf(
                                                                         CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
                                                                         CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
                                                                     )
                                                                     val currentIndex = colorsList.indexOf(cellColor)
                                                                     val nextIndex = (currentIndex + 1) % colorsList.size
                                                                     val targetColor = colorsList[nextIndex]

                                                                     val defaultReferences = mapOf(
                                                                         CubeColor.ORANGE to IntVector3(255, 130, 0),
                                                                         CubeColor.RED to IntVector3(220, 20, 20),
                                                                         CubeColor.YELLOW to IntVector3(240, 240, 0),
                                                                         CubeColor.WHITE to IntVector3(230, 230, 230),
                                                                         CubeColor.GREEN to IntVector3(0, 160, 0),
                                                                         CubeColor.BLUE to IntVector3(0, 0, 200)
                                                                     )
                                                                     val refRGB = defaultReferences[targetColor]!!

                                                                     if (!isCenter) {
                                                                         val updatedRawGrid = rawGrid.map { it.copyOf() }.toTypedArray()
                                                                         updatedRawGrid[row][col] = refRGB

                                                                         val updatedRaw = appState.scannedRawRGBs.toMutableMap()
                                                                         updatedRaw[currentFace] = updatedRawGrid
                                                                         appState.scannedRawRGBs = updatedRaw
                                                                     }

                                                                     val updatedGrid = grid.map { it.copyOf() }.toTypedArray()
                                                                     updatedGrid[row][col] = targetColor
                                                                     val updatedGrids = appState.scannedGrids.toMutableMap()
                                                                     updatedGrids[currentFace] = updatedGrid
                                                                     appState.scannedGrids = updatedGrids
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
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Alignment sliders with values
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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
                                        appState.gridScales = updated
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
                                        appState.gridOffsetsX = updated
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
                                        appState.gridOffsetsY = updated
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

                        // Re-capture button
                        Button(
                            onClick = {
                                val updatedPaths = appState.scannedFilePaths.toMutableMap()
                                updatedPaths.remove(currentFace)
                                appState.scannedFilePaths = updatedPaths
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
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = appState.strings.cancelButton,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        appState.errorMessage = null
                        appState.infoMessage = null
                        if (appState.scannerStep > 0) appState.scannerStep--
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
                    Text(
                        text = appState.strings.backButton,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                val hasCurrentScan = appState.scannedGrids.containsKey(currentFace)
                if (appState.scannerStep < 5) {
                    Button(
                        onClick = {
                            appState.errorMessage = null
                            appState.infoMessage = null
                            var foundNext = false
                            for (i in 1..5) {
                                val nextIdx = (appState.scannerStep + i) % 6
                                val nextFace = FaceName.values()[nextIdx]
                                if (!appState.scannedGrids.containsKey(nextFace)) {
                                    appState.scannerStep = nextIdx
                                    foundNext = true
                                    break
                                }
                            }
                            if (!foundNext) {
                                appState.scannerStep = (appState.scannerStep + 1).coerceAtMost(5)
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
                        border = if (hasCurrentScan) null else BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                        modifier = Modifier.weight(1.1f).height(42.dp)
                    ) {
                        Text(
                            text = appState.strings.nextButton,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            val completeGrids = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
                            var isValid = true
                            for (face in FaceName.values()) {
                                val gridVal = appState.scannedGrids[face]
                                if (gridVal != null) {
                                    completeGrids[face] = gridVal
                                } else {
                                    isValid = false
                                }
                            }
                            if (isValid) {
                                onComplete(completeGrids)
                                appState.successMessage = appState.strings.successScanComplete
                            } else {
                                appState.errorMessage = appState.strings.errorScanAllFaces
                            }
                        },
                        enabled = appState.scannedGrids.size == 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (appState.scannedGrids.size == 6) RubikTheme.colors.accentOrange else RubikTheme.colors.backgroundSecondary,
                            contentColor = if (appState.scannedGrids.size == 6) Color.White else RubikTheme.colors.textSecondary,
                            disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                            disabledContentColor = RubikTheme.colors.buttonDisabledText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        border = if (appState.scannedGrids.size == 6) null else BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                        modifier = Modifier.weight(1.1f).height(42.dp)
                    ) {
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
    }
}
