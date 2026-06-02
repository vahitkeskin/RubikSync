package com.vahitkeskin.rubiksync.utils

import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName

/**
 * Utility to parse native/script color layouts from JSON string professionally.
 * Supports compact, formatted, case-insensitive, and spacing-tolerant JSON representations.
 *
 * Example input:
 * {
 *   "U": "OOOOOOOOO",
 *   "D": "RRRRRRRRR",
 *   "L": "YYYYYYYYY",
 *   "R": "WWWWWWWWW",
 *   "F": "GGGGGGGGG",
 *   "B": "BBBBBBBBB"
 * }
 */
fun parseDetectedState(jsonStr: String): Map<FaceName, Array<Array<CubeColor>>>? {
    try {
        // Clean JSON formatting by removing curly braces, double quotes, single quotes, and all whitespace characters (spaces, newlines, tabs)
        val cleaned = jsonStr.replace(Regex("[\\s'\"{}]+"), "")
        if (cleaned.isEmpty()) return null

        val entries = cleaned.split(",")
        val faces = mutableMapOf<FaceName, Array<Array<CubeColor>>>()

        val faceMap = mapOf(
            "U" to FaceName.U, "D" to FaceName.D, "L" to FaceName.L,
            "R" to FaceName.R, "F" to FaceName.F, "B" to FaceName.B
        )

        val colorMap = mapOf(
            'O' to CubeColor.ORANGE, 'R' to CubeColor.RED, 'Y' to CubeColor.YELLOW,
            'W' to CubeColor.WHITE, 'G' to CubeColor.GREEN, 'B' to CubeColor.BLUE
        )

        for (entry in entries) {
            if (entry.isEmpty()) continue
            val parts = entry.split(":")
            if (parts.size != 2) continue
            val key = parts[0].uppercase()
            val value = parts[1].uppercase()

            val face = faceMap[key] ?: continue
            
            // Each face must contain exactly 9 color characters
            if (value.length != 9) return null

            val grid = Array(3) { Array(3) { CubeColor.INTERNAL } }
            for (i in 0 until 9) {
                val r = i / 3
                val c = i % 3
                val char = value[i]
                val color = colorMap[char] ?: return null // Return null on invalid color code
                grid[r][c] = color
            }
            faces[face] = grid
        }

        // Professional validation: a Rubik's Cube state must define all 6 faces
        if (faces.size != 6) {
            return null
        }

        return faces
    } catch (e: Exception) {
        return null
    }
}
