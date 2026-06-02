package com.vahitkeskin.rubiksync.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.state.RubikAppState

@Composable
fun BoxScope.FeedbackOverlay(
    appState: RubikAppState
) {
    val errorMessage = appState.errorMessage
    if (errorMessage != null) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xEEFF3B30))
                .border(1.dp, Color(0xFFFF3B30), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clickable { appState.errorMessage = null }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = errorMessage,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✕",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
