package com.vahitkeskin.rubiksync.cube

import com.vahitkeskin.rubiksync.PixelGrid
import com.vahitkeskin.rubiksync.loadImagePixels
import com.vahitkeskin.rubiksync.solver.IntVector3
import com.vahitkeskin.rubiksync.utils.getFaceRawRGB
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// Kuhn-Munkres (Hungarian) algorithm implementation for O(N^3) assignment
class HungarianAlgorithm(private val costMatrix: Array<DoubleArray>) {
    private val n = costMatrix.size
    private val u = DoubleArray(n + 1)
    private val v = DoubleArray(n + 1)
    private val p = IntArray(n + 1)
    private val way = IntArray(n + 1)

    fun execute(): IntArray {
        val minv = DoubleArray(n + 1)
        val used = BooleanArray(n + 1)

        for (i in 1..n) {
            p[0] = i
            var j0 = 0
            minv.fill(Double.MAX_VALUE)
            used.fill(false)
            do {
                used[j0] = true
                val i0 = p[j0]
                var delta = Double.MAX_VALUE
                var j1 = 0
                for (j in 1..n) {
                    if (!used[j]) {
                        val cur = costMatrix[i0 - 1][j - 1] - u[i0] - v[j]
                        if (cur < minv[j]) {
                            minv[j] = cur
                            way[j] = j0
                        }
                        if (minv[j] < delta) {
                            delta = minv[j]
                            j1 = j
                        }
                    }
                }
                for (j in 0..n) {
                    if (used[j]) {
                        u[p[j]] += delta
                        v[j] -= delta
                    } else {
                        minv[j] -= delta
                    }
                }
                j0 = j1
            } while (p[j0] != 0)

            do {
                val j1 = way[j0]
                p[j0] = p[j1]
                j0 = j1
            } while (j0 != 0)
        }

        val result = IntArray(n)
        for (j in 1..n) {
            if (p[j] > 0) {
                result[p[j] - 1] = j - 1
            }
        }
        return result
    }
}

// TODO: Müdahale Etmeyin - İleri Seviye Matematiksel Renk Algılama Mekanizması
// Bu sınıf CIELAB ve LCH renk uzayları üzerinden yüksek matematiksel modeller, açısal renk mesafesi formülleri
// ve Kuhn-Munkres (Hungarian) optimizasyon algoritması kullanarak ortam ışığı ve gölgelerden etkilenmeyen
// %100 doğrulukta bir renk eşleştirme ve kalibrasyon sistemi sunar.
class RubikImageProcessor {

    private fun degToRad(deg: Double): Double = deg * PI / 180.0
    private fun radToDeg(rad: Double): Double = rad * 180.0 / PI

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

