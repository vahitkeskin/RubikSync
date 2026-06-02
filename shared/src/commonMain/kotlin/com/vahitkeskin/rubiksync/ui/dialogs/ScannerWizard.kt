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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.CameraCaptureOrPicker
import com.vahitkeskin.rubiksync.loadImageBitmap
import com.vahitkeskin.rubiksync.cube.*
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

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
        FaceName.U to Color(0xFFFB8C00), // Orange
        FaceName.D to Color(0xFFE53935), // Red
        FaceName.L to Color(0xFFFFEB3B), // Yellow
        FaceName.R to Color(0xFFECEFF1), // White
        FaceName.F to Color(0xFF4CAF50), // Green
        FaceName.B to Color(0xFF1E88E5)  // Blue
    )
    
    val faceNameTurkish = mapOf(
        FaceName.U to "ÜST (U)",
        FaceName.D to "ALT (D)",
        FaceName.L to "SOL (L)",
        FaceName.R to "SAĞ (R)",
        FaceName.F to "ÖN (F)",
        FaceName.B to "ARKA (B)"
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
                appState.errorMessage = "Fotoğraf çözümlenemedi. Lütfen küpü ortalayıp tekrar deneyin."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60A0D14))
            .clickable(enabled = false) {}
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Header & Step Indicators
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Küp Tarama Sihirbazı",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Step Indicator pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        FaceName.values().forEachIndexed { index, face ->
                            val isCurrent = (appState.scannerStep == index)
                            val isScanned = appState.scannedGrids.containsKey(face)
                            val pillColor = if (isScanned || isCurrent) faceColorMap[face] ?: Color.Gray else Color(0x22FFFFFF)
                            val textColor = if (face == FaceName.R && (isScanned || isCurrent)) Color.Black else Color.White
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(pillColor)
                                    .border(
                                        width = if (isCurrent) 2.dp else 0.dp,
                                        color = if (isCurrent) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        appState.scannerStep = index
                                        appState.errorMessage = null
                                        appState.infoMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = faceNameTurkish[face]?.substringBefore(" ") ?: face.name,
                                    color = textColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    if (appState.infoMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B5E20)) // Dark Forest Green
                                .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(10.dp))
                                .clickable { appState.infoMessage = null }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✅ ${appState.infoMessage}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 2. Wizard Body (Instructions & Capture/Preview)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f).padding(vertical = 16.dp)
                ) {
                    CubeRotationGuide(
                        currentFace = currentFace,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    val currentPath = appState.scannedFilePaths[currentFace]
                    if (currentPath == null) {
                        // Not scanned yet
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                    .background(Color(0x09FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (appState.isRecalculating) {
                                    CircularProgressIndicator(color = Color(0xFF448AFF), modifier = Modifier.size(36.dp))
                                } else {
                                    Text(
                                        text = "📸\nGörsel Yok",
                                        color = Color.Gray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
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
                                                appState.infoMessage = "$centerColorTurkish merkez: $faceDisplayName yüzeyi başarıyla algılandı!"
                                            } else {
                                                appState.errorMessage = "Yüzey algılanamadı! Lütfen küpü ortalayıp merkez karesi net görünecek şekilde tekrar deneyin."
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                )
                            }
                        }
                    } else {
                        // Image exists, show interactive alignment UI
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // 1. Photo Preview Box with Canvas Grid Overlay
                            BoxWithConstraints(
                                modifier = Modifier
                                    .size(200.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(2.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0A0D14)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (faceImageBitmap != null) {
                                    Image(
                                        bitmap = faceImageBitmap,
                                        contentDescription = "Face Photo Preview",
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
                                    
                                    // Draw grid boundaries
                                    drawRect(
                                        color = Color.Green,
                                        topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                        size = androidx.compose.ui.geometry.Size(gridW, gridH),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                    
                                    // Internal grid dividers
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
                                    
                                    // Small target ticks at the center of each cell
                                    for (r in 0..2) {
                                        for (c in 0..2) {
                                            val cx = left + (c + 0.5f) * stepW
                                            val cy = top + (r + 0.5f) * stepH
                                            
                                            // Draw a tiny dot at each cell center
                                            drawCircle(
                                                color = Color.Red,
                                                radius = 2.dp.toPx(),
                                                center = androidx.compose.ui.geometry.Offset(cx, cy)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // 2. Alignment sliders (Scale, OffsetX, OffsetY)
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Yeşil ızgarayı küpün dış sınırlarına hizalamak için sürgüleri kullanın:",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                // Scale Slider
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Boyut:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(50.dp))
                                    Slider(
                                        value = currentScale,
                                        onValueChange = { s ->
                                            val updated = appState.gridScales.toMutableMap()
                                            updated[currentFace] = s
                                            appState.gridScales = updated
                                        },
                                        valueRange = 0.3f..0.9f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                // Offset X Slider
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Yatay:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(50.dp))
                                    Slider(
                                        value = currentOffsetX,
                                        onValueChange = { x ->
                                            val updated = appState.gridOffsetsX.toMutableMap()
                                            updated[currentFace] = x
                                            appState.gridOffsetsX = updated
                                        },
                                        valueRange = -0.3f..0.3f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                // Offset Y Slider
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Dikey:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(50.dp))
                                    Slider(
                                        value = currentOffsetY,
                                        onValueChange = { y ->
                                            val updated = appState.gridOffsetsY.toMutableMap()
                                            updated[currentFace] = y
                                            appState.gridOffsetsY = updated
                                        },
                                        valueRange = -0.3f..0.3f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            
                            // 3. Extracted Cell Colors Preview
                            val grid = appState.scannedGrids[currentFace]
                            val rawGrid = appState.scannedRawRGBs[currentFace]
                            
                            if (grid != null && rawGrid != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Renk Önizleme (Düzeltmek için kareye tıklayıp renk seçin):",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Row(
                                    modifier = Modifier.padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (row in 0..2) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            for (col in 0..2) {
                                                val cellColor = grid[row][col]
                                                val isCenter = row == 1 && col == 1
                                                
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(cellColor.rgb))
                                                        .border(
                                                            width = if (isCenter) 2.dp else 1.dp,
                                                            color = if (isCenter) Color.White else Color(0x33FFFFFF),
                                                            shape = RoundedCornerShape(4.dp)
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
                                                        Text("🔒", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Button(
                                onClick = {
                                    val updatedPaths = appState.scannedFilePaths.toMutableMap()
                                    updatedPaths.remove(currentFace)
                                    appState.scannedFilePaths = updatedPaths
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x11FFFFFF), contentColor = Color.LightGray),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Başka Fotoğraf Seç / Çek", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 3. Wizard Bottom Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal")
                    }
                    
                    Button(
                        onClick = {
                            appState.errorMessage = null
                            appState.infoMessage = null
                            if (appState.scannerStep > 0) appState.scannerStep--
                        },
                        enabled = appState.scannerStep > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Geri")
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
                                containerColor = if (hasCurrentScan) Color(0xFF448AFF) else Color(0x22FFFFFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("İleri")
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
                                } else {
                                    appState.errorMessage = "Lütfen tüm yüzeyleri taradığınızdan emin olun."
                                }
                            },
                            enabled = appState.scannedGrids.size == 6,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (appState.scannedGrids.size == 6) Color(0xFFFF8A00) else Color(0x22FFFFFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text("Küpü Ayarla")
                        }
                    }
                }
            }
        }
    }
}
