package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

@Composable
fun CustomPopupDialog(
    icon: ImageVector,
    title: String,
    description: String,
    cancelText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    iconTint: Color = RubikTheme.colors.accentBlue,
    iconBgColor: Color = RubikTheme.colors.backgroundPrimary,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = RubikTheme.colors.backgroundSecondary),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp) // Offset for the circular icon
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = title,
                        color = RubikTheme.colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = description,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RubikTheme.colors.backgroundTertiary,
                                contentColor = RubikTheme.colors.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = cancelText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = iconTint,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = confirmText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Top Circular Icon Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(iconBgColor)
                    .border(4.dp, RubikTheme.colors.backgroundSecondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
