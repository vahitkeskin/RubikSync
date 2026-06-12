package com.vahitkeskin.rubiksync

import com.vahitkeskin.rubiksync.ui.state.*
import com.vahitkeskin.rubiksync.ui.icons.CameraIcon
import com.vahitkeskin.rubiksync.ui.icons.GalleryIcon

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import com.vahitkeskin.rubiksync.solver.IntVector3
import java.io.File
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.platform.LocalLifecycleOwner
import java.util.concurrent.Executors

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun runDetectionScript(): String {
    return """{"status":"error", "message":"Fotoğraf algılama özelliği yalnızca masaüstü platformlarında desteklenmektedir."}"""
}

actual fun loadImagePixels(filePath: String): PixelGrid? {
    return try {
        val file = File(filePath)
        if (!file.exists()) return null
        val originalBitmap = android.graphics.BitmapFactory.decodeFile(filePath) ?: return null
        
        // Correct rotation based on EXIF metadata
        val exif = android.media.ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            android.media.ExifInterface.TAG_ORIENTATION,
            android.media.ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = android.graphics.Matrix()
        var needRotate = false
        when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(90f)
                needRotate = true
            }
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180f)
                needRotate = true
            }
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.postRotate(270f)
                needRotate = true
            }
        }
        
        val bitmap = if (needRotate) {
            val rotated = android.graphics.Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )
            if (rotated != originalBitmap) {
                originalBitmap.recycle()
            }
            rotated
        } else {
            originalBitmap
        }

        val minDim = kotlin.math.min(bitmap.width, bitmap.height)
        val startX = (bitmap.width - minDim) / 2
        val startY = (bitmap.height - minDim) / 2
        val squareBitmap = android.graphics.Bitmap.createBitmap(bitmap, startX, startY, minDim, minDim)
        if (squareBitmap != bitmap) {
            bitmap.recycle()
        }
        
        object : PixelGrid {
            override val width: Int = squareBitmap.width
            override val height: Int = squareBitmap.height
            override fun getRGB(x: Int, y: Int): IntVector3 {
                val pixel = squareBitmap.getPixel(x, y)
                val r = android.graphics.Color.red(pixel)
                val g = android.graphics.Color.green(pixel)
                val b = android.graphics.Color.blue(pixel)
                return IntVector3(r, g, b)
            }
        }
    } catch (e: Exception) {
        null
    }
}

actual fun loadImageBitmap(filePath: String): ImageBitmap? {
    return try {
        val file = File(filePath)
        if (!file.exists()) return null
        val originalBitmap = android.graphics.BitmapFactory.decodeFile(filePath) ?: return null
        
        // Correct rotation based on EXIF metadata
        val exif = android.media.ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            android.media.ExifInterface.TAG_ORIENTATION,
            android.media.ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = android.graphics.Matrix()
        var needRotate = false
        when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(90f)
                needRotate = true
            }
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180f)
                needRotate = true
            }
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.postRotate(270f)
                needRotate = true
            }
        }
        val bitmap = if (needRotate) {
            val rotated = android.graphics.Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )
            if (rotated != originalBitmap) originalBitmap.recycle()
            rotated
        } else {
            originalBitmap
        }

        val minDim = kotlin.math.min(bitmap.width, bitmap.height)
        val startX = (bitmap.width - minDim) / 2
        val startY = (bitmap.height - minDim) / 2
        val squareBitmap = android.graphics.Bitmap.createBitmap(bitmap, startX, startY, minDim, minDim)
        if (squareBitmap != bitmap) {
            bitmap.recycle()
        }
        squareBitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

