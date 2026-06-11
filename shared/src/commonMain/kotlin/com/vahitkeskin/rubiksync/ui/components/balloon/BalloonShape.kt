package com.vahitkeskin.rubiksync.ui.components.balloon

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class BalloonShape(
    private val arrowWidth: Float,
    private val arrowHeight: Float,
    private val arrowX: Float,
    private val isBelow: Boolean,
    private val cornerRadius: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val w = size.width
        val h = size.height
        val cr = cornerRadius

        if (isBelow) {
            val top = arrowHeight
            val bottom = h

            path.moveTo(cr, top)
            path.lineTo(arrowX - arrowWidth / 2f, top)
            path.lineTo(arrowX, 0f)
            path.lineTo(arrowX + arrowWidth / 2f, top)

            path.lineTo(w - cr, top)
            path.arcTo(Rect(w - cr * 2, top, w, top + cr * 2), -90f, 90f, false)

            path.lineTo(w, bottom - cr)
            path.arcTo(Rect(w - cr * 2, bottom - cr * 2, w, bottom), 0f, 90f, false)

            path.lineTo(cr, bottom)
            path.arcTo(Rect(0f, bottom - cr * 2, cr * 2, bottom), 90f, 90f, false)

            path.lineTo(0f, top + cr)
            path.arcTo(Rect(0f, top, cr * 2, top + cr * 2), 180f, 90f, false)

            path.close()
        } else {
            val top = 0f
            val bottom = h - arrowHeight

            path.moveTo(cr, top)
            path.lineTo(w - cr, top)
            path.arcTo(Rect(w - cr * 2, top, w, top + cr * 2), -90f, 90f, false)

            path.lineTo(w, bottom - cr)
            path.arcTo(Rect(w - cr * 2, bottom - cr * 2, w, bottom), 0f, 90f, false)

            path.lineTo(arrowX + arrowWidth / 2f, bottom)
            path.lineTo(arrowX, h)
            path.lineTo(arrowX - arrowWidth / 2f, bottom)

            path.lineTo(cr, bottom)
            path.arcTo(Rect(0f, bottom - cr * 2, cr * 2, bottom), 90f, 90f, false)

            path.lineTo(0f, top + cr)
            path.arcTo(Rect(0f, top, cr * 2, top + cr * 2), 180f, 90f, false)

            path.close()
        }

        return Outline.Generic(path)
    }
}