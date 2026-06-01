package com.vahitkeskin.rubiksync.cube

import com.vahitkeskin.rubiksync.PixelGrid
import com.vahitkeskin.rubiksync.loadImagePixels
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class RubikImageProcessor {

    // Converts RGB (0-255) to CIE L*a*b*
    fun rgbToLab(r: Int, g: Int, b: Int): Triple<Double, Double, Double> {
        // 1. Convert RGB to XYZ
        var rL = r / 255.0
        var gL = g / 255.0
        var bL = b / 255.0

        rL = if (rL > 0.04045) ((rL + 0.055) / 1.055).pow(2.4) else rL / 12.92
        gL = if (gL > 0.04045) ((gL + 0.055) / 1.055).pow(2.4) else gL / 12.92
        bL = if (bL > 0.04045) ((bL + 0.055) / 1.055).pow(2.4) else bL / 12.92

        rL *= 100.0
        gL *= 100.0
        bL *= 100.0

        // D65 illuminant coefficients
        val x = rL * 0.4124 + gL * 0.3576 + bL * 0.1805
        val y = rL * 0.2126 + gL * 0.7152 + bL * 0.0722
        val z = rL * 0.0193 + gL * 0.1192 + bL * 0.9505

        // 2. Convert XYZ to CIE L*a*b*
        var xN = x / 95.047
        var yN = y / 100.000
        var zN = z / 108.883

        val epsilon = 0.008856
        val kappa = 7.787

        xN = if (xN > epsilon) xN.pow(1.0 / 3.0) else (kappa * xN) + (16.0 / 116.0)
        yN = if (yN > epsilon) yN.pow(1.0 / 3.0) else (kappa * yN) + (16.0 / 116.0)
        zN = if (zN > epsilon) zN.pow(1.0 / 3.0) else (kappa * zN) + (16.0 / 116.0)

        val l = (116.0 * yN) - 16.0
        val a = 500.0 * (xN - yN)
        val b = 200.0 * (yN - zN)
        return Triple(l, a, b)
    }

    // Calculates the Euclidean distance between two CIE L*a*b* colors
    fun labDistance(lab1: Triple<Double, Double, Double>, lab2: Triple<Double, Double, Double>): Double {
        val dL = lab1.first - lab2.first
        val dA = lab1.second - lab2.second
        val dB = lab1.third - lab2.third
        return sqrt(dL * dL + dA * dA + dB * dB)
    }

    // Process a face image and extract raw average RGB values for all 9 cells (3x3 grid)
    fun processFaceImageRaw(
        filePath: String,
        face: FaceName,
        scale: Float = 0.55f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ): Array<Array<IntVector3>>? {
        val grid = loadImagePixels(filePath) ?: return null
        
        val w = grid.width
        val h = grid.height
        
        val cropW = (w * scale).toInt().coerceIn(3, w)
        val cropH = (h * scale).toInt().coerceIn(3, h)
        
        val left = ((w - cropW) / 2f + offsetX * w).toInt().coerceIn(0, w - cropW)
        val top = ((h - cropH) / 2f + offsetY * h).toInt().coerceIn(0, h - cropH)
        
        val cellW = cropW / 3f
        val cellH = cropH / 3f
        
        val result = Array(3) { Array(3) { IntVector3(128, 128, 128) } }
        
        // Dynamically calculate patch size based on cell dimensions (approx. 30% of the cell size)
        val patchW = (cellW * 0.15f).toInt().coerceAtLeast(3)
        val patchH = (cellH * 0.15f).toInt().coerceAtLeast(3)
        
        for (r in 0..2) {
            for (c in 0..2) {
                // Calculate center of the cell
                val cx = (left + (c + 0.5f) * cellW).toInt()
                val cy = (top + (r + 0.5f) * cellH).toInt()
                
                // Sample center patch to avoid black borders between facelets
                var sumR = 0L
                var sumG = 0L
                var sumB = 0L
                var count = 0
                
                for (px in cx - patchW..cx + patchW) {
                    for (py in cy - patchH..cy + patchH) {
                        if (px in 0 until w && py in 0 until h) {
                            val rgb = grid.getRGB(px, py)
                            val rVal = rgb.x
                            val gVal = rgb.y
                            val bVal = rgb.z
                            
                            val maxRGB = max(rVal, max(gVal, bVal))
                            val minRGB = min(rVal, min(gVal, bVal))
                            
                            val isBlackBorder = rVal < 45 && gVal < 45 && bVal < 45
                            val isSpecularGlare = maxRGB > 250 && (maxRGB - minRGB) < 15
                            
                            if (!isBlackBorder && !isSpecularGlare) {
                                sumR += rVal
                                sumG += gVal
                                sumB += bVal
                                count++
                            }
                        }
                    }
                }
                
                // Fallback to simple average if all pixels were filtered out
                if (count == 0) {
                    for (px in cx - patchW..cx + patchW) {
                        for (py in cy - patchH..cy + patchH) {
                            if (px in 0 until w && py in 0 until h) {
                                val rgb = grid.getRGB(px, py)
                                sumR += rgb.x
                                sumG += rgb.y
                                sumB += rgb.z
                                count++
                            }
                        }
                    }
                }
                
                val avgR = if (count > 0) (sumR / count).toInt() else 128
                val avgG = if (count > 0) (sumG / count).toInt() else 128
                val avgB = if (count > 0) (sumB / count).toInt() else 128
                
                result[r][c] = IntVector3(avgR, avgG, avgB)
            }
        }
        
        return result
    }

    // Classifies all scanned face grids using self-calibrating centers
    fun classifyAll(
        rawGrids: Map<FaceName, Array<Array<IntVector3>>>
    ): Map<FaceName, Array<Array<CubeColor>>> {
        
        // Define default references for missing scans (using standard clean color values)
        val defaultReferences = mapOf(
            CubeColor.ORANGE to IntVector3(255, 130, 0),
            CubeColor.RED to IntVector3(220, 20, 20),
            CubeColor.YELLOW to IntVector3(240, 240, 0),
            CubeColor.WHITE to IntVector3(230, 230, 230),
            CubeColor.GREEN to IntVector3(0, 160, 0),
            CubeColor.BLUE to IntVector3(0, 0, 200)
        )

        // Map centers to their expected face names
        val centerMapping = mapOf(
            CubeColor.ORANGE to FaceName.U,
            CubeColor.RED to FaceName.D,
            CubeColor.YELLOW to FaceName.L,
            CubeColor.WHITE to FaceName.R,
            CubeColor.GREEN to FaceName.F,
            CubeColor.BLUE to FaceName.B
        )

        // Gather real centers or fallback to default
        val referencesLab = mutableMapOf<CubeColor, Triple<Double, Double, Double>>()
        for (color in CubeColor.values()) {
            if (color == CubeColor.INTERNAL) continue
            
            val targetFace = centerMapping[color]!!
            val rawFaceGrid = rawGrids[targetFace]
            
            // Get center cell (1, 1) if scanned, otherwise fallback
            val refRGB = if (rawFaceGrid != null) {
                rawFaceGrid[1][1]
            } else {
                defaultReferences[color]!!
            }
            referencesLab[color] = rgbToLab(refRGB.x, refRGB.y, refRGB.z)
        }

        // Lock centers for each face name
        val lockedCenters = mapOf(
            FaceName.U to CubeColor.ORANGE,
            FaceName.D to CubeColor.RED,
            FaceName.L to CubeColor.YELLOW,
            FaceName.R to CubeColor.WHITE,
            FaceName.F to CubeColor.GREEN,
            FaceName.B to CubeColor.BLUE
        )

        // Classify all cells
        val resultGrids = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
        for (face in FaceName.values()) {
            val rawFaceGrid = rawGrids[face] ?: continue
            val classifiedGrid = Array(3) { Array(3) { CubeColor.INTERNAL } }
            
            for (r in 0..2) {
                for (c in 0..2) {
                    if (r == 1 && c == 1) {
                        // Enforce center locking
                        classifiedGrid[r][c] = lockedCenters[face]!!
                    } else {
                        val cellRGB = rawFaceGrid[r][c]
                        val cellLab = rgbToLab(cellRGB.x, cellRGB.y, cellRGB.z)
                        
                        var closestColor = CubeColor.WHITE
                        var minDistance = Double.MAX_VALUE
                        
                        for ((refColor, refLab) in referencesLab) {
                            val dist = labDistance(cellLab, refLab)
                            if (dist < minDistance) {
                                minDistance = dist
                                closestColor = refColor
                            }
                        }
                        classifiedGrid[r][c] = closestColor
                    }
                }
            }
            resultGrids[face] = classifiedGrid
        }

        return resultGrids
    }
}
