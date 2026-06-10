package com.vahitkeskin.rubiksync.ui.screens.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
internal fun ColorPalette(
    appState: RubikAppState,
    onPositioned: (Rect) -> Unit
) {
    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                }
        ) {
            val colorName = when (appState.selectedColor) {
                CubeColor.ORANGE -> appState.strings.colorOrange
                CubeColor.RED -> appState.strings.colorRed
                CubeColor.YELLOW -> appState.strings.colorYellow
                CubeColor.WHITE -> appState.strings.colorWhite
                CubeColor.GREEN -> appState.strings.colorGreen
                CubeColor.BLUE -> appState.strings.colorBlue
                else -> ""
            }

            Text(
                text = "${appState.strings.brushColorPrefix}$colorName",
                color = RubikTheme.colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp),
                maxLines = 1
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val paletteColors = listOf(
                    CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
                    CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
                )
                paletteColors.forEach { color ->
                    val isSelected = appState.selectedColor == color

                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 36.dp else 32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(color.rgb))
                            .border(
                                width = if (isSelected) 2.5.dp else 0.5.dp,
                                color = if (isSelected) RubikTheme.colors.textPrimary else RubikTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { appState.updateSelectedColor(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (color == CubeColor.WHITE) Color.Black else Color.White)
                            )
                        }
                    }
                }
            }
        }
        AuraBalloon(
            text = appState.strings.showcaseEditorPalette,
            isVisible = appState.editorShowcaseStep == 3 && !appState.isEditorShowcaseCompleted,
            isBelow = false,
            onDismiss = { appState.advanceEditorShowcase() }
        )
    }
}
