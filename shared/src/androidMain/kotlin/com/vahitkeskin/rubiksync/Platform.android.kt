package com.vahitkeskin.rubiksync

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
                    color = Color(0xFFF1C40F),
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
                        color = Color(0xFF2ECC71),
                        start = androidx.compose.ui.geometry.Offset(left + i * cellSize, top),
                        end = androidx.compose.ui.geometry.Offset(left + i * cellSize, top + gridSize),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                // Horizontals
                for (i in 1..2) {
                    drawLine(
                        color = Color(0xFF2ECC71),
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
                            color = Color(0xFF2ECC71),
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
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Küp yüzünü sarı çerçeve içine hizalayın",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
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
                        .background(Color(0x33FFFFFF), RoundedCornerShape(24.dp))
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
                            if (flashMode == ImageCapture.FLASH_MODE_ON) Color(0xFFFF8A00) else Color(0x33FFFFFF),
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
            Toast.makeText(context, "Kamera izni reddedildi. Taramak için galeriyi kullanabilirsiniz.", Toast.LENGTH_LONG).show()
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
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { checkAndLaunchCamera() },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF448AFF),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("📷 Fotoğraf Çek", fontSize = 13.sp)
        }
        
        Button(
            onClick = { galleryLauncher.launch("image/*") },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0x22FFFFFF),
                contentColor = Color.LightGray
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("🖼️ Galeriden Seç", fontSize = 13.sp)
        }
    }
    
    if (showCameraPreview) {
        CameraPreviewDialog(
            faceName = faceName,
            onDismiss = { showCameraPreview = false },
            onImageCaptured = { filePath ->
                showCameraPreview = false
                onImageSelected(filePath)
            }
        )
    }
    
    if (showPermissionExplanation) {
        Dialog(onDismissRequest = { showPermissionExplanation = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Kamera İzni Gerekli",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Zeka küpünün renklerini otomatik olarak algılamak için kameranızı kullanmamız gerekiyor. Kameranızla küpün yüzlerini fotoğraflayarak saniyeler içinde çözümü görebilirsiniz. İzniniz güvenle saklanır.",
                        color = Color(0xFFB0BEC5),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showPermissionExplanation = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x11FFFFFF),
                                contentColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Daha Sonra", fontSize = 13.sp)
                        }
                        
                        Button(
                            onClick = {
                                showPermissionExplanation = false
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF448AFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("İzin Ver", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

actual fun logMoveDetail(moveLabel: String, phase: String, mathDetails: String) {
    timber.log.Timber.tag("RubikSyncMath").d("Move: $moveLabel (%s)\n%s", phase, mathDetails)
}

actual fun getCurrentYear(): Int {
    return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
}

@Composable
actual fun BindBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}