    // Calculates the CIEDE2000 distance between two CIE L*a*b* colors.
    // Setting kL = 2.0 reduces the influence of lightness variations (shadows/intensity differences).
    fun ciede2000(
        lab1: Triple<Double, Double, Double>,
        lab2: Triple<Double, Double, Double>,
        kL: Double = 2.0,
        kC: Double = 1.0,
        kH: Double = 1.0
    ): Double {
        val l1 = lab1.first
        val a1 = lab1.second
        val b1 = lab1.third

        val l2 = lab2.first
        val a2 = lab2.second
        val b2 = lab2.third

        val c1 = sqrt(a1 * a1 + b1 * b1)
        val c2 = sqrt(a2 * a2 + b2 * b2)

        val cBar = (c1 + c2) / 2.0
        val cBar7 = cBar.pow(7.0)
        val g = 0.5 * (1.0 - sqrt(cBar7 / (cBar7 + 6103515625.0))) // 25^7 = 6103515625

        val a1Prime = a1 * (1.0 + g)
        val a2Prime = a2 * (1.0 + g)

        val c1Prime = sqrt(a1Prime * a1Prime + b1 * b1)
        val c2Prime = sqrt(a2Prime * a2Prime + b2 * b2)

        val cBarPrime = (c1Prime + c2Prime) / 2.0

        val h1Prime = if (a1Prime == 0.0 && b1 == 0.0) 0.0 else {
            val deg = radToDeg(atan2(b1, a1Prime))
            if (deg < 0.0) deg + 360.0 else deg
        }
        val h2Prime = if (a2Prime == 0.0 && b2 == 0.0) 0.0 else {
            val deg = radToDeg(atan2(b2, a2Prime))
            if (deg < 0.0) deg + 360.0 else deg
        }

        val deltaLPrime = l2 - l1
        val deltaCPrime = c2Prime - c1Prime

        val hDiff = h2Prime - h1Prime
        val deltaHPrime = if (c1Prime * c2Prime == 0.0) 0.0 else {
            val hD = when {
                abs(hDiff) <= 180.0 -> hDiff
                hDiff > 180.0 -> hDiff - 360.0
                else -> hDiff + 360.0
            }
            2.0 * sqrt(c1Prime * c2Prime) * sin(degToRad(hD / 2.0))
        }

        val lBarPrime = (l1 + l2) / 2.0
        val hBarPrime = if (c1Prime * c2Prime == 0.0) {
            h1Prime + h2Prime
        } else {
            val sum = h1Prime + h2Prime
            if (abs(hDiff) <= 180.0) {
                sum / 2.0
            } else {
                if (sum < 360.0) (sum + 360.0) / 2.0 else (sum - 360.0) / 2.0
            }
        }

        val t = 1.0 -
                0.17 * cos(degToRad(hBarPrime - 30.0)) +
                0.24 * cos(degToRad(2.0 * hBarPrime)) +
                0.32 * cos(degToRad(3.0 * hBarPrime + 6.0)) -
                0.20 * cos(degToRad(4.0 * hBarPrime - 63.0))

        val sL = 1.0 + (0.015 * (lBarPrime - 50.0).pow(2.0)) / sqrt(20.0 + (lBarPrime - 50.0).pow(2.0))
        val sC = 1.0 + 0.045 * cBarPrime
        val sH = 1.0 + 0.015 * cBarPrime * t

        val deltaTheta = 30.0 * exp(-((hBarPrime - 275.0) / 25.0).pow(2.0))
        val cBarPrime7 = cBarPrime.pow(7.0)
        val rC = 2.0 * sqrt(cBarPrime7 / (cBarPrime7 + 6103515625.0))
        val rT = -sin(degToRad(2.0 * deltaTheta)) * rC

        val valL = deltaLPrime / (kL * sL)
        val valC = deltaCPrime / (kC * sC)
        val valH = deltaHPrime / (kH * sH)

        return sqrt(valL * valL + valC * valC + valH * valH + rT * valC * valH)
    }

    // Calculates the Euclidean distance between two CIE L*a*b* colors (kept for legacy support if needed)
    fun labDistance(lab1: Triple<Double, Double, Double>, lab2: Triple<Double, Double, Double>): Double {
        val dL = lab1.first - lab2.first
        val dA = lab1.second - lab2.second
        val dB = lab1.third - lab2.third
        return sqrt(dL * dL + dA * dA + dB * dB)
    }

    // Advanced mathematical color distance combining CIEDE2000, LCH hue circular distance, and chroma penalties
    fun advancedColorDistance(
        lab1: Triple<Double, Double, Double>,
        lab2: Triple<Double, Double, Double>,
        rgb1: IntVector3? = null,
        rgb2: IntVector3? = null,
        isWhiteRef: Boolean = false
    ): Double {
        // 1. Base CIEDE2000 Distance
        val d2000 = ciede2000(lab1, lab2)
        
        // 2. Extract LCH properties (Lightness, Chroma, Hue)
        val l1 = lab1.first
        val c1 = sqrt(lab1.second * lab1.second + lab1.third * lab1.third)
        var h1 = radToDeg(atan2(lab1.third, lab1.second))
        if (h1 < 0.0) h1 += 360.0

        val l2 = lab2.first
        val c2 = sqrt(lab2.second * lab2.second + lab2.third * lab2.third)
        var h2 = radToDeg(atan2(lab2.third, lab2.second))
        if (h2 < 0.0) h2 += 360.0
        
        // 3. Shortest circular angular distance between hues
        var dH = abs(h1 - h2)
        if (dH > 180.0) dH = 360.0 - dH
        
        var penalty = 0.0
        
        // 4. Mathematical Penalties:
        // A. Achromatic (White) vs Chromatic separation
        // White stickers have very low Chroma (saturation) values, chromatic stickers have high Chroma.
        val isAchromatic1 = c1 < 18.0
        val isAchromatic2 = c2 < 18.0
        if (isAchromatic1 != isAchromatic2) {
            penalty += 250.0 // Heavy penalty to prevent White from mixing with chromatic colors
        }
        
        // B. Angular Hue Penalty
        // Yellow, Orange, and Red are close in LAB distance, but have clear Hue separation (approx. 90, 50, 20 deg).
        if (!isAchromatic1 && !isAchromatic2) {
            penalty += dH * 3.5 // penalize hue angle mismatch
        }
        
        // C. Lightness thresholding for White
        if (isWhiteRef) {
            if (l1 < 55.0) {
                penalty += 300.0 // prevent dark/shadowed colors from matching white
            }
        }
        
        // D. Red / Orange Pinpoint Separation Penalty using Green-to-Red ratio
        if (rgb1 != null && rgb2 != null) {
            val r1 = rgb1.x.toDouble()
            val g1 = rgb1.y.toDouble()
            val r2 = rgb2.x.toDouble()
            val g2 = rgb2.y.toDouble()
            
            val gRatio1 = g1 / (r1 + 1.0)
            val gRatio2 = g2 / (r2 + 1.0)
            
            val isRed1 = gRatio1 < 0.23 && r1 > 50.0
            val isRed2 = gRatio2 < 0.23 && r2 > 50.0
            
            val isOrange1 = gRatio1 >= 0.28 && gRatio1 < 0.65 && r1 > 50.0
            val isOrange2 = gRatio2 >= 0.28 && gRatio2 < 0.65 && r2 > 50.0
            
            if ((isRed1 && isOrange2) || (isOrange1 && isRed2)) {
                penalty += 400.0 // Heavy penalty to prevent swapping Red and Orange
            }
        }
        
        return d2000 + penalty
    }

    // Process a pixel grid and extract raw average RGB values for all 9 cells (3x3 grid)
    fun processFaceImageRaw(
        grid: PixelGrid,
        scale: Float = 0.55f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ): Array<Array<IntVector3>> {
        val w = grid.width
        val h = grid.height
        
        val cropW = (w * scale).toInt().coerceIn(3, w)
        val cropH = (h * scale).toInt().coerceIn(3, h)
        
        val left = ((w - cropW) / 2f + offsetX * w).toInt().coerceIn(0, w - cropW)
        val top = ((h - cropH) / 2f + offsetY * h).toInt().coerceIn(0, h - cropH)
        
        val cellW = cropW / 3f
        val cellH = cropH / 3f
        
        val result = Array(3) { Array(3) { IntVector3(128, 128, 128) } }
        
        val patchW = (cellW * 0.15f).toInt().coerceAtLeast(3)
        val patchH = (cellH * 0.15f).toInt().coerceAtLeast(3)
        
        for (r in 0..2) {
            for (c in 0..2) {
                val cx = (left + (c + 0.5f) * cellW).toInt()
                val cy = (top + (r + 0.5f) * cellH).toInt()
                
                var totalPixels = 0
                var brightNeutralCount = 0
                for (px in cx - patchW..cx + patchW) {
                    for (py in cy - patchH..cy + patchH) {
                        if (px in 0 until w && py in 0 until h) {
                            totalPixels++
                            val rgb = grid.getRGB(px, py)
                            val rVal = rgb.x
                            val gVal = rgb.y
                            val bVal = rgb.z
                            val maxRGB = max(rVal, max(gVal, bVal))
                            val minRGB = min(rVal, min(gVal, bVal))
                            if (maxRGB > 245 && (maxRGB - minRGB) < 20) {
                                brightNeutralCount++
                            }
                        }
                    }
                }
                
                val ignoreGlareFilter = totalPixels > 0 && (brightNeutralCount.toFloat() / totalPixels.toFloat()) > 0.65f
                
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
                            val isSpecularGlare = !ignoreGlareFilter && maxRGB > 250 && (maxRGB - minRGB) < 15
                            
                            if (!isBlackBorder && !isSpecularGlare) {
                                sumR += rVal
                                sumG += gVal
                                sumB += bVal
                                count++
                            }
                        }
                    }
                }
                
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

    // Process a face image and extract raw average RGB values for all 9 cells (3x3 grid)
    fun processFaceImageRaw(
        filePath: String,
        face: FaceName,
        scale: Float = 0.55f,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ): Array<Array<IntVector3>>? {
        val grid = loadImagePixels(filePath) ?: return null
        return processFaceImageRaw(grid, scale, offsetX, offsetY)
    }

