package com.vahitkeskin.rubiksync.ui.cube

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
fun FaceGrid(
    face: FaceName,
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    modifier: Modifier = Modifier,
    cellSize: Dp = 24.dp,
    spacing: Dp = 2.dp,
    isClickable: Boolean = true,
    onCellClick: (FaceName, Int, Int) -> Unit = { _, _, _ -> }
) {
    val grid = faces[face]!!
    Column(
        modifier = modifier
            .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(8.dp))
            .background(BlackAlpha05, RoundedCornerShape(8.dp))
            .padding(spacing * 2),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                for (c in 0..2) {
                    val color = grid[r][c]
                    val isCenter = r == 1 && c == 1
                    val canClick = isClickable && !isCenter
                    
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .clip(RoundedCornerShape((cellSize.value * 0.15f).dp))
                            .background(Color(color.rgb))
                            .clickable(enabled = canClick) {
                                onCellClick(face, r, c)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCenter) {
                            Box(
                                modifier = Modifier
                                    .size((cellSize.value * 0.18f).dp)
                                    .clip(RoundedCornerShape((cellSize.value * 0.09f).dp))
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun FaceGridDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val dummyFaces = FaceName.values().associateWith { Array(3) { Array(3) { CubeColor.GREEN } } }
        Box(
            modifier = Modifier
                .background(RubikTheme.colors.backgroundPrimary)
                .padding(16.dp)
        ) {
            FaceGrid(
                face = FaceName.F,
                faces = dummyFaces,
                cellSize = 40.dp
            )
        }
    }
}

@Preview
@Composable
fun FaceGridLightPreview() {
    PreviewRubikTheme(isDark = false) {
        val dummyFaces = FaceName.values().associateWith { Array(3) { Array(3) { CubeColor.ORANGE } } }
        Box(
            modifier = Modifier
                .background(RubikTheme.colors.backgroundPrimary)
                .padding(16.dp)
        ) {
            FaceGrid(
                face = FaceName.U,
                faces = dummyFaces,
                cellSize = 40.dp
            )
        }
    }
}
