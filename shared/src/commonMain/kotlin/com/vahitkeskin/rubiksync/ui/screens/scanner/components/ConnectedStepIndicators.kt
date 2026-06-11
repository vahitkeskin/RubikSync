package com.vahitkeskin.rubiksync.ui.screens.scanner.components

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
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun ConnectedStepIndicators(
    appState: RubikAppState,
    faceColorMap: Map<FaceName, Color>,
    onPositioned: (Rect) -> Unit
) {
    Box {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                }
        ) {
            FaceName.values().forEachIndexed { index, face ->
                val isCurrent = (appState.scannerStep == index)
                val isScanned = appState.scannedFilePaths.containsKey(face)

                val baseColor = faceColorMap[face] ?: Color.Gray
                val circleBg =
                    if (isScanned || isCurrent) baseColor.copy(alpha = 0.9f) else RubikTheme.colors.backgroundTertiary
                val textColor =
                    if (face == FaceName.R && (isScanned || isCurrent)) Color.Black else (if (isScanned || isCurrent) Color.White else RubikTheme.colors.textSecondary)

                if (index > 0) {
                    // Connecting line
                    val prevScanned =
                        appState.scannedFilePaths.containsKey(FaceName.values()[index - 1])

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (prevScanned) RubikTheme.colors.accentGreen.copy(alpha = 0.5f)
                                else RubikTheme.colors.borderSubtle
                            )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(circleBg)
                        .border(
                            width = if (isCurrent) 2.dp else 0.dp,
                            color = if (isCurrent) RubikTheme.colors.textPrimary else Color.Transparent,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .clickable {
                            appState.updateScannerStep(index)
                            appState.updateErrorMessage(null)
                            appState.updateInfoMessage(null)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = face.name,
                        color = textColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }
        AuraBalloon(
            text = appState.strings.showcaseScannerIndicator,
            isVisible = appState.scannerShowcaseStep == 1 && !appState.isScannerShowcaseCompleted,
            isBelow = true,
            onDismiss = { appState.advanceScannerShowcase() }
        )
    }
}
