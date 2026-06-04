package com.vahitkeskin.rubiksync

import com.vahitkeskin.rubiksync.ui.state.*
import com.vahitkeskin.rubiksync.ui.icons.CameraIcon
import com.vahitkeskin.rubiksync.ui.icons.GalleryIcon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import platform.UIKit.*
import platform.Foundation.*
import platform.CoreGraphics.*
import kotlinx.cinterop.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Image
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.memcpy
import platform.AVFoundation.*
import androidx.compose.ui.interop.UIKitView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.BorderStroke

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun runDetectionScript(): String {
    return """{"status":"error", "message":"Fotoğraf algılama özelliği yalnızca masaüstü platformlarında desteklenmektedir."}"""
}

@OptIn(ExperimentalForeignApi::class)
actual fun loadImagePixels(filePath: String): PixelGrid? {
    return try {
        val originalImage = UIImage.imageWithContentsOfFile(filePath) ?: return null
        
        // Correct rotation by rendering UIImage to a temporary context using drawInRect
        val size = originalImage.size
        val width = size.useContents { width }.toInt()
        val height = size.useContents { height }.toInt()
        
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        originalImage.drawInRect(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
        val image = UIGraphicsGetImageFromCurrentImageContext() ?: originalImage
        UIGraphicsEndImageContext()
        
        // Center crop to a perfect square
        val minDim = kotlin.math.min(width, height)
        val startX = (width - minDim) / 2.0
        val startY = (height - minDim) / 2.0
        
        val squareSize = CGSizeMake(minDim.toDouble(), minDim.toDouble())
        UIGraphicsBeginImageContextWithOptions(squareSize, false, 1.0)
        image.drawInRect(CGRectMake(-startX, -startY, width.toDouble(), height.toDouble()))
        val squareImage = UIGraphicsGetImageFromCurrentImageContext() ?: image
        UIGraphicsEndImageContext()
        
        val cgImage = squareImage.CGImage ?: return null
        val cgWidth = CGImageGetWidth(cgImage).toInt()
        val cgHeight = CGImageGetHeight(cgImage).toInt()
        
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val bytesPerPixel = 4
        val bytesPerRow = cgWidth * bytesPerPixel
        val bitsPerComponent = 8
        
        val totalBytes = cgWidth * cgHeight * bytesPerPixel
        val rawData = nativeHeap.allocArray<ByteVar>(totalBytes)
        
        val context = CGBitmapContextCreate(
            rawData,
            cgWidth.toULong(),
            cgHeight.toULong(),
            bitsPerComponent.toULong(),
            bytesPerRow.toULong(),
            colorSpace,
            CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
        ) ?: return null
        
        val rect = CGRectMake(0.0, 0.0, cgWidth.toDouble(), cgHeight.toDouble())
        CGContextDrawImage(context, rect, cgImage)
        CGContextRelease(context)
        
        object : PixelGrid {
            override val width: Int = cgWidth
            override val height: Int = cgHeight
            override fun getRGB(x: Int, y: Int): com.vahitkeskin.rubiksync.solver.IntVector3 {
                val index = (y * cgWidth + x) * 4
                val r = rawData[index].toInt() and 0xFF
                val g = rawData[index + 1].toInt() and 0xFF
                val b = rawData[index + 2].toInt() and 0xFF
                return com.vahitkeskin.rubiksync.solver.IntVector3(r, g, b)
            }
        }
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun loadImageBitmap(filePath: String): ImageBitmap? {
    return try {
        val originalImage = UIImage.imageWithContentsOfFile(filePath) ?: return null
        
        val size = originalImage.size
        val width = size.useContents { width }.toInt()
        val height = size.useContents { height }.toInt()
        
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        originalImage.drawInRect(CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble()))
        val image = UIGraphicsGetImageFromCurrentImageContext() ?: originalImage
        UIGraphicsEndImageContext()
        
        val minDim = kotlin.math.min(width, height)
        val startX = (width - minDim) / 2.0
        val startY = (height - minDim) / 2.0
        
        val squareSize = CGSizeMake(minDim.toDouble(), minDim.toDouble())
        UIGraphicsBeginImageContextWithOptions(squareSize, false, 1.0)
        image.drawInRect(CGRectMake(-startX, -startY, width.toDouble(), height.toDouble()))
        val squareImage = UIGraphicsGetImageFromCurrentImageContext() ?: image
        UIGraphicsEndImageContext()
        
        val data = UIImageJPEGRepresentation(squareImage, 0.8) ?: return null
        val bytes = data.bytes ?: return null
        val length = data.length.toInt()
        val byteArray = ByteArray(length)
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, data.length)
        }
        val skiaImage = Image.makeFromEncoded(byteArray)
        val skiaBitmap = org.jetbrains.skia.Bitmap.makeFromImage(skiaImage)
        skiaBitmap.asComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}

actual val isCameraSupported: Boolean = true

private var activeCameraDelegate: PhotoCaptureDelegate? = null

@OptIn(ExperimentalForeignApi::class)
class PhotoCaptureDelegate(
    private val onImageCaptured: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val viewW: Double,
    private val viewH: Double
) : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
    
    override fun captureOutput(
        output: AVCapturePhotoOutput,
        didFinishProcessingPhoto: AVCapturePhoto,
        error: NSError?
    ) {
        if (error != null) {
            onError("Fotoğraf işlenirken hata oluştu: ${error.localizedDescription}")
            activeCameraDelegate = null
            return
        }
        
        val photoData = didFinishProcessingPhoto.fileDataRepresentation()
        if (photoData == null) {
            onError("Fotoğraf verisi alınamadı.")
            activeCameraDelegate = null
            return
        }
        
        val originalImage = UIImage.imageWithData(photoData)
        if (originalImage == null) {
            onError("Görsel oluşturulamadı.")
            activeCameraDelegate = null
            return
        }
        
        // 1. Draw to correct orientation
        val size = originalImage.size
        val width = size.useContents { width }
        val height = size.useContents { height }
        
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        originalImage.drawInRect(CGRectMake(0.0, 0.0, width, height))
        val orientedImage = UIGraphicsGetImageFromCurrentImageContext() ?: originalImage
        UIGraphicsEndImageContext()
        
        // 2. Crop to aspect ratio of viewW : viewH using AspectFill calculation
        val orientedSize = orientedImage.size
        val wImg = orientedSize.useContents { width }
        val hImg = orientedSize.useContents { height }
        
        val scale = kotlin.math.max(viewW / wImg, viewH / hImg)
        val visibleW = viewW / scale
        val visibleH = viewH / scale
        
        val startX = (wImg - visibleW) / 2.0
        val startY = (hImg - visibleH) / 2.0
        
        val targetSize = CGSizeMake(visibleW, visibleH)
        UIGraphicsBeginImageContextWithOptions(targetSize, false, 1.0)
        orientedImage.drawInRect(CGRectMake(-startX, -startY, wImg, hImg))
        val croppedImage = UIGraphicsGetImageFromCurrentImageContext() ?: orientedImage
        UIGraphicsEndImageContext()
        
        val data = UIImageJPEGRepresentation(croppedImage, 0.8)
        if (data != null) {
            val tempDir = NSTemporaryDirectory()
            val fileName = "temp_ios_camera_${NSDate().timeIntervalSince1970.toLong()}.jpg"
            val path = tempDir + fileName
            if (data.writeToFile(path, true)) {
                onImageCaptured(path)
            } else {
                onError("Fotoğraf dosyaya yazılamadı.")
            }
        } else {
            onError("Fotoğraf sıkıştırılamadı.")
        }
        activeCameraDelegate = null
    }
}

