package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.RubikCubeState

@Composable
fun DashboardHeader(
    cubeState: RubikCubeState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title — compact
        Text(
            text = "RUBIK SYNC",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 4.sp,
            maxLines = 1
        )
        Text(
            text = "3D INTERACTIVE SIMULATOR",
            color = Color(0xFF5A6A7D),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            maxLines = 1
        )

        // Move History — scrollable chips
        if (cubeState.moveHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161D2A))
                    .padding(vertical = 4.dp, horizontal = 6.dp)
                    .horizontalScroll(rememberScrollState(initial = 10000)),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hamle:",
                    color = Color(0xFF5A6A7D),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                cubeState.moveHistory.takeLast(12).forEachIndexed { idx, move ->
                    val isLast = idx == cubeState.moveHistory.takeLast(12).lastIndex

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isLast) {
                                    Brush.horizontalGradient(listOf(Color(0xFFFF8A00), Color(0xFFFF5252)))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFF1E2633), Color(0xFF1E2633)))
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = move.label,
                            color = if (isLast) Color.White else Color(0xFFAABBCC),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
