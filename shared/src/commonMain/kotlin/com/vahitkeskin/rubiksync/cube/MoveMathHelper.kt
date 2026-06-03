package com.vahitkeskin.rubiksync.cube

private fun formatMatrix(
    m11: Int, m12: Int, m13: Int,
    m21: Int, m22: Int, m23: Int,
    m31: Int, m32: Int, m33: Int
): String {
    fun cell(v: Int): String = if (v < 0) " $v" else "  $v"
    
    val r1 = "│ ${cell(m11)} ${cell(m12)} ${cell(m13)} │"
    val r2 = "│ ${cell(m21)} ${cell(m22)} ${cell(m23)} │"
    val r3 = "│ ${cell(m31)} ${cell(m32)} ${cell(m33)} │"
    
    val top = "┌" + " ".repeat(13) + "┐"
    val bottom = "└" + " ".repeat(13) + "┘"
    
    return "$top\n$r1\n$r2\n$r3\n$bottom"
}

fun getMoveMathDetails(move: MoveType): String {
    val axisStr = when {
        move.axis.x > 0.5f -> "X = (1, 0, 0)"
        move.axis.y > 0.5f -> "Y = (0, 1, 0)"
        move.axis.z > 0.5f -> "Z = (0, 0, 1)"
        else -> "Bilinmeyen Eksen"
    }
    
    val layerAxis = when {
        move.axis.x > 0.5f -> "x"
        move.axis.y > 0.5f -> "y"
        move.axis.z > 0.5f -> "z"
        else -> "koordinatı"
    }
    
    val angleDeg = if (move.angleSign > 0) "90°" else "-90°"
    val angleRadSymbol = if (move.angleSign > 0) "π / 2" else "-π / 2"
    
    val s = move.angleSign.toInt()
    val matrixStr = when {
        move.axis.x > 0.5f -> formatMatrix(
            1, 0, 0,
            0, 0, -s,
            0, s, 0
        )
        move.axis.y > 0.5f -> formatMatrix(
            0, 0, s,
            0, 1, 0,
            -s, 0, 0
        )
        move.axis.z > 0.5f -> formatMatrix(
            0, -s, 0,
            s, 0, 0,
            0, 0, 1
        )
        else -> "N/A"
    }

    return """
        Eksen (u): $axisStr
        Katman (d): $layerAxis = ${move.layerValue}
        Açı (θ): $angleRadSymbol rad ($angleDeg)
        
        Rotasyon Koşulu (Layer Selector):
        p • u = d
        
        Rodrigues Rotasyon Formülü:
        v' = v•cos(θ) + (u × v)•sin(θ) + u•(u•v)•(1 - cos(θ))
        
        Rotasyon Matrisi (R):
        $matrixStr
        
        Dönüşüm Eşleşmeleri:
        p' = R • p
        r' = R • r
        u' = R • u
        f' = R • f
    """.trimIndent()
}