@OptIn(ExperimentalForeignApi::class)
class CameraViewController(
    private val onImageCaptured: (String) -> Unit,
    private val onError: (String) -> Unit
) : UIViewController(nibName = null, bundle = null) {
    
    val captureSession = AVCaptureSession()
    val photoOutput = AVCapturePhotoOutput()
    var previewLayer: AVCaptureVideoPreviewLayer? = null
    var device: AVCaptureDevice? = null
    var flashEnabled by mutableStateOf(false)
    private val cameraQueue = dispatch_queue_create("com.vahitkeskin.rubiksync.camera", null)
    
    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.blackColor
        setupSession()
    }
    
    private fun setupSession() {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        if (device == null) {
            onError("Kamera aygıtı bulunamadı.")
            return
        }
        this.device = device
        
        val input = memScoped {
            val errorVar = alloc<ObjCObjectVar<NSError?>>()
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, errorVar.ptr)
            val error = errorVar.value
            if (error != null) {
                onError("Kamera girişi oluşturulamadı: ${error.localizedDescription}")
                return
            }
            input
        }
        
        if (input == null) {
            onError("Kamera girişi oluşturulamadı.")
            return
        }
        
        captureSession.beginConfiguration()
        
        if (captureSession.canAddInput(input)) {
            captureSession.addInput(input)
        } else {
            onError("Kamera girişi eklenemedi.")
            captureSession.commitConfiguration()
            return
        }
        
        if (captureSession.canAddOutput(photoOutput)) {
            captureSession.addOutput(photoOutput)
        } else {
            onError("Kamera çıktısı eklenemedi.")
            captureSession.commitConfiguration()
            return
        }
        
        captureSession.commitConfiguration()
        
        val previewLayer = AVCaptureVideoPreviewLayer.layerWithSession(captureSession)
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        view.layer.addSublayer(previewLayer)
        this.previewLayer = previewLayer
    }
    
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        previewLayer?.frame = view.bounds
    }
    
    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        dispatch_async(cameraQueue) {
            captureSession.startRunning()
        }
    }
    
    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        dispatch_async(cameraQueue) {
            captureSession.stopRunning()
        }
    }
    
    fun toggleFlash() {
        val device = this.device ?: return
        if (device.hasFlash) {
            memScoped {
                val errorVar = alloc<ObjCObjectVar<NSError?>>()
                if (device.lockForConfiguration(errorVar.ptr)) {
                    flashEnabled = !flashEnabled
                    device.unlockForConfiguration()
                } else {
                    val error = errorVar.value
                    onError("Flaş yapılandırılamadı: ${error?.localizedDescription}")
                }
            }
        }
    }
    
    fun capturePhoto() {
        val settings = AVCapturePhotoSettings.photoSettings()
        if (device?.hasFlash == true && flashEnabled) {
            settings.flashMode = AVCaptureFlashModeOn
        } else {
            settings.flashMode = AVCaptureFlashModeOff
        }
        
        val bounds = view.bounds
        val viewW = bounds.useContents { size.width }
        val viewH = bounds.useContents { size.height }
        
        val delegate = PhotoCaptureDelegate(onImageCaptured, onError, viewW, viewH)
        activeCameraDelegate = delegate
        photoOutput.capturePhotoWithSettings(settings, delegate)
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun CameraPreviewDialog(
    faceName: String,
    onDismiss: () -> Unit,
    onImageCaptured: (String) -> Unit
) {
    val cameraVC = remember {
        CameraViewController(
            onImageCaptured = { path ->
                onImageCaptured(path)
            },
            onError = { err ->
                println("Camera Error: $err")
            }
        )
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            UIKitView(
                factory = { cameraVC.view },
                modifier = Modifier.fillMaxSize()
            )
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                val scale = 0.55f
                val minDim = kotlin.math.min(w, h)
                val gridSize = minDim * scale
                
                val left = (w - gridSize) / 2f
                val top = (h - gridSize) / 2f
                
                drawRect(
                    color = AccentYellowSun,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(gridSize, gridSize),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
                
                val cellSize = gridSize / 3f
                
                for (i in 1..2) {
                    drawLine(
                        color = AccentGreenEmerald,
                        start = androidx.compose.ui.geometry.Offset(left + i * cellSize, top),
                        end = androidx.compose.ui.geometry.Offset(left + i * cellSize, top + gridSize),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                for (i in 1..2) {
                    drawLine(
                        color = AccentGreenEmerald,
                        start = androidx.compose.ui.geometry.Offset(left, top + i * cellSize),
                        end = androidx.compose.ui.geometry.Offset(left + gridSize, top + i * cellSize),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                
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
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .background(WhiteAlpha20, RoundedCornerShape(24.dp))
                ) {
                    Text("✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White, RoundedCornerShape(36.dp))
                        .clickable {
                            cameraVC.capturePhoto()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(31.dp))
                    )
                }
                
                IconButton(
                    onClick = {
                        cameraVC.toggleFlash()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (cameraVC.flashEnabled) AccentOrange else WhiteAlpha20,
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    Text(
                        text = if (cameraVC.flashEnabled) "⚡" else "💡",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
class ImagePickerDelegate(
    private val onImageSelected: (String) -> Unit,
    private val onDismiss: () -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        if (image != null) {
            val data = UIImageJPEGRepresentation(image, 0.8)
            if (data != null) {
                val tempDir = NSTemporaryDirectory()
                val fileName = "temp_ios_capture_${NSDate().timeIntervalSince1970.toLong()}.jpg"
                val path = tempDir + fileName
                data.writeToFile(path, true)
                onImageSelected(path)
            }
        }
        picker.dismissViewControllerAnimated(true, null)
        onDismiss()
    }
    
    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, null)
        onDismiss()
    }
}

fun getTopViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    var topController = keyWindow?.rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }
    return topController
}

@Composable
actual fun CameraCaptureOrPicker(
    faceName: String,
    takePhotoLabel: String,
    chooseGalleryLabel: String,
    selectImageLabel: String,
    onImageSelected: (String) -> Unit,
    modifier: Modifier
) {
    var showPermissionExplanation by remember { mutableStateOf(false) }
    var showSettingsExplanation by remember { mutableStateOf(false) }
    var showCameraPreview by remember { mutableStateOf(false) }
    
    var activeDelegate by remember { mutableStateOf<ImagePickerDelegate?>(null) }
    
    fun launchGallery() {
        val topVC = getTopViewController() ?: return
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        
        val delegate = ImagePickerDelegate(
            onImageSelected = onImageSelected,
            onDismiss = { activeDelegate = null }
        )
        activeDelegate = delegate
        picker.delegate = delegate
        
        topVC.presentViewController(picker, animated = true, completion = null)
    }
    
    fun checkAndLaunchCamera() {
        val cameraAvailable = UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        )
        
        if (!cameraAvailable) {
            launchGallery()
            return
        }
        
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        when (status) {
            AVAuthorizationStatusAuthorized -> {
                showCameraPreview = true
            }
            AVAuthorizationStatusNotDetermined -> {
                showPermissionExplanation = true
            }
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                showSettingsExplanation = true
            }
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
            onClick = { launchGallery() },
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
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
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
                        color = LightCardBorder,
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
                                containerColor = WhiteAlpha07,
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
                                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                                    dispatch_async(dispatch_get_main_queue()) {
                                        if (granted) {
                                            showCameraPreview = true
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
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
    
    if (showSettingsExplanation) {
        Dialog(onDismissRequest = { showSettingsExplanation = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardBg),
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
                        text = "Kamera İzni Devre Dışı",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Kamera izni daha önce reddedilmiş. Lütfen Ayarlar'dan kameraya izin verin.",
                        color = LightCardBorder,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showSettingsExplanation = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WhiteAlpha07,
                                contentColor = Color.LightGray
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Vazgeç", fontSize = 13.sp)
                        }
                        
                        Button(
                            onClick = {
                                showSettingsExplanation = false
                                val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
                                if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
                                    UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any?>(), completionHandler = null)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ayarlar", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

actual fun logMoveDetail(moveLabel: String, phase: String, mathDetails: String) {
    println("RubikSyncMath: Move: $moveLabel ($phase)\n$mathDetails")
}

actual fun getCurrentYear(): Int {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy"
    return formatter.stringFromDate(NSDate()).toIntOrNull() ?: 2026
}

actual fun getSystemLanguageCode(): String {
    val languages = NSLocale.preferredLanguages
    val firstLang = languages.firstOrNull() as? String ?: "en"
    return firstLang.split("-").firstOrNull() ?: "en"
}

@Composable
actual fun BindBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS
}

// --- Cube Sound Implementation ---

actual fun initCubeSound() {
    // No-op on iOS
}

actual fun playCubeRotateSound() {
    // No-op on iOS
}

actual fun releaseCubeSound() {
    // No-op on iOS
}