package com.vahitkeskin.rubiksync

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val cubeState = remember { RubikCubeState() }

    // Editor & Solver State
    var showEditorDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(CubeColor.ORANGE) }
    var editorFaces by remember {
        mutableStateOf(
            mapOf(
                FaceName.U to Array(3) { Array(3) { CubeColor.ORANGE } },
                FaceName.D to Array(3) { Array(3) { CubeColor.RED } },
                FaceName.L to Array(3) { Array(3) { CubeColor.YELLOW } },
                FaceName.R to Array(3) { Array(3) { CubeColor.WHITE } },
                FaceName.F to Array(3) { Array(3) { CubeColor.GREEN } },
                FaceName.B to Array(3) { Array(3) { CubeColor.BLUE } }
            )
        )
    }
    var activeSolution by remember { mutableStateOf<List<MoveType>?>(null) }
    var currentSolutionStep by remember { mutableStateOf(0) }
    var isPlaybackRunning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDetecting by remember { mutableStateOf(false) }
    var isRecalculating by remember { mutableStateOf(false) }
    
    // Scanner Wizard State
    var showScannerWizard by remember { mutableStateOf(false) }
    var scannerStep by remember { mutableStateOf(0) }
    var scannedGrids by remember {
        mutableStateOf(
            mutableMapOf<FaceName, Array<Array<CubeColor>>>()
        )
    }
    var scannedRawRGBs by remember {
        mutableStateOf(
            mutableMapOf<FaceName, Array<Array<IntVector3>>>()
        )
    }
    var scannedFilePaths by remember {
        mutableStateOf(
            mutableMapOf<FaceName, String>()
        )
    }
    var gridScales by remember {
        mutableStateOf(
            FaceName.values().associateWith { 0.55f }.toMutableMap()
        )
    }
    var gridOffsetsX by remember {
        mutableStateOf(
            FaceName.values().associateWith { 0f }.toMutableMap()
        )
    }
    var gridOffsetsY by remember {
        mutableStateOf(
            FaceName.values().associateWith { 0f }.toMutableMap()
        )
    }

    // LaunchedEffect for automatic playback of solver steps
    LaunchedEffect(isPlaybackRunning, currentSolutionStep, activeSolution) {
        if (isPlaybackRunning && activeSolution != null && currentSolutionStep < activeSolution!!.size) {
            val nextMove = activeSolution!![currentSolutionStep]
            cubeState.executeMove(nextMove)
            currentSolutionStep++
            if (currentSolutionStep >= activeSolution!!.size) {
                isPlaybackRunning = false
            }
        }
    }

    // LaunchedEffect to process face images reactively when paths or alignment sliders change
    val currentFaceForEffect = if (showScannerWizard && scannerStep in 0..5) FaceName.values()[scannerStep] else null
    val currentFilePathForEffect = currentFaceForEffect?.let { scannedFilePaths[it] }
    val currentScaleForEffect = currentFaceForEffect?.let { gridScales[it] } ?: 0.55f
    val currentOffsetXForEffect = currentFaceForEffect?.let { gridOffsetsX[it] } ?: 0f
    val currentOffsetYForEffect = currentFaceForEffect?.let { gridOffsetsY[it] } ?: 0f

    LaunchedEffect(currentFaceForEffect, currentFilePathForEffect, currentScaleForEffect, currentOffsetXForEffect, currentOffsetYForEffect) {
        if (currentFaceForEffect != null && currentFilePathForEffect != null) {
            isRecalculating = true
            val parsedRaw = withContext(Dispatchers.Default) {
                RubikImageProcessor().processFaceImageRaw(
                    filePath = currentFilePathForEffect,
                    face = currentFaceForEffect,
                    scale = currentScaleForEffect,
                    offsetX = currentOffsetXForEffect,
                    offsetY = currentOffsetYForEffect
                )
            }
            isRecalculating = false
            if (parsedRaw != null) {
                val updatedRaw = scannedRawRGBs.toMutableMap()
                updatedRaw[currentFaceForEffect] = parsedRaw
                scannedRawRGBs = updatedRaw
                
                // Recalibrate dynamic colors based on centers
                scannedGrids = RubikImageProcessor().classifyAll(scannedRawRGBs).toMutableMap()
            } else {
                errorMessage = "Fotoğraf çözümlenemedi. Lütfen küpü ortalayıp tekrar deneyin."
            }
        }
    }

    // Camera State
    var yaw by remember { mutableStateOf(-0.55f) }       // Initial yaw - viewing top-left-front
    var pitch by remember { mutableStateOf(0.40f) }      // Initial pitch
    var cameraDistance by remember { mutableStateOf(10.0f) }
    var panX by remember { mutableStateOf(0f) }
    var panY by remember { mutableStateOf(0f) }

    // Gesture Handler
    val gestureHandler = remember(cubeState) {
        GestureHandler(
            state = cubeState,
            onCameraOrbit = { dy, dp ->
                yaw = (yaw + dy) % (2f * PI.toFloat())
                pitch = (pitch + dp).coerceIn(-1.4f, 1.4f)
            },
            onCameraZoom = { dz ->
                cameraDistance = (cameraDistance + dz).coerceIn(4f, 12f)
            },
            onCameraPan = { dx, dy ->
                panX += dx
                panY += dy
            },
            onLayerRotate = { move ->
                coroutineScope.launch {
                    cubeState.executeMove(move)
                }
            }
        )
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF8A00),
            background = Color(0xFF0F1520),
            surface = Color(0xFF1E2633)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A2639), // Dark steel blue center
                            Color(0xFF0A0D14)  // Absolute deep space outer edges
                        )
                    )
                )
                .safeContentPadding()
        ) {
            // Main 3D Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val changes = event.changes
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()

                                if (event.type == PointerEventType.Scroll) {
                                    val scrollDelta = event.changes.firstOrNull()?.scrollDelta
                                    if (scrollDelta != null) {
                                        cameraDistance = (cameraDistance + scrollDelta.y * 0.15f).coerceIn(4f, 12f)
                                        event.changes.forEach { it.consume() }
                                    }
                                    continue
                                }

                                if (changes.isEmpty()) continue

                                if (changes.size == 1) {
                                    val change = changes.first()
                                    if (change.pressed) {
                                        if (change.changedToDown()) {
                                            gestureHandler.handleTouchDown(
                                                change.position,
                                                width,
                                                height,
                                                yaw,
                                                pitch,
                                                cameraDistance,
                                                panX,
                                                panY
                                            )
                                            change.consume()
                                        } else if (change.previousPressed) {
                                            val dragAmount = change.position - change.previousPosition
                                            gestureHandler.handleTouchDrag(
                                                change.position,
                                                dragAmount,
                                                width,
                                                height,
                                                yaw,
                                                pitch,
                                                cameraDistance,
                                                panX,
                                                panY
                                            )
                                            change.consume()
                                        }
                                    } else if (change.changedToUp()) {
                                        gestureHandler.handleTouchUp()
                                        change.consume()
                                    }
                                } else if (changes.size >= 2) {
                                    // 2-Finger Zoom and Pan
                                    val p1 = changes[0]
                                    val p2 = changes[1]
                                    if (p1.pressed && p2.pressed) {
                                        val pos1 = p1.position
                                        val pos2 = p2.position
                                        val prev1 = p1.previousPosition
                                        val prev2 = p2.previousPosition

                                        val currentDist = (pos1 - pos2).getDistance()
                                        val prevDist = (prev1 - prev2).getDistance()

                                        // Zoom
                                        val zoomDelta = (prevDist - currentDist) * 0.015f
                                        cameraDistance = (cameraDistance + zoomDelta).coerceIn(4f, 12f)

                                        // Pan (average movement of two fingers)
                                        val panDelta = ((pos1 - prev1) + (pos2 - prev2)) * 0.5f
                                        // Adjust pan sensitivity relative to zoom distance
                                        panX += panDelta.x * 0.005f * cameraDistance
                                        panY += -panDelta.y * 0.005f * cameraDistance

                                        p1.consume()
                                        p2.consume()
                                    }
                                }
                            }
                        }
                    }
            ) {
                val renderer = CubeRenderer(
                    state = cubeState,
                    yaw = yaw,
                    pitch = pitch,
                    cameraDistance = cameraDistance,
                    panX = panX,
                    panY = panY
                )
                renderer.draw(this, size.width, size.height)
            }

            // Top Dashboard (Title & History)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "RUBIK SYNC",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3D Interactive Simulation",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )

                // Move History Card
                if (cubeState.moveHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33000000))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Geçmiş: " + cubeState.moveHistory.takeLast(8).joinToString(" ") { it.label },
                            color = Color(0xFFFFBD59),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Dashboard (Controls Card)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glassmorphic control panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0x1A1E2633))
                        .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        cubeState.scramble()
                                    }
                                },
                                enabled = !cubeState.isAnimating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF8A00),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Karıştır", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        cubeState.undo()
                                    }
                                },
                                enabled = !cubeState.isAnimating && cubeState.moveHistory.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x33FFFFFF),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Geri Al", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    cubeState.reset()
                                    yaw = -0.55f
                                    pitch = 0.40f
                                    cameraDistance = 6.5f
                                    panX = 0f
                                    panY = 0f
                                },
                                enabled = !cubeState.isAnimating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x22FF3B30),
                                    contentColor = Color(0xFFFF3B30)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Sıfırla", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Tasarla & Çözüm Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    showEditorDialog = true
                                },
                                enabled = !cubeState.isAnimating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x33448AFF),
                                    contentColor = Color(0xFF448AFF)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Tasarla", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val solver = RubikSolver()
                                    val solution = solver.solve(cubeState)
                                    if (solution != null && solution.isNotEmpty()) {
                                        activeSolution = solution
                                        currentSolutionStep = 0
                                        isPlaybackRunning = false
                                        errorMessage = null
                                    } else if (solution != null && solution.isEmpty()) {
                                        errorMessage = "Küp zaten çözülmüş durumda!"
                                    } else {
                                        errorMessage = "Çözüm bulunamadı! Küp yapısı hatalı olabilir."
                                    }
                                },
                                enabled = !cubeState.isAnimating,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x334CAF50),
                                    contentColor = Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp)
                            ) {
                                Text("Çözücü", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Rotation Speed Control Slider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hız:",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                modifier = Modifier.width(42.dp)
                            )

                            Slider(
                                value = 400f - cubeState.rotationSpeedMs, // Inverse value: higher ms is slower speed
                                onValueChange = { speed ->
                                    cubeState.rotationSpeedMs = 400f - speed
                                },
                                valueRange = 100f..350f, // Map to speedMs: 50ms (fast) to 300ms (slow)
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color(0xFFFF8A00),
                                    inactiveTrackColor = Color(0x22FFFFFF),
                                    thumbColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${cubeState.rotationSpeedMs.toInt()} ms",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(54.dp)
                            )
                        }
                    }
                }
            }

            // Error Message Banner
            errorMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(0.90f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xEEFF3B30))
                        .border(1.dp, Color(0xFFFF3B30), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { errorMessage = null }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✕",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Solution Playback Controller
            activeSolution?.let { solution ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 200.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(0.90f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xE61E2633))
                        .border(1.5.dp, Color(0x334CAF50), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Çözüm Adımları (${solution.size} Hamle)",
                                color = Color(0xFF4CAF50),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Close button
                            IconButton(
                                onClick = {
                                    activeSolution = null
                                    isPlaybackRunning = false
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "✕",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val scrollState = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(scrollState)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            solution.forEachIndexed { index, move ->
                                val isCurrent = index == currentSolutionStep
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isCurrent) Color(0xFF4CAF50) else Color(0x11FFFFFF))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = move.label,
                                        color = if (isCurrent) Color.White else Color.LightGray,
                                        fontSize = 14.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    cubeState.setCustomState(editorFaces)
                                    currentSolutionStep = 0
                                    isPlaybackRunning = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Başa Dön")
                            }

                            Button(
                                onClick = { isPlaybackRunning = !isPlaybackRunning },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPlaybackRunning) Color(0xFFFF3B30) else Color(0xFF4CAF50),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (isPlaybackRunning) "Duraklat" else "Oynat")
                            }

                            Button(
                                onClick = {
                                    if (currentSolutionStep < solution.size) {
                                        coroutineScope.launch {
                                            cubeState.executeMove(solution[currentSolutionStep])
                                            currentSolutionStep++
                                        }
                                    }
                                },
                                enabled = currentSolutionStep < solution.size && !cubeState.isAnimating,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("İleri >")
                            }
                        }
                    }
                }
            }

            // Unfolded 2D Cube Net Design Editor Overlay
            if (showEditorDialog) {
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
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Küp Tasarımcısı",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Küpünüzün yüzey renklerini boyayın veya fotoğraflardan algılayın",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            // Fotoğraf Tarama Sihirbazı Başlatma Kartı
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                                    .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "📸 Fotoğraf ile Hızlı Tarama",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Küpünüzün 6 yüzünü sırayla fotoğraflayıp saniyeler içinde renkleri otomatik algılatın.",
                                            color = Color.LightGray,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                    
                                    Button(
                                        onClick = {
                                            scannerStep = 0
                                            scannedGrids = mutableMapOf()
                                            scannedRawRGBs = mutableMapOf()
                                            scannedFilePaths = mutableMapOf()
                                            gridScales = FaceName.values().associateWith { 0.55f }.toMutableMap()
                                            gridOffsetsX = FaceName.values().associateWith { 0f }.toMutableMap()
                                            gridOffsetsY = FaceName.values().associateWith { 0f }.toMutableMap()
                                            showScannerWizard = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF448AFF), contentColor = Color.White),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("Taramayı Başlat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    FaceGrid(FaceName.U, editorFaces) { face, row, col ->
                                        val updated = editorFaces.mapValues { (name, grid) ->
                                            if (name == face) {
                                                val newGrid = grid.map { it.copyOf() }.toTypedArray()
                                                newGrid[row][col] = selectedColor
                                                newGrid
                                            } else {
                                                grid
                                            }
                                        }
                                        editorFaces = updated
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FaceGrid(FaceName.L, editorFaces) { face, row, col ->
                                        val updated = editorFaces.mapValues { (name, grid) ->
                                            if (name == face) {
                                                val newGrid = grid.map { it.copyOf() }.toTypedArray()
                                                newGrid[row][col] = selectedColor
                                                newGrid
                                            } else {
                                                grid
                                            }
                                        }
                                        editorFaces = updated
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FaceGrid(FaceName.F, editorFaces) { face, row, col ->
                                        val updated = editorFaces.mapValues { (name, grid) ->
                                            if (name == face) {
                                                val newGrid = grid.map { it.copyOf() }.toTypedArray()
                                                newGrid[row][col] = selectedColor
                                                newGrid
                                            } else {
                                                grid
                                            }
                                        }
                                        editorFaces = updated
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FaceGrid(FaceName.R, editorFaces) { face, row, col ->
                                        val updated = editorFaces.mapValues { (name, grid) ->
                                            if (name == face) {
                                                val newGrid = grid.map { it.copyOf() }.toTypedArray()
                                                newGrid[row][col] = selectedColor
                                                newGrid
                                            } else {
                                                grid
                                            }
                                        }
                                        editorFaces = updated
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FaceGrid(FaceName.B, editorFaces) { face, row, col ->
                                        val updated = editorFaces.mapValues { (name, grid) ->
                                            if (name == face) {
                                                val newGrid = grid.map { it.copyOf() }.toTypedArray()
                                                newGrid[row][col] = selectedColor
                                                newGrid
                                            } else {
                                                grid
                                            }
                                        }
                                        editorFaces = updated
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    FaceGrid(FaceName.D, editorFaces) { face, row, col ->
                                        val updated = editorFaces.mapValues { (name, grid) ->
                                            if (name == face) {
                                                val newGrid = grid.map { it.copyOf() }.toTypedArray()
                                                newGrid[row][col] = selectedColor
                                                newGrid
                                            } else {
                                                grid
                                            }
                                        }
                                        editorFaces = updated
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Renk Paleti", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val paletteColors = listOf(
                                        CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
                                        CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
                                    )
                                    paletteColors.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(color.rgb))
                                                .border(
                                                    width = if (selectedColor == color) 3.dp else 1.dp,
                                                    color = if (selectedColor == color) Color.White else Color(0x33FFFFFF),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { selectedColor = color }
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { showEditorDialog = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("İptal")
                                }

                                Button(
                                    onClick = {
                                        editorFaces = mapOf(
                                            FaceName.U to Array(3) { Array(3) { CubeColor.ORANGE } },
                                            FaceName.D to Array(3) { Array(3) { CubeColor.RED } },
                                            FaceName.L to Array(3) { Array(3) { CubeColor.YELLOW } },
                                            FaceName.R to Array(3) { Array(3) { CubeColor.WHITE } },
                                            FaceName.F to Array(3) { Array(3) { CubeColor.GREEN } },
                                            FaceName.B to Array(3) { Array(3) { CubeColor.BLUE } }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FF3B30), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Temizle")
                                }

                                Button(
                                    onClick = {
                                        val success = cubeState.setCustomState(editorFaces)
                                        if (success) {
                                            showEditorDialog = false
                                            activeSolution = null
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Geçersiz küp tasarımı! Lütfen tüm parçaları doğru renklendirdiğinizden emin olun."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("Uygula")
                                }
                            }
                        }
                    }
                }
            }

            if (showScannerWizard) {
                val currentFace = FaceName.values()[scannerStep]
                
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
                
                val faceScanInstructions = mapOf(
                    FaceName.U to "Küpün Üst (U) yüzünü fotoğraflayın. Merkez kare Turuncu olmalıdır. Küpü düz tutun ve ışık yansıması olmadığından emin olun.",
                    FaceName.D to "Küpün Alt (D) yüzünü fotoğraflayın. Merkez kare Kırmızı olmalıdır. Küpü düz tutun ve ışık yansıması olmadığından emin olun.",
                    FaceName.L to "Küpün Sol (L) yüzünü fotoğraflayın. Merkez kare Sarı olmalıdır. Küpü düz tutun ve ışık yansıması olmadığından emin olun.",
                    FaceName.R to "Küpün Sağ (R) yüzünü fotoğraflayın. Merkez kare Beyaz olmalıdır. Küpü düz tutun ve ışık yansıması olmadığından emin olun.",
                    FaceName.F to "Küpün Ön (F) yüzünü fotoğraflayın. Merkez kare Yeşil olmalıdır. Küpü düz tutun ve ışık yansıması olmadığından emin olun.",
                    FaceName.B to "Küpün Arka (B) yüzünü fotoğraflayın. Merkez kare Mavi olmalıdır. Küpü düz tutun ve ışık yansıması olmadığından emin olun."
                )

                val faceImageBitmap = remember(currentFace, scannedFilePaths[currentFace]) {
                    val path = scannedFilePaths[currentFace]
                    if (path != null) {
                        loadImageBitmap(path)
                    } else {
                        null
                    }
                }

                val currentScale = gridScales[currentFace] ?: 0.55f
                val currentOffsetX = gridOffsetsX[currentFace] ?: 0f
                val currentOffsetY = gridOffsetsY[currentFace] ?: 0f

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
                                        val isCurrent = (scannerStep == index)
                                        val isScanned = scannedGrids.containsKey(face)
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
                                                ),
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
                            }

                            // 2. Wizard Body (Instructions & Capture/Preview)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.weight(1f).padding(vertical = 16.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Adım ${scannerStep + 1}: ${faceNameTurkish[currentFace]} Yüzeyi",
                                            color = faceColorMap[currentFace] ?: Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = faceScanInstructions[currentFace] ?: "",
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                                
                                val currentPath = scannedFilePaths[currentFace]
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
                                            if (isRecalculating) {
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
                                        
                                        if (!isRecalculating) {
                                            CameraCaptureOrPicker(
                                                faceName = currentFace.name,
                                                onImageSelected = { filePath ->
                                                    val updatedPaths = scannedFilePaths.toMutableMap()
                                                    updatedPaths[currentFace] = filePath
                                                    scannedFilePaths = updatedPaths
                                                    
                                                    val updatedScales = gridScales.toMutableMap()
                                                    updatedScales[currentFace] = 0.55f
                                                    gridScales = updatedScales
                                                    
                                                    val updatedOffsetsX = gridOffsetsX.toMutableMap()
                                                    updatedOffsetsX[currentFace] = 0f
                                                    gridOffsetsX = updatedOffsetsX
                                                    
                                                    val updatedOffsetsY = gridOffsetsY.toMutableMap()
                                                    updatedOffsetsY[currentFace] = 0f
                                                    gridOffsetsY = updatedOffsetsY
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
                                                
                                                // Draw outer bounding box in dashed yellow
                                                drawRect(
                                                    color = Color(0xFFF1C40F),
                                                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                                                    size = androidx.compose.ui.geometry.Size(gridW, gridH),
                                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                        width = 2.dp.toPx(),
                                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                    )
                                                )
                                                
                                                val cellW = gridW / 3f
                                                val cellH = gridH / 3f
                                                
                                                // Vertical lines
                                                for (i in 1..2) {
                                                    drawLine(
                                                        color = Color(0xFF2ECC71),
                                                        start = androidx.compose.ui.geometry.Offset(left + i * cellW, top),
                                                        end = androidx.compose.ui.geometry.Offset(left + i * cellW, top + gridH),
                                                        strokeWidth = 2.dp.toPx()
                                                    )
                                                }
                                                
                                                // Horizontal lines
                                                for (i in 1..2) {
                                                    drawLine(
                                                        color = Color(0xFF2ECC71),
                                                        start = androidx.compose.ui.geometry.Offset(left, top + i * cellH),
                                                        end = androidx.compose.ui.geometry.Offset(left + gridW, top + i * cellH),
                                                        strokeWidth = 2.dp.toPx()
                                                    )
                                                }
                                                
                                                val patchW = cellW * 0.15f
                                                val patchH = cellH * 0.15f
                                                for (r in 0..2) {
                                                    for (c in 0..2) {
                                                        val cx = left + (c + 0.5f) * cellW
                                                        val cy = top + (r + 0.5f) * cellH
                                                        
                                                        // Draw patch border
                                                        drawRect(
                                                            color = Color(0x802ECC71),
                                                            topLeft = androidx.compose.ui.geometry.Offset(cx - patchW, cy - patchH),
                                                            size = androidx.compose.ui.geometry.Size(patchW * 2, patchH * 2),
                                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
                                                        )
                                                        
                                                        // Center dot
                                                        drawCircle(
                                                            color = Color(0xFF2ECC71),
                                                            radius = 2.dp.toPx(),
                                                            center = androidx.compose.ui.geometry.Offset(cx, cy)
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            // Recalculating badge
                                            if (isRecalculating) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(8.dp)
                                                        .background(Color(0xCC0A0D14), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                                ) {
                                                    Text("Hesaplanıyor...", color = Color(0xFF2ECC71), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        
                                        // 2. Alignment Sliders
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Izgara Boyutu:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                                                Slider(
                                                    value = currentScale,
                                                    onValueChange = { 
                                                        val valMut = gridScales.toMutableMap()
                                                        valMut[currentFace] = it
                                                        gridScales = valMut 
                                                    },
                                                    valueRange = 0.3f..0.85f,
                                                    colors = SliderDefaults.colors(
                                                        activeTrackColor = Color(0xFFFF8A00),
                                                        inactiveTrackColor = Color(0x22FFFFFF),
                                                        thumbColor = Color.White
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Yatay Konum:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                                                Slider(
                                                    value = currentOffsetX,
                                                    onValueChange = { 
                                                        val valMut = gridOffsetsX.toMutableMap()
                                                        valMut[currentFace] = it
                                                        gridOffsetsX = valMut 
                                                    },
                                                    valueRange = -0.25f..0.25f,
                                                    colors = SliderDefaults.colors(
                                                        activeTrackColor = Color(0xFF448AFF),
                                                        inactiveTrackColor = Color(0x22FFFFFF),
                                                        thumbColor = Color.White
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Dikey Konum:", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.width(90.dp))
                                                Slider(
                                                    value = currentOffsetY,
                                                    onValueChange = { 
                                                        val valMut = gridOffsetsY.toMutableMap()
                                                        valMut[currentFace] = it
                                                        gridOffsetsY = valMut 
                                                    },
                                                    valueRange = -0.25f..0.25f,
                                                    colors = SliderDefaults.colors(
                                                        activeTrackColor = Color(0xFF448AFF),
                                                        inactiveTrackColor = Color(0x22FFFFFF),
                                                        thumbColor = Color.White
                                                    ),
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                        
                                        // 3. Classified 3x3 Grid
                                        val grid = scannedGrids[currentFace]
                                        if (grid != null) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Algılanan Renkler (Düzeltmek için Dokunun)",
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    for (row in 0..2) {
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            for (col in 0..2) {
                                                                val color = grid[row][col]
                                                                val isCenter = (row == 1 && col == 1)
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(36.dp)
                                                                        .clip(RoundedCornerShape(6.dp))
                                                                        .background(Color(color.rgb))
                                                                        .border(
                                                                            width = if (isCenter) 2.dp else 1.dp,
                                                                            color = if (isCenter) Color.White else Color(0x33FFFFFF),
                                                                            shape = RoundedCornerShape(6.dp)
                                                                        )
                                                                        .clickable(enabled = !isCenter) {
                                                                            val colorsList = listOf(
                                                                                CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
                                                                                CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
                                                                            )
                                                                            val curColor = grid[row][col]
                                                                            val nextIdx = (colorsList.indexOf(curColor) + 1) % colorsList.size
                                                                            val targetColor = colorsList[nextIdx]
                                                                            
                                                                            // Keep raw RGB mapping in sync with manual edits
                                                                            val rawGrid = scannedRawRGBs[currentFace]
                                                                            if (rawGrid != null) {
                                                                                val defaultReferences = mapOf(
                                                                                    CubeColor.ORANGE to IntVector3(255, 130, 0),
                                                                                    CubeColor.RED to IntVector3(220, 20, 20),
                                                                                    CubeColor.YELLOW to IntVector3(240, 240, 0),
                                                                                    CubeColor.WHITE to IntVector3(230, 230, 230),
                                                                                    CubeColor.GREEN to IntVector3(0, 160, 0),
                                                                                    CubeColor.BLUE to IntVector3(0, 0, 200)
                                                                                )
                                                                                val centerMapping = mapOf(
                                                                                    CubeColor.ORANGE to FaceName.U,
                                                                                    CubeColor.RED to FaceName.D,
                                                                                    CubeColor.YELLOW to FaceName.L,
                                                                                    CubeColor.WHITE to FaceName.R,
                                                                                    CubeColor.GREEN to FaceName.F,
                                                                                    CubeColor.BLUE to FaceName.B
                                                                                )
                                                                                val targetFace = centerMapping[targetColor]!!
                                                                                val refRGB = scannedRawRGBs[targetFace]?.get(1)?.get(1)
                                                                                    ?: defaultReferences[targetColor]!!
                                                                                    
                                                                                val updatedRawGrid = rawGrid.map { it.copyOf() }.toTypedArray()
                                                                                updatedRawGrid[row][col] = refRGB
                                                                                
                                                                                val updatedRaw = scannedRawRGBs.toMutableMap()
                                                                                updatedRaw[currentFace] = updatedRawGrid
                                                                                scannedRawRGBs = updatedRaw
                                                                            }
                                                                            
                                                                            // Also directly update classified grid to ensure it updates visually instantly
                                                                            val updatedGrid = grid.map { it.copyOf() }.toTypedArray()
                                                                            updatedGrid[row][col] = targetColor
                                                                            val updatedGrids = scannedGrids.toMutableMap()
                                                                            updatedGrids[currentFace] = updatedGrid
                                                                            scannedGrids = updatedGrids
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
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Button(
                                            onClick = {
                                                val updatedPaths = scannedFilePaths.toMutableMap()
                                                updatedPaths.remove(currentFace)
                                                scannedFilePaths = updatedPaths
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
                                    onClick = {
                                        showScannerWizard = false
                                        scannerStep = 0
                                        scannedGrids = mutableMapOf()
                                        scannedRawRGBs = mutableMapOf()
                                        scannedFilePaths = mutableMapOf()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("İptal")
                                }
                                
                                Button(
                                    onClick = { if (scannerStep > 0) scannerStep-- },
                                    enabled = scannerStep > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Geri")
                                }
                                
                                val hasCurrentScan = scannedGrids.containsKey(currentFace)
                                if (scannerStep < 5) {
                                    Button(
                                        onClick = { scannerStep++ },
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
                                            // Copy grids to editor
                                            val completeGrids = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
                                            var isValid = true
                                            for (face in FaceName.values()) {
                                                val gridVal = scannedGrids[face]
                                                if (gridVal != null) {
                                                    completeGrids[face] = gridVal
                                                } else {
                                                    isValid = false
                                                }
                                            }
                                            if (isValid) {
                                                editorFaces = completeGrids
                                                showScannerWizard = false
                                                scannerStep = 0
                                                errorMessage = null
                                            } else {
                                                errorMessage = "Lütfen tüm yüzeyleri taradığınızdan emin olun."
                                            }
                                        },
                                        enabled = scannedGrids.size == 6,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (scannedGrids.size == 6) Color(0xFFFF8A00) else Color(0x22FFFFFF),
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

            // Photo detection loading indicator overlay
            if (isDetecting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xB3000000))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFF8A00))
                            Text(
                                text = "Küp Fotoğrafları İşleniyor...",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Renkler algılanıyor, lütfen bekleyin...",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun parseDetectedState(jsonStr: String): Map<FaceName, Array<Array<CubeColor>>>? {
    try {
        val faces = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
        for (face in FaceName.values()) {
            val faceNameStr = "\"${face.name}\""
            val faceIndex = jsonStr.indexOf(faceNameStr)
            if (faceIndex == -1) return null
            
            var bracketCount = 0
            var startIndex = -1
            var endIndex = -1
            for (i in faceIndex + faceNameStr.length until jsonStr.length) {
                val char = jsonStr[i]
                if (char == '[') {
                    if (bracketCount == 0) {
                        startIndex = i
                    }
                    bracketCount++
                } else if (char == ']') {
                    bracketCount--
                    if (bracketCount == 0) {
                        endIndex = i
                        break
                    }
                }
            }
            if (startIndex == -1 || endIndex == -1) return null
            
            val arrayStr = jsonStr.substring(startIndex, endIndex + 1)
            val inner = arrayStr.trim().removeSurrounding("[", "]")
            val rows = inner.split("],")
            if (rows.size != 3) return null
            
            val grid = Array(3) { Array(3) { CubeColor.INTERNAL } }
            for (r in 0..2) {
                val rowClean = rows[r].replace("[", "").replace("]", "").replace("\"", "").trim()
                val cells = rowClean.split(",")
                if (cells.size != 3) return null
                for (c in 0..2) {
                    val colorName = cells[c].trim().uppercase()
                    grid[r][c] = CubeColor.valueOf(colorName)
                }
            }
            faces[face] = grid
        }
        return faces
    } catch (e: Exception) {
        return null
    }
}

@Composable
fun FaceGrid(
    face: FaceName,
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    onCellClick: (FaceName, Int, Int) -> Unit
) {
    val grid = faces[face]!!
    Column(
        modifier = Modifier
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(4.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (c in 0..2) {
                    val color = grid[r][c]
                    val isCenter = r == 1 && c == 1
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(color.rgb))
                            .clickable(enabled = !isCenter) {
                                onCellClick(face, r, c)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCenter) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }
        }
    }
}