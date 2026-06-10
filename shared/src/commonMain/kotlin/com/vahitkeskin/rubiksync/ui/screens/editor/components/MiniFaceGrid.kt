package com.vahitkeskin.rubiksync.ui.screens.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.state.BlackAlpha02
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
internal fun MiniFaceGrid(
    face: FaceName,
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    isActive: Boolean,
    onFaceSelect: (FaceName) -> Unit
) {
    val grid = faces[face] ?: Array(3) { Array(3) { CubeColor.INTERNAL } }
    Column(
        modifier = Modifier
            .border(
                width = if (isActive) 1.5.dp else 0.5.dp,
                color = if (isActive) RubikTheme.colors.accentBlue else RubikTheme.colors.borderSubtle,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onFaceSelect(face) }
            .background(
                if (isActive) RubikTheme.colors.accentBlue.copy(alpha = 0.08f) else BlackAlpha02,
                RoundedCornerShape(4.dp)
            )
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(0.5.dp)
    ) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.5.dp)) {
                for (c in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color(grid[r][c].rgb))
                    )
                }
            }
        }
    }
}
