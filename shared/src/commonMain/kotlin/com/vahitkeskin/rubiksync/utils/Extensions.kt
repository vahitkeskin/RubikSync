package com.vahitkeskin.rubiksync.utils

import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.solver.IntVector3

/**
 * Extension to return a default value if the receiver is null.
 */
fun <T> T?.orDefault(default: T): T = this ?: default

/**
 * Boolean? extension to return false if null.
 */
fun Boolean?.orFalse(): Boolean = this ?: false

/**
 * Boolean? extension to return true if null.
 */
fun Boolean?.orTrue(): Boolean = this ?: true

/**
 * Int? extension to return 0 if null.
 */
fun Int?.orZero(): Int = this ?: 0

/**
 * Long? extension to return 0L if null.
 */
fun Long?.orZero(): Long = this ?: 0L

/**
 * Double? extension to return 0.0 if null.
 */
fun Double?.orZero(): Double = this ?: 0.0

/**
 * Float? extension to return 0.0f if null.
 */
fun Float?.orZero(): Float = this ?: 0.0f

/**
 * Safe 2D array color retrieval for FaceName mapping.
 */
fun Map<FaceName, Array<Array<CubeColor>>>.getFaceColor(face: FaceName, row: Int, col: Int): CubeColor {
    return this[face]?.getOrNull(row)?.getOrNull(col) ?: CubeColor.INTERNAL
}

/**
 * Safe 2D array IntVector3 retrieval for FaceName mapping.
 */
fun Map<FaceName, Array<Array<IntVector3>>>.getFaceRawRGB(face: FaceName, row: Int, col: Int): IntVector3 {
    return this[face]?.getOrNull(row)?.getOrNull(col) ?: IntVector3(0, 0, 0)
}

/**
 * Encodes a string into base64.
 */
fun base64Encode(text: String): String {
    val bytes = text.encodeToByteArray()
    val table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    val result = StringBuilder()
    var i = 0
    while (i < bytes.size) {
        val b0 = bytes[i].toInt() and 0xFF
        val b1 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else -1
        val b2 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else -1
        
        val pad1 = b1 == -1
        val pad2 = b2 == -1
        
        val chunk = (b0 shl 16) or 
                    ((if (b1 != -1) b1 else 0) shl 8) or 
                    (if (b2 != -1) b2 else 0)
        
        val c0 = (chunk shr 18) and 63
        val c1 = (chunk shr 12) and 63
        val c2 = (chunk shr 6) and 63
        val c3 = chunk and 63
        
        result.append(table[c0])
        result.append(table[c1])
        result.append(if (pad1) '=' else table[c2])
        result.append(if (pad2) '=' else table[c3])
        
        i += 3
    }
    return result.toString()
}