private fun cropImageToAspectRatio(filePath: String, targetWidth: Float, targetHeight: Float) {
    try {
        val file = File(filePath)
        if (!file.exists() || targetWidth <= 0f || targetHeight <= 0f) return
        val originalBitmap = android.graphics.BitmapFactory.decodeFile(filePath) ?: return
        
        val exif = android.media.ExifInterface(filePath)
        val orientation = exif.getAttributeInt(
            android.media.ExifInterface.TAG_ORIENTATION,
            android.media.ExifInterface.ORIENTATION_NORMAL
        )
        val matrix = android.graphics.Matrix()
        var needRotate = false
        when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(90f)
                needRotate = true
            }
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180f)
                needRotate = true
            }
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.postRotate(270f)
                needRotate = true
            }
        }
        val bitmap = if (needRotate) {
            val rotated = android.graphics.Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )
            if (rotated != originalBitmap) originalBitmap.recycle()
            rotated
        } else {
            originalBitmap
        }
        
        val wImg = bitmap.width.toFloat()
        val hImg = bitmap.height.toFloat()
        
        val scale = kotlin.math.max(targetWidth / wImg, targetHeight / hImg)
        val visibleW = targetWidth / scale
        val visibleH = targetHeight / scale
        
        val startX = ((wImg - visibleW) / 2f).coerceAtLeast(0f).toInt()
        val startY = ((hImg - visibleH) / 2f).coerceAtLeast(0f).toInt()
        val cropW = visibleW.toInt().coerceAtMost(bitmap.width - startX)
        val cropH = visibleH.toInt().coerceAtMost(bitmap.height - startY)
        
        if (cropW > 0 && cropH > 0) {
            val croppedBitmap = android.graphics.Bitmap.createBitmap(bitmap, startX, startY, cropW, cropH)
            if (croppedBitmap != bitmap) {
                bitmap.recycle()
            }
            file.outputStream().use { out ->
                croppedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
            }
            croppedBitmap.recycle()
            
            val newExif = android.media.ExifInterface(filePath)
            newExif.setAttribute(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL.toString())
            newExif.saveAttributes()
        } else {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

actual val isCameraSupported: Boolean = true

@Composable
fun CameraPreviewDialog(
    faceName: String,
    guidanceText: String,
    onDismiss: () -> Unit,
    onImageCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    LaunchedEffect(flashMode) {
        imageCapture.flashMode = flashMode
    }
    
    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Live PreviewView
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            
            // Alignment Grid and Guidelines Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                val scale = 0.55f
                val minDim = kotlin.math.min(w, h)
                val gridSize = minDim * scale
                
                val left = (w - gridSize) / 2f
                val top = (h - gridSize) / 2f
                
                // Draw yellow dashed crop boundaries
                drawRect(
                    color = AccentYellowSun,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(gridSize, gridSize),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
                
                // Draw green grid divisions
                val cellSize = gridSize / 3f
                
                // Verticals
                for (i in 1..2) {
                    drawLine(
                        color = AccentGreenEmerald,
                        start = androidx.compose.ui.geometry.Offset(left + i * cellSize, top),
                        end = androidx.compose.ui.geometry.Offset(left + i * cellSize, top + gridSize),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                // Horizontals
                for (i in 1..2) {
                    drawLine(
                        color = AccentGreenEmerald,
                        start = androidx.compose.ui.geometry.Offset(left, top + i * cellSize),
                        end = androidx.compose.ui.geometry.Offset(left + gridSize, top + i * cellSize),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                
                // Small green dot at the center of each of the 9 facelets
                for (r in 0..2) {
                    for (c in 0..2) {
                        val cx = left + (c + 0.5f) * cellSize
                        val cy = top + (r + 0.5f) * cellSize
                        drawCircle(
                            color = AccentGreenEmerald,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(cx, cy)
                        )
                    }
                }
            }
            
            // Top instructions bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Küp yüzünü sarı çerçeve içine hizalayın",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = guidanceText,
                        color = AccentOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Capture Buttons controls bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(bottom = 32.dp, top = 24.dp)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel Button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(WhiteAlpha20, RoundedCornerShape(24.dp))
                ) {
                    Text("✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                // Custom Shutter/Capture Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White, RoundedCornerShape(36.dp))
                        .clickable {
                            val tempFile = File(context.cacheDir, "camera_capture_${faceName}_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()
                            imageCapture.takePicture(
                                outputOptions,
                                cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        cropImageToAspectRatio(tempFile.absolutePath, previewView.width.toFloat(), previewView.height.toFloat())
                                        onImageCaptured(tempFile.absolutePath)
                                    }
                                    
                                    override fun onError(exception: ImageCaptureException) {
                                        exception.printStackTrace()
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(31.dp))
                    )
                }
                
                // Flash Toggle Button
                IconButton(
                    onClick = {
                        flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) {
                            ImageCapture.FLASH_MODE_ON
                        } else {
                            ImageCapture.FLASH_MODE_OFF
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (flashMode == ImageCapture.FLASH_MODE_ON) AccentOrange else WhiteAlpha20,
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Text(
                        text = if (flashMode == ImageCapture.FLASH_MODE_ON) "⚡" else "💡",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
actual fun CameraCaptureOrPicker(
    faceName: String,
    takePhotoLabel: String,
    chooseGalleryLabel: String,
    selectImageLabel: String,
    guidanceText: String,
    permissionRequiredTitle: String,
    cameraPermissionDesc: String,
    permissionLater: String,
    permissionGrant: String,
    cameraPermissionDenied: String,
    onImageSelected: (String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    var showPermissionExplanation by remember { mutableStateOf(false) }
    var showCameraPreview by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraPreview = true
        } else {
            Toast.makeText(context, cameraPermissionDenied, Toast.LENGTH_LONG).show()
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val galleryTempFile = File(context.cacheDir, "gallery_capture_${faceName}.jpg")
                if (galleryTempFile.exists()) galleryTempFile.delete()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    galleryTempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (galleryTempFile.exists()) {
                    onImageSelected(galleryTempFile.absolutePath)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Görsel yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun checkAndLaunchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission) {
            showCameraPreview = true
        } else {
            showPermissionExplanation = true
        }
    }
    
    val isDark = RubikTheme.colors.isDark
    val accentBlue = RubikTheme.colors.accentBlue
    val accentOrange = RubikTheme.colors.accentOrange

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Camera Card
        Card(
            onClick = { checkAndLaunchCamera() },
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) accentBlue.copy(alpha = 0.16f) else accentBlue.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 2.dp,
                color = if (isDark) accentBlue.copy(alpha = 0.7f) else accentBlue
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = CameraIcon,
                    contentDescription = takePhotoLabel,
                    tint = accentBlue,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = takePhotoLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else accentBlue,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
        
        // Gallery Card
        Card(
            onClick = { 
                Toast.makeText(context, guidanceText, Toast.LENGTH_LONG).show()
                galleryLauncher.launch("image/*") 
            },
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) accentOrange.copy(alpha = 0.16f) else accentOrange.copy(alpha = 0.08f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 2.dp,
                color = if (isDark) accentOrange.copy(alpha = 0.7f) else accentOrange
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = GalleryIcon,
                    contentDescription = chooseGalleryLabel,
                    tint = accentOrange,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = chooseGalleryLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else accentOrange,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
    
    if (showCameraPreview) {
        CameraPreviewDialog(
            faceName = faceName,
            guidanceText = guidanceText,
            onDismiss = { showCameraPreview = false },
            onImageCaptured = { filePath ->
                showCameraPreview = false
                onImageSelected(filePath)
            }
        )
    }
    
    if (showPermissionExplanation) {
        com.vahitkeskin.rubiksync.ui.components.CustomPopupDialog(
            icon = CameraIcon,
            title = permissionRequiredTitle,
            description = cameraPermissionDesc,
            cancelText = permissionLater,
            confirmText = permissionGrant,
            onDismiss = { showPermissionExplanation = false },
            onConfirm = {
                showPermissionExplanation = false
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}

actual fun logMoveDetail(moveLabel: String, phase: String, mathDetails: String) {
    timber.log.Timber.tag("RubikSyncMath").d("Move: $moveLabel (%s)\n%s", phase, mathDetails)
}

actual fun getCurrentYear(): Int {
    return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
}

actual fun getSystemLanguageCode(): String {
    return java.util.Locale.getDefault().language
}

@Composable
actual fun BindBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

@Composable
actual fun rememberShakeDetector(enabled: Boolean, onShake: () -> Unit) {
    if (!enabled) return

    val context = LocalContext.current
    val currentOnShake = rememberUpdatedState(onShake)

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        val accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)

        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f
        var lastUpdate = 0L

        val shakeThreshold = 12.0f
        val shakeIntervalMs = 500L
        var lastShakeTime = 0L

        val listener = object : android.hardware.SensorEventListener {
            override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                if (event == null) return
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val currentTime = System.currentTimeMillis()
                val diffTime = currentTime - lastUpdate
                if (diffTime > 100) {
                    if (lastUpdate != 0L) {
                        val deltaX = x - lastX
                        val deltaY = y - lastY
                        val deltaZ = z - lastZ

                        val acceleration = kotlin.math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()
                        if (acceleration > shakeThreshold) {
                            if (currentTime - lastShakeTime > shakeIntervalMs) {
                                lastShakeTime = currentTime
                                currentOnShake.value()
                            }
                        }
                    }
                    lastX = x
                    lastY = y
                    lastZ = z
                    lastUpdate = currentTime
                }
            }

            override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {}
        }

        if (accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }
}

// --- Cube Sound Implementation ---

private var appContext: android.content.Context? = null
private var soundPool: android.media.SoundPool? = null
private var rotateSoundId: Int = 0
private var isSoundLoaded = false

fun setAppContext(context: android.content.Context) {
    appContext = context.applicationContext
}

actual fun initCubeSound() {
    val context = appContext ?: return
    if (soundPool != null) return // Already initialized

    val audioAttributes = android.media.AudioAttributes.Builder()
        .setUsage(android.media.AudioAttributes.USAGE_GAME)
        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    val pool = android.media.SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(audioAttributes)
        .build()

    soundPool = pool

    // Resolve resource ID dynamically to avoid compile-time dependency on app module's R class
    val resId = context.resources.getIdentifier("cube_rotate", "raw", context.packageName)
    if (resId != 0) {
        rotateSoundId = pool.load(context, resId, 1)
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == rotateSoundId && status == 0) {
                isSoundLoaded = true
            }
        }
    }
}

actual fun playCubeRotateSound() {
    if (isSoundLoaded) {
        soundPool?.play(rotateSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }
}

actual fun releaseCubeSound() {
    soundPool?.release()
    soundPool = null
    isSoundLoaded = false
}

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

@Composable
actual fun PlatformWebView(
    url: String,
    modifier: androidx.compose.ui.Modifier
) {
    var rawMarkdown by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 10000
                conn.readTimeout = 10000
                rawMarkdown = conn.inputStream.bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                errorText = e.message ?: "Network error"
            }
        }
    }

    if (errorText != null) {
        Box(
            modifier = modifier
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Error loading documentation:\n$errorText",
                color = RubikTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
        }
    } else if (rawMarkdown == null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RubikTheme.colors.accentBlue)
        }
    } else {
        val isDark = RubikTheme.colors.isDark
        val colorMode = if (isDark) "dark" else "light"

        val cssUrl = "https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.5.1/github-markdown-$colorMode.min.css"

        val base64Markdown = kotlin.io.encoding.Base64.encode(rawMarkdown!!.encodeToByteArray())

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link id="theme-stylesheet" rel="stylesheet" href="$cssUrl">
                <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
                <style>
                    html, body {
                        background-color: transparent !important;
                        margin: 0;
                        padding: 0;
                        width: 100%;
                        height: 100%;
                    }
                    body {
                        box-sizing: border-box;
                        padding: 16px;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
                    }
                    .markdown-body {
                        box-sizing: border-box;
                        min-width: 200px;
                        max-width: 980px;
                        margin: 0 auto;
                        background-color: transparent !important;
                    }
                    @media (max-width: 767px) {
                        .markdown-body {
                            padding: 8px;
                        }
                    }
                </style>
            </head>
            <body class="markdown-body" data-color-mode="$colorMode" data-dark-theme="dark" data-light-theme="light">
                <div id="content">Loading...</div>
                <script>
                    try {
                        const base64Str = "$base64Markdown";
                        const binString = atob(base64Str);
                        const bytes = Uint8Array.from(binString, (m) => m.codePointAt(0));
                        const markdown = new TextDecoder().decode(bytes);
                        document.getElementById('content').innerHTML = marked.parse(markdown);
                    } catch (err) {
                        document.getElementById('content').innerHTML = "<h1>Error parsing documentation</h1><p>" + err.message + "</p>";
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        val htmlRef = remember {
            object {
                var value = ""
                var markdown = ""
            }
        }

        androidx.compose.ui.viewinterop.AndroidView(
            factory = { context ->
                android.webkit.WebView(context).apply {
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    webChromeClient = android.webkit.WebChromeClient()
                    webViewClient = android.webkit.WebViewClient()
                }
            },
            update = { webView ->
                if (htmlRef.value != htmlContent) {
                    val isMarkdownChanged = htmlRef.markdown != rawMarkdown
                    htmlRef.value = htmlContent
                    htmlRef.markdown = rawMarkdown!!

                    if (isMarkdownChanged) {
                        val baseURL = url.substringBeforeLast("/") + "/"
                        webView.loadDataWithBaseURL(baseURL, htmlContent, "text/html", "UTF-8", null)
                    } else {
                        val js = """
                            (function() {
                                const link = document.getElementById('theme-stylesheet');
                                if (link) {
                                    link.href = '$cssUrl';
                                }
                                const body = document.body;
                                if (body) {
                                    body.setAttribute('data-color-mode', '$colorMode');
                                }
                            })();
                        """.trimIndent()
                        webView.evaluateJavascript(js, null)
                    }
                }
            },
            modifier = modifier
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun CameraCaptureOrPickerPreview() {
    ThemeDualPreview(label = "Camera Capture or Picker (Android)") {
        CameraCaptureOrPicker(
            faceName = "F",
            takePhotoLabel = "Fotoğraf Çek",
            chooseGalleryLabel = "Galeriden Seç",
            selectImageLabel = "Resim Seçin",
            guidanceText = "Lütfen Ön (Yeşil) yüzeyini tarayın.",
            permissionRequiredTitle = "Kamera İzni Gerekli",
            cameraPermissionDesc = "Açıklama",
            permissionLater = "Daha Sonra",
            permissionGrant = "İzin Ver",
            cameraPermissionDenied = "Reddedildi",
            onImageSelected = {},
            modifier = Modifier.width(300.dp)
        )
    }
}