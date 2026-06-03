package com.vahitkeskin.rubiksync.cube

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
    
    val matrixStr = when {
        move.axis.x > 0.5f -> {
            val s = move.angleSign.toInt()
            """
            |  1     0     0  |
            |  0     0    ${if (s > 0) "-1" else " 1"}  |
            |  0    ${if (s > 0) " 1" else "-1"}     0  |
            """.trimIndent()
        }
        move.axis.y > 0.5f -> {
            val s = move.angleSign.toInt()
            """
            |  0     0    ${if (s > 0) " 1" else "-1"}  |
            |  0     1     0  |
            | ${if (s > 0) "-1" else " 1"}     0     0  |
            """.trimIndent()
        }
        move.axis.z > 0.5f -> {
            val s = move.angleSign.toInt()
            """
            |  0    ${if (s > 0) "-1" else " 1"}     0  |
            | ${if (s > 0) " 1" else "-1"}     0     0  |
            |  0     0     1  |
            """.trimIndent()
        }
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
