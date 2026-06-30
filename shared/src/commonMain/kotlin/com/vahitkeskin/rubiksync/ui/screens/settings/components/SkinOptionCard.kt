package com.vahitkeskin.rubiksync.ui.screens.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeSkin
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun SkinOptionCard(
    skin: CubeSkin,
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgUnselected = RubikTheme.colors.backgroundTertiary
    val textPrimary = RubikTheme.colors.textPrimary
    val textSecondary = RubikTheme.colors.textSecondary

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) {
            if (RubikTheme.colors.isDark) SelectionDarkOrange else AccentOrangeSoftBg
        } else bgUnselected,
        animationSpec = tween(300)
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(animatedBgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                brush = if (isSelected) {
                    Brush.linearGradient(listOf(AccentOrange, AccentRedCoral))
                } else {
                    Brush.linearGradient(
                        listOf(
                            if (RubikTheme.colors.isDark) WhiteAlpha04 else BlackAlpha10,
                            if (RubikTheme.colors.isDark) WhiteAlpha04 else BlackAlpha10
                        )
                    )
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = skin.emoji,
            fontSize = 22.sp
        )
        Text(
            text = label,
            color = if (isSelected) AccentOrange else textPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = description,
            color = textSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            val colors = listOf(
                CubeColor.ORANGE,
                CubeColor.RED,
                CubeColor.YELLOW,
                CubeColor.WHITE,
                CubeColor.GREEN,
                CubeColor.BLUE
            )
            colors.forEach { cubeColor ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(skin.getStickerColor(cubeColor))
                        .border(0.5.dp, if (RubikTheme.colors.isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f), CircleShape)
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .height(2.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(AccentOrange, AccentRedCoral)
                        )
                    )
            )
        }
    }
}
