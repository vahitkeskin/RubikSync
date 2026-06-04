package com.vahitkeskin.rubiksync

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.foundation.shape.RoundedCornerShape
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
    onImageSelected: (String) -> Unit,
    modifier: Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Button(
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Resim Seç (${faceName})", java.awt.FileDialog.LOAD)
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
            containerColor = Color(0xFF3B4B66),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text("📁 Resim Seç", fontSize = 14.sp)
    }
}

actual fun logMoveDetail(moveLabel: String, phase: String, mathDetails: String) {
    println("RubikSyncMath: Move: $moveLabel ($phase)\n$mathDetails")
}