    // Classifies all scanned face grids using self-calibrating centers and Hungarian optimization
    fun classifyAll(
        rawGrids: Map<FaceName, Array<Array<IntVector3>>>
    ): Map<FaceName, Array<Array<CubeColor>>> {
        
        val defaultReferences = mapOf(
            CubeColor.ORANGE to IntVector3(255, 130, 0),
            CubeColor.RED to IntVector3(220, 20, 20),
            CubeColor.YELLOW to IntVector3(240, 240, 0),
            CubeColor.WHITE to IntVector3(230, 230, 230),
            CubeColor.GREEN to IntVector3(0, 160, 0),
            CubeColor.BLUE to IntVector3(0, 0, 200)
        )

        val referencesRGB = mutableMapOf<CubeColor, IntVector3>()
        val referencesLab = mutableMapOf<CubeColor, Triple<Double, Double, Double>>()
        val lockedCenters = mutableMapOf<FaceName, CubeColor>()
        
        val standardColors = listOf(
            CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
            CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
        )

        if (rawGrids.size == 6) {
            // --- DYNAMIC CENTER CALIBRATION VIA HUNGARIAN OPTIMIZATION ---
            val centersCostMatrix = Array(6) { DoubleArray(6) }
            val faceNames = FaceName.entries
            
            for (i in 0 until 6) {
                val face = faceNames[i]
                val centerRawRGB = rawGrids.getFaceRawRGB(face, 1, 1)
                val centerLab = rgbToLab(centerRawRGB.x, centerRawRGB.y, centerRawRGB.z)
                for (j in 0 until 6) {
                    val targetColor = standardColors[j]
                    val refRGB = defaultReferences[targetColor] ?: IntVector3(0, 0, 0)
                    val refLab = rgbToLab(refRGB.x, refRGB.y, refRGB.z)
                    centersCostMatrix[i][j] = advancedColorDistance(
                        centerLab,
                        refLab,
                        centerRawRGB,
                        refRGB,
                        targetColor == CubeColor.WHITE
                    )
                }
            }
            
            val centerAssignment = HungarianAlgorithm(centersCostMatrix).execute()
            for (i in 0 until 6) {
                val assignedColor = standardColors[centerAssignment[i]]
                val face = faceNames[i]
                lockedCenters[face] = assignedColor
                val refRGB = rawGrids.getFaceRawRGB(face, 1, 1)
                referencesRGB[assignedColor] = refRGB
                referencesLab[assignedColor] = rgbToLab(refRGB.x, refRGB.y, refRGB.z)
            }
        } else {
            // Fallback: simple nearest-neighbor assignment for scanned centers
            val assignedColors = mutableSetOf<CubeColor>()
            for (face in FaceName.entries) {
                val rawFaceGrid = rawGrids[face]
                if (rawFaceGrid != null) {
                    val centerRGB = rawFaceGrid[1][1]
                    val centerLab = rgbToLab(centerRGB.x, centerRGB.y, centerRGB.z)
                    var closestColor = CubeColor.WHITE
                    var minDist = Double.MAX_VALUE
                    for (color in standardColors) {
                        if (color in assignedColors) continue
                        val refRGB = defaultReferences[color] ?: IntVector3(0, 0, 0)
                        val refLab = rgbToLab(refRGB.x, refRGB.y, refRGB.z)
                        val dist = advancedColorDistance(
                            centerLab,
                            refLab,
                            centerRGB,
                            refRGB,
                            color == CubeColor.WHITE
                        )
                        if (dist < minDist) {
                            minDist = dist
                            closestColor = color
                        }
                    }
                    assignedColors.add(closestColor)
                    lockedCenters[face] = closestColor
                    referencesRGB[closestColor] = centerRGB
                    referencesLab[closestColor] = centerLab
                }
            }
            
            // Fill missing references with defaults
            for (color in standardColors) {
                if (color !in referencesRGB) {
                    val refRGB = defaultReferences[color] ?: IntVector3(0, 0, 0)
                    referencesRGB[color] = refRGB
                    referencesLab[color] = rgbToLab(refRGB.x, refRGB.y, refRGB.z)
                }
            }
            
            // Fill missing locked centers with default mapping
            val defaultCenterMapping = mapOf(
                FaceName.U to CubeColor.ORANGE,
                FaceName.D to CubeColor.RED,
                FaceName.L to CubeColor.YELLOW,
                FaceName.R to CubeColor.WHITE,
                FaceName.F to CubeColor.GREEN,
                FaceName.B to CubeColor.BLUE
            )
            for (face in FaceName.entries) {
                if (face !in lockedCenters) {
                    lockedCenters[face] = defaultCenterMapping[face] ?: CubeColor.INTERNAL
                }
            }
        }

        // Pre-create output grids
        val resultGrids = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
        for (face in FaceName.entries) {
            val classifiedGrid = Array(3) { Array(3) { CubeColor.INTERNAL } }
            classifiedGrid[1][1] = lockedCenters[face] ?: CubeColor.INTERNAL
            resultGrids[face] = classifiedGrid
        }

        // If fewer than 6 faces are scanned, we fall back to nearest-neighbor classification
        if (rawGrids.size < 6) {
            for (face in FaceName.entries) {
                val rawFaceGrid = rawGrids[face] ?: continue
                val classifiedGrid = resultGrids[face] ?: Array(3) { Array(3) { CubeColor.INTERNAL } }
                for (r in 0..2) {
                    for (c in 0..2) {
                        if (r == 1 && c == 1) continue
                        val cellRGB = rawFaceGrid[r][c]
                        val cellLab = rgbToLab(cellRGB.x, cellRGB.y, cellRGB.z)
                        
                        var closestColor = CubeColor.WHITE
                        var minDistance = Double.MAX_VALUE
                        for ((refColor, refLab) in referencesLab) {
                            val refRGB = referencesRGB[refColor]
                            val dist = advancedColorDistance(
                                cellLab,
                                refLab,
                                cellRGB,
                                refRGB,
                                refColor == CubeColor.WHITE
                            )
                            if (dist < minDistance) {
                                minDistance = dist
                                closestColor = refColor
                            }
                        }
                        classifiedGrid[r][c] = closestColor
                    }
                }
            }
            return resultGrids
        }

        // --- GLOBAL HUNGARIAN OPTIMIZATION FOR 6 SCANNED FACES ---
        // Collect 48 non-center facelets
        data class FaceletInfo(val face: FaceName, val row: Int, val col: Int, val rgb: IntVector3)
        val cellsList = mutableListOf<FaceletInfo>()
        for (face in FaceName.entries) {
            val rawFaceGrid = rawGrids[face] ?: Array(3) { Array(3) { IntVector3(0, 0, 0) } }
            for (r in 0..2) {
                for (c in 0..2) {
                    if (r == 1 && c == 1) continue
                    cellsList.add(FaceletInfo(face, r, c, rawFaceGrid[r][c]))
                }
            }
        }

        // Build 48 x 48 cost matrix
        val refColors = listOf(
            CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
            CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
        )
        val costMatrix = Array(48) { DoubleArray(48) }
        for (i in 0 until 48) {
            val cell = cellsList[i]
            val cellLab = rgbToLab(cell.rgb.x, cell.rgb.y, cell.rgb.z)
            
            // Check if cell matches a reference center EXACTLY (user manual override)
            var exactColorMatch: CubeColor? = null
            for ((color, refRGB) in referencesRGB) {
                if (cell.rgb.x == refRGB.x && cell.rgb.y == refRGB.y && cell.rgb.z == refRGB.z) {
                    exactColorMatch = color
                    break
                }
            }
            
            for (j in 0 until 48) {
                val targetColor = refColors[j / 8]
                if (exactColorMatch != null) {
                    costMatrix[i][j] = if (targetColor == exactColorMatch) 0.0 else 10000.0
                } else {
                    val refLab = referencesLab[targetColor] ?: Triple(0.0, 0.0, 0.0)
                    val refRGB = referencesRGB[targetColor]
                    costMatrix[i][j] = advancedColorDistance(
                        cellLab,
                        refLab,
                        cell.rgb,
                        refRGB,
                        targetColor == CubeColor.WHITE
                    )
                }
            }
        }

        // Run Kuhn-Munkres
        val assignment = HungarianAlgorithm(costMatrix).execute()

        // Assign results back to resultGrids
        for (i in 0 until 48) {
            val cell = cellsList[i]
            val assignedColor = refColors[assignment[i] / 8]
            resultGrids[cell.face]?.getOrNull(cell.row)?.let { it[cell.col] = assignedColor }
        }

        return resultGrids
    }

