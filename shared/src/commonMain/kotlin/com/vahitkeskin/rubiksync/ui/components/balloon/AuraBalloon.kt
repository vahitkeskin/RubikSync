package com.vahitkeskin.rubiksync.ui.components.balloon

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.ui.state.AccentOrange
import com.vahitkeskin.rubiksync.ui.state.PreviewRubikTheme
import com.vahitkeskin.rubiksync.ui.state.Slate800
import com.vahitkeskin.rubiksync.ui.state.Slate900

/**
 * Dinamik konumlandırmalı ve ekran kenarı duyarlı "Sivri uçlu" bilgi balonu.
 * Sivri uç her zaman info ikonunu gösterir, balon ise ekran sınırları içinde kalır.
 */
@Composable
fun AuraBalloon(
    text: String,
    isVisible: Boolean,
    isBelow: Boolean = false,
    onDismiss: () -> Unit
) {
    var arrowX by remember { mutableStateOf(0f) }
    var balloonWidth by remember { mutableStateOf(0f) }

    val pivotFractionX = if (balloonWidth > 0f) arrowX / balloonWidth else 0.5f
    val pivotFractionY = if (isBelow) 0f else 1f
    val transformOrigin = TransformOrigin(pivotFractionX, pivotFractionY)

    val isAnimatedVisible = isVisible && balloonWidth > 0f

    val animationProgress by animateFloatAsState(
        targetValue = if (isAnimatedVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        )
    )

    // Keep popup in composition if parent wants it visible OR if it's animating out
    val isPopupVisible = isVisible || animationProgress > 0f

    if (isPopupVisible) {
        val density = LocalDensity.current

        Popup(
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    // Ekran kenarından bırakılacak boşluk (item padding ile aynı)
                    val screenPadding = with(density) { 16.dp.toPx() }.toInt()

                    // Balonun yatay konumu: İkonun merkezine göre ortala ama ekran sınırlarında tut
                    val anchorCenter = anchorBounds.left + anchorBounds.width / 2
                    var x = anchorCenter - popupContentSize.width / 2
                    x = x.coerceIn(
                        screenPadding,
                        windowSize.width - popupContentSize.width - screenPadding
                    )

                    // Ok ucunun (pointer) balon üzerindeki yatay konumu - balon gövdesinden dışarı taşmayı önle
                    balloonWidth = popupContentSize.width.toFloat()
                    // 16.dp (cornerRadius) + 9.dp (half arrowWidth) = 25.dp
                    val minArrowX = with(density) { 25.dp.toPx() }
                    val maxArrowX =
                        if (balloonWidth > 0f) (balloonWidth - minArrowX).coerceAtLeast(minArrowX) else minArrowX
                    arrowX = (anchorCenter - x).toFloat().coerceIn(minArrowX, maxArrowX)

                    // Balonun dikey konumu: İkonun hemen üstünde veya altında
                    val y = if (isBelow) {
                        anchorBounds.bottom + with(density) { 4.dp.toPx() }.toInt()
                    } else {
                        anchorBounds.top - popupContentSize.height - with(density) { 4.dp.toPx() }.toInt()
                    }

                    return IntOffset(x, y)
                }
            },
            onDismissRequest = onDismiss,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            BalloonContent(
                text = text,
                onDismiss = onDismiss,
                arrowX = arrowX,
                isBelow = isBelow,
                animationProgress = animationProgress,
                transformOrigin = transformOrigin
            )
        }
    }
}

@Composable
private fun BalloonContent(
    text: String,
    onDismiss: () -> Unit,
    arrowX: Float,
    isBelow: Boolean,
    animationProgress: Float,
    transformOrigin: TransformOrigin
) {
    val arrowWidthPx = with(LocalDensity.current) { 18.dp.toPx() }
    val arrowHeightPx = with(LocalDensity.current) { 10.dp.toPx() }
    val cornerRadiusPx = with(LocalDensity.current) { 16.dp.toPx() }

    val balloonShape = remember(arrowX, isBelow) {
        BalloonShape(
            arrowWidth = arrowWidthPx,
            arrowHeight = arrowHeightPx,
            arrowX = arrowX,
            isBelow = isBelow,
            cornerRadius = cornerRadiusPx
        )
    }

    Box(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .graphicsLayer {
                alpha = animationProgress
                scaleX = animationProgress
                scaleY = animationProgress
                this.transformOrigin = transformOrigin
            }
            .clickable(indication = null, interactionSource = null) { onDismiss() }
            .shadow(12.dp, balloonShape)
            .clip(balloonShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(AccentOrange.copy(alpha = 0.4f), Color.White)
                ),
                shape = balloonShape
            )
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = if (isBelow) 22.dp else 12.dp,
                bottom = if (!isBelow) 22.dp else 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                lineHeight = 16.sp
            ),
            color = Slate800,
            textAlign = TextAlign.Center
        )
    }
}

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

@Preview
@Composable
fun BalloonContentAbovePreview() {
    PreviewRubikTheme(isDark = true) {
        Box(
            modifier = Modifier
                .background(Slate900)
                .padding(24.dp)
        ) {
            BalloonContent(
                text = "Küpü döndürmek ve incelemek için parmağınızı sürükleyin.",
                onDismiss = {},
                arrowX = 140f,
                isBelow = false,
                animationProgress = 1f,
                transformOrigin = TransformOrigin(0.5f, 1f)
            )
        }
    }
}

@Preview
@Composable
fun BalloonContentBelowPreview() {
    PreviewRubikTheme(isDark = false) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(24.dp)
        ) {
            BalloonContent(
                text = "Tebrikler! Küp çözüldü.",
                onDismiss = {},
                arrowX = 80f,
                isBelow = true,
                animationProgress = 1f,
                transformOrigin = TransformOrigin(0.3f, 0f)
            )
        }
    }
}
