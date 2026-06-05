package com.vahitkeskin.rubiksync

import com.vahitkeskin.rubiksync.ui.state.*
import com.vahitkeskin.rubiksync.ui.icons.GalleryIcon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.File

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun runDetectionScript(): String {
    return try {
        val userDir = System.getProperty("user.dir")
        
        // Try python3 first
        var output = executeCommand(listOf("python3", "scripts/detect_cube.py"), userDir)
        if (output.contains("Pillow kütüphanesi") || output.contains("status")) {
            return output
        }
        
        // Fallback to python
        output = executeCommand(listOf("python", "scripts/detect_cube.py"), userDir)
        output
    } catch (e: Exception) {
        """{"status":"error", "message":"Python betiği çalıştırılamadı: ${e.message}"}"""
    }
}

private fun executeCommand(command: List<String>, workingDir: String): String {
    val process = ProcessBuilder(command)
        .directory(File(workingDir))
        .redirectErrorStream(true)
        .start()
        
    val reader = BufferedReader(InputStreamReader(process.inputStream, "UTF-8"))
    val builder = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        builder.append(line)
    }
    process.waitFor()
    return builder.toString()
}

actual fun loadImagePixels(filePath: String): PixelGrid? {
    return try {
        val file = File(filePath)
        if (!file.exists()) return null
        val originalImage = javax.imageio.ImageIO.read(file) ?: return null
        val minDim = kotlin.math.min(originalImage.width, originalImage.height)
        val startX = (originalImage.width - minDim) / 2
        val startY = (originalImage.height - minDim) / 2
        val image = originalImage.getSubimage(startX, startY, minDim, minDim)
        object : PixelGrid {
            override val width: Int = image.width
            override val height: Int = image.height
            override fun getRGB(x: Int, y: Int): com.vahitkeskin.rubiksync.solver.IntVector3 {
                val rgb = image.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                return com.vahitkeskin.rubiksync.solver.IntVector3(r, g, b)
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
        val originalImage = javax.imageio.ImageIO.read(file) ?: return null
        val minDim = kotlin.math.min(originalImage.width, originalImage.height)
        val startX = (originalImage.width - minDim) / 2
        val startY = (originalImage.height - minDim) / 2
        val image = originalImage.getSubimage(startX, startY, minDim, minDim)
        image.toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}

actual val isCameraSupported: Boolean = false

@Composable
actual fun CameraCaptureOrPicker(
    faceName: String,
    takePhotoLabel: String,
    chooseGalleryLabel: String,
    selectImageLabel: String,
    onImageSelected: (String) -> Unit,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Button(
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, selectImageLabel, java.awt.FileDialog.LOAD)
                    fileDialog.setFilenameFilter { _, name ->
                        val lower = name.lowercase()
                        lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                    }
                    fileDialog.isVisible = true
                    val dir = fileDialog.directory
                    val file = fileDialog.file
                    if (dir != null && file != null) {
                        val fullPath = File(dir, file).absolutePath
                        onImageSelected(fullPath)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = RubikTheme.colors.backgroundSecondary,
            contentColor = RubikTheme.colors.textPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
        modifier = modifier.height(42.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = GalleryIcon,
                contentDescription = "Select Image",
                modifier = Modifier.size(16.dp),
                tint = RubikTheme.colors.textPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = selectImageLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

actual fun logMoveDetail(moveLabel: String, phase: String, mathDetails: String) {
    println("RubikSyncMath: Move: $moveLabel ($phase)\n$mathDetails")
}

actual fun getCurrentYear(): Int {
    return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
}

actual fun getSystemLanguageCode(): String {
    return java.util.Locale.getDefault().language
}

@Composable
actual fun BindBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on JVM/Desktop
}

@Composable
actual fun rememberShakeDetector(enabled: Boolean, onShake: () -> Unit) {
    // No-op on JVM/Desktop
}

// --- Cube Sound Implementation ---

actual fun initCubeSound() {
    // No-op on JVM/Desktop
}

actual fun playCubeRotateSound() {
    // No-op on JVM/Desktop
}

actual fun releaseCubeSound() {
    // No-op on JVM/Desktop
}