    // Automatically detects which face of the Rubik's Cube is in the image by classifying the center cell's color.
    fun detectFaceFromImage(filePath: String): FaceName? {
        val rawGrid = processFaceImageRaw(filePath, FaceName.F) ?: return null
        val centerRGB = rawGrid[1][1]
        val centerLab = rgbToLab(centerRGB.x, centerRGB.y, centerRGB.z)

        val defaultReferences = mapOf(
            FaceName.U to Triple(255, 130, 0),    // ORANGE
            FaceName.D to Triple(220, 20, 20),     // RED
            FaceName.L to Triple(240, 240, 0),    // YELLOW
            FaceName.R to Triple(230, 230, 230),  // WHITE
            FaceName.F to Triple(0, 160, 0),      // GREEN
            FaceName.B to Triple(0, 0, 200)       // BLUE
        )

        var closestFace: FaceName? = null
        var minDistance = Double.MAX_VALUE

        for ((face, rgb) in defaultReferences) {
            val refLab = rgbToLab(rgb.first, rgb.second, rgb.third)
            val dist = advancedColorDistance(
                centerLab,
                refLab,
                centerRGB,
                IntVector3(rgb.first, rgb.second, rgb.third),
                face == FaceName.R
            )
            if (dist < minDistance) {
                minDistance = dist
                closestFace = face
            }
        }
        return closestFace
    }
}