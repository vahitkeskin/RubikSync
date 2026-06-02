package com.vahitkeskin.rubiksync.ui.components

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
import androidx.compose.ui.unit.dp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName

@Composable
fun FaceGrid(
    face: FaceName,
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    onCellClick: (FaceName, Int, Int) -> Unit
) {
    val grid = faces[face]!!
    Column(
        modifier = Modifier
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(4.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (c in 0..2) {
                    val color = grid[r][c]
                    val isCenter = r == 1 && c == 1
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(color.rgb))
                            .clickable(enabled = !isCenter) {
                                onCellClick(face, r, c)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCenter) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }
        }
    }
}
