package com.vahitkeskin.rubiksync.ui.screens.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun FaceSelectorTabs(
    appState: RubikAppState,
    activeFace: FaceName,
    onFaceSelect: (FaceName) -> Unit,
    onPositioned: (Rect) -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                },
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FaceName.entries.forEach { face ->
                val isSelected = face == activeFace
                val centerColor = when (face) {
                    FaceName.U -> AccentOrangeDark
                    FaceName.D -> AccentRedMaterial
                    FaceName.L -> AccentYellowMaterial
                    FaceName.R -> LightCardBg
                    FaceName.F -> AccentGreenMaterial
                    FaceName.B -> AccentBlueMedium
                }
                val label = when (face) {
                    FaceName.U -> appState.strings.faceU
                    FaceName.D -> appState.strings.faceD
                    FaceName.L -> appState.strings.faceL
                    FaceName.R -> appState.strings.faceR
                    FaceName.F -> appState.strings.faceF
                    FaceName.B -> appState.strings.faceB
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) {
                                if (RubikTheme.colors.isDark) DarkBorderPrimary else AccentBlueFaintBg
                            } else {
                                RubikTheme.colors.backgroundSecondary
                            }
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) RubikTheme.colors.accentBlue else RubikTheme.colors.borderSubtle,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onFaceSelect(face) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(centerColor)
                        )
                        Text(
                            text = label,
                            color = if (isSelected) {
                                if (RubikTheme.colors.isDark) Color.White else AccentBlueNavy
                            } else {
                                RubikTheme.colors.textSecondary
                            },
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        AuraBalloon(
            text = appState.strings.showcaseEditorFaces,
            isVisible = appState.editorShowcaseStep == 1 && !appState.isEditorShowcaseCompleted,
            isBelow = true,
            onDismiss = { appState.advanceEditorShowcase() }
        )
    }
}
