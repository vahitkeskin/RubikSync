package com.vahitkeskin.rubiksync.ui.screens.scanner.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun GridAlignControls(
    appState: RubikAppState,
    currentFace: FaceName,
    currentScale: Float,
    currentOffsetX: Float,
    currentOffsetY: Float,
    viewportBounds: Rect?,
    scannerTargetBounds: Rect?,
    isScrollInProgress: Boolean,
    onPositioned: (Rect) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Alignment sliders with values
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInRoot()
                        val size = coords.size
                        onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                    },
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = appState.strings.alignGridDesc,
                    color = RubikTheme.colors.textSecondary,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Scale
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appState.strings.sizeLabel,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        maxLines = 1
                    )
                    Slider(
                        value = currentScale,
                        onValueChange = { s ->
                            val updated = appState.gridScales.toMutableMap()
                            updated[currentFace] = s
                            appState.updateGridScales(updated)
                        },
                        valueRange = 0.3f..0.9f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = RubikTheme.colors.accentBlue,
                            inactiveTrackColor = RubikTheme.colors.speedTrack,
                            thumbColor = if (RubikTheme.colors.isDark) Color.White else RubikTheme.colors.accentBlue
                        )
                    )
                    Text(
                        text = "${(currentScale * 100).toInt()}%",
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 9.sp,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }

                // Offset X
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appState.strings.horizontalLabel,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        maxLines = 1
                    )
                    Slider(
                        value = currentOffsetX,
                        onValueChange = { x ->
                            val updated = appState.gridOffsetsX.toMutableMap()
                            updated[currentFace] = x
                            appState.updateGridOffsetsX(updated)
                        },
                        valueRange = -0.3f..0.3f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = RubikTheme.colors.accentBlue,
                            inactiveTrackColor = RubikTheme.colors.speedTrack,
                            thumbColor = if (RubikTheme.colors.isDark) Color.White else RubikTheme.colors.accentBlue
                        )
                    )
                    Text(
                        text = "${(currentOffsetX * 100).toInt()}",
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 9.sp,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }

                // Offset Y
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = appState.strings.verticalLabel,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(40.dp),
                        maxLines = 1
                    )
                    Slider(
                        value = currentOffsetY,
                        onValueChange = { y ->
                            val updated = appState.gridOffsetsY.toMutableMap()
                            updated[currentFace] = y
                            appState.updateGridOffsetsY(updated)
                        },
                        valueRange = -0.3f..0.3f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = RubikTheme.colors.accentBlue,
                            inactiveTrackColor = RubikTheme.colors.speedTrack,
                            thumbColor = if (RubikTheme.colors.isDark) Color.White else RubikTheme.colors.accentBlue
                        )
                    )
                    Text(
                        text = "${(currentOffsetY * 100).toInt()}",
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 9.sp,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
            val viewport = viewportBounds
            val isSlidersVisible = appState.scannerShowcaseStep == 4 &&
                    !appState.isScannerShowcaseCompleted &&
                    viewport != null &&
                    scannerTargetBounds != null &&
                    scannerTargetBounds.bottom >= viewport.top + 10f &&
                    scannerTargetBounds.top <= viewport.bottom - 10f &&
                    !isScrollInProgress

            AuraBalloon(
                text = appState.strings.showcaseScannerSliders,
                isVisible = isSlidersVisible,
                isBelow = false,
                onDismiss = { appState.advanceScannerShowcase() }
            )
        }

        // Re-capture button
        Button(
            onClick = {
                val updatedPaths = appState.scannedFilePaths.toMutableMap()
                updatedPaths.remove(currentFace)
                appState.updateScannedFilePaths(updatedPaths)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = RubikTheme.colors.backgroundSecondary,
                contentColor = RubikTheme.colors.textSecondary
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
            border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = appState.strings.retakePhoto,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
