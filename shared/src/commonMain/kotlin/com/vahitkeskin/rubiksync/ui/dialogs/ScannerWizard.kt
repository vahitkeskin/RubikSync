package com.vahitkeskin.rubiksync.ui.dialogs

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
        FaceName.U to Color(0xFFFB8C00),
        FaceName.D to Color(0xFFE53935),
        FaceName.L to Color(0xFFFFEB3B),
        FaceName.R to Color(0xFFECEFF1),
        FaceName.F to Color(0xFF4CAF50),
        FaceName.B to Color(0xFF1E88E5)
    )

    val faceNameTurkish = mapOf(
        FaceName.U to "Üst",
        FaceName.D to "Alt",
        FaceName.L to "Sol",
        FaceName.R to "Sağ",
        FaceName.F to "Ön",
        FaceName.B to "Arka"
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
                appState.errorMessage = "Fotoğraf çözümlenemedi."
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0F1724),
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
                                listOf(Color(0xFF448AFF), Color(0xFF30D158))
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
                    text = "📷 Küp Tarama",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )

                Text(
                    text = "${appState.scannedGrids.size}/6 yüz tarandı",
                    color = Color(0xFF4A5568),
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
                        val circleBg = if (isScanned || isCurrent) baseColor.copy(alpha = 0.9f) else Color(0xFF1C2536)
                        val textColor = if (face == FaceName.R && (isScanned || isCurrent)) Color.Black else Color.White

                        if (index > 0) {
                            // Connecting line
                            val prevScanned = appState.scannedGrids.containsKey(FaceName.values()[index - 1])
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        if (prevScanned) Color(0xFF30D158).copy(alpha = 0.5f)
                                        else Color(0xFF1C2536)
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
                                    color = if (isCurrent) Color.White else Color.Transparent,
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
                                    listOf(Color(0xFF0B1F12), Color(0xFF112218))
                                )
                            )
                            .border(0.5.dp, Color(0x2230D158), RoundedCornerShape(10.dp))
                            .clickable { appState.infoMessage = null }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✅ ${appState.infoMessage ?: ""}",
                            color = Color(0xFF6DD58C),
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
                                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(14.dp))
                                .background(Color(0xFF111827)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (appState.isRecalculating) {
                                CircularProgressIndicator(
                                    color = Color(0xFF448AFF),
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
                                        text = "Görsel Yok",
                                        color = Color(0xFF4A5568),
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

                                            val faceDisplayName = faceNameTurkish[detectedFace] ?: detectedFace.name
                                            val centerColorTurkish = when (detectedFace) {
                                                FaceName.U -> "Turuncu"
                                                FaceName.D -> "Kırmızı"
                                                FaceName.L -> "Sarı"
                                                FaceName.R -> "Beyaz"
                                                FaceName.F -> "Yeşil"
                                                FaceName.B -> "Mavi"
                                            }
                                            appState.infoMessage = "$centerColorTurkish: $faceDisplayName yüz algılandı"
                                        } else {
                                            appState.errorMessage = "Yüz algılanamadı!"
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
                        // Photo Preview with Grid Overlay
                        BoxWithConstraints(
                            modifier = Modifier
                                .size(190.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0x15FFFFFF), RoundedCornerShape(16.dp))
                                .background(Color(0xFF0A0E18)),
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

                        // Alignment sliders with values
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "🔧 Izgarayı küpün kenarlarına hizalayın",
                                color = Color(0xFF4A5568),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Scale
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Boyut",
                                    color = Color(0xFF6B7A8D),
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
                                        activeTrackColor = Color(0xFF448AFF),
                                        inactiveTrackColor = Color(0xFF1C2536),
                                        thumbColor = Color.White
                                    )
                                )
                                Text(
                                    text = "${(currentScale * 100).toInt()}%",
                                    color = Color(0xFF6B7A8D),
                                    fontSize = 9.sp,
                                    modifier = Modifier.width(32.dp),
                                    textAlign = TextAlign.End,
                                    maxLines = 1
                                )
                            }

                            // Offset X
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Yatay",
                                    color = Color(0xFF6B7A8D),
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
                                        activeTrackColor = Color(0xFF448AFF),
                                        inactiveTrackColor = Color(0xFF1C2536),
                                        thumbColor = Color.White
                                    )
                                )
                                Text(
                                    text = "${(currentOffsetX * 100).toInt()}",
                                    color = Color(0xFF6B7A8D),
                                    fontSize = 9.sp,
                                    modifier = Modifier.width(32.dp),
                                    textAlign = TextAlign.End,
                                    maxLines = 1
                                )
                            }

                            // Offset Y
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Dikey",
                                    color = Color(0xFF6B7A8D),
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
                                        activeTrackColor = Color(0xFF448AFF),
                                        inactiveTrackColor = Color(0xFF1C2536),
                                        thumbColor = Color.White
                                    )
                                )
                                Text(
                                    text = "${(currentOffsetY * 100).toInt()}",
                                    color = Color(0xFF6B7A8D),
                                    fontSize = 9.sp,
                                    modifier = Modifier.width(32.dp),
                                    textAlign = TextAlign.End,
                                    maxLines = 1
                                )
                            }
                        }

                        // Extracted Cell Colors Preview
                        val grid = appState.scannedGrids[currentFace]
                        val rawGrid = appState.scannedRawRGBs[currentFace]

                        if (grid != null && rawGrid != null) {
                            Text(
                                text = "🎨 Renk Önizleme (düzeltmek için tıklayın)",
                                color = Color(0xFF6B7A8D),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (row in 0..2) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        for (col in 0..2) {
                                            val cellColor = grid[row][col]
                                            val isCenter = row == 1 && col == 1

                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(RoundedCornerShape(5.dp))
                                                    .background(Color(cellColor.rgb))
                                                    .border(
                                                        width = if (isCenter) 1.5.dp else 0.5.dp,
                                                        color = if (isCenter) Color.White else Color(0x1AFFFFFF),
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

                        // Re-capture button
                        Button(
                            onClick = {
                                val updatedPaths = appState.scannedFilePaths.toMutableMap()
                                updatedPaths.remove(currentFace)
                                appState.scannedFilePaths = updatedPaths
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF141B28),
                                contentColor = Color(0xFF8A99AD)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = "📸 Yeniden Çek",
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
                        containerColor = Color(0xFF141B28),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(
                        text = "✕ İptal",
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
                        containerColor = Color(0xFF141B28),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF0A0E18),
                        disabledContentColor = Color(0xFF4A5568)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(
                        text = "← Geri",
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
                            containerColor = if (hasCurrentScan) Color(0xFF448AFF) else Color(0xFF141B28),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF0A0E18),
                            disabledContentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1.1f).height(42.dp)
                    ) {
                        Text(
                            text = "İleri →",
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
                                appState.successMessage = "Küp başarıyla tarandı! ✅"
                            } else {
                                appState.errorMessage = "Tüm yüzeyleri tarayın."
                            }
                        },
                        enabled = appState.scannedGrids.size == 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (appState.scannedGrids.size == 6) Color(0xFFFF8A00) else Color(0xFF141B28),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF0A0E18),
                            disabledContentColor = Color(0xFF4A5568)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1.1f).height(42.dp)
                    ) {
                        Text(
                            text = "✓ Ayarla",
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
