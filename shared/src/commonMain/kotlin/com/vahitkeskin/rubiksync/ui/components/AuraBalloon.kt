package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.vahitkeskin.rubiksync.ui.state.AccentOrange

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
            durationMillis = 300,
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
                    x = x.coerceIn(screenPadding, windowSize.width - popupContentSize.width - screenPadding)
                    
                    // Ok ucunun (pointer) balon üzerindeki yatay konumu - balon gövdesinden dışarı taşmayı önle
                    balloonWidth = popupContentSize.width.toFloat()
                    val minArrowX = with(density) { 16.dp.toPx() }
                    val maxArrowX = if (balloonWidth > 0f) (balloonWidth - minArrowX).coerceAtLeast(minArrowX) else minArrowX
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
            properties = PopupProperties(focusable = true)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(max = 280.dp)
            .graphicsLayer {
                alpha = animationProgress
                scaleX = animationProgress
                scaleY = animationProgress
                this.transformOrigin = transformOrigin
            }
            .clickable(indication = null, interactionSource = null) { onDismiss() }
    ) {
        if (isBelow) {
            // Sivri uç (Arrow) - Üstte, yukarıyı gösteriyor
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .zIndex(1f)
                    .offset(y = 1.dp)
            ) {
                val arrowWidth = 18.dp.toPx()
                val arrowHeight = 10.dp.toPx()
                
                val arrowPath = Path().apply {
                    moveTo(arrowX - arrowWidth / 2, arrowHeight)
                    lineTo(arrowX, 0f)
                    lineTo(arrowX + arrowWidth / 2, arrowHeight)
                    close()
                }
                
                drawOutline(
                    outline = Outline.Generic(arrowPath),
                    color = Color.White
                )
            }
        }

        // Balon gövdesi (Açık Renk)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        listOf(AccentOrange.copy(alpha = 0.4f), Color.White)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp
                ),
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )
        }
        
        if (!isBelow) {
            // Sivri uç (Arrow) - Altta, aşağıyı gösteriyor
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .zIndex(1f)
                    .offset(y = (-1).dp)
            ) {
                val arrowWidth = 18.dp.toPx()
                val arrowHeight = 10.dp.toPx()
                
                val arrowPath = Path().apply {
                    moveTo(arrowX - arrowWidth / 2, 0f)
                    lineTo(arrowX, arrowHeight)
                    lineTo(arrowX + arrowWidth / 2, 0f)
                    close()
                }
                
                drawOutline(
                    outline = Outline.Generic(arrowPath),
                    color = Color.White
                )
            }
        }
    }
}
