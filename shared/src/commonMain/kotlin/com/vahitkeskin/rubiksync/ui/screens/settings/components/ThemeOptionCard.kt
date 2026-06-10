package com.vahitkeskin.rubiksync.ui.screens.settings.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun ThemeOptionCard(
    emoji: String,
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
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = emoji,
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

        // Seçili göstergesi
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

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
private fun ThemeOptionCardDarkSelectedPreview() {
    PreviewRubikTheme(isDark = true) {
        Box(
            modifier = Modifier
                .background(RubikTheme.colors.backgroundPanel)
                .padding(16.dp)
        ) {
            ThemeOptionCard(
                emoji = "☀️",
                label = "Açık",
                description = "Temiz görünüm",
                isSelected = true,
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun ThemeOptionCardLightUnselectedPreview() {
    PreviewRubikTheme(isDark = false) {
        Box(
            modifier = Modifier
                .background(RubikTheme.colors.backgroundPanel)
                .padding(16.dp)
        ) {
            ThemeOptionCard(
                emoji = "🌙",
                label = "Karanlık",
                description = "Karanlık mod",
                isSelected = false,
                onClick = {}
            )
        }
    }
}
