package com.vahitkeskin.rubiksync.ui.screens.scanner.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.solver.IntVector3
import com.vahitkeskin.rubiksync.ui.components.balloon.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun ScannedWorkspace(
    appState: RubikAppState,
    currentFace: FaceName,
    faceImageBitmap: ImageBitmap?,
    currentScale: Float,
    currentOffsetX: Float,
    currentOffsetY: Float,
    viewportBounds: Rect?,
    scannerTargetBounds: Rect?,
    isScrollInProgress: Boolean,
    onPreviewPositioned: (Rect) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Box: Photo Preview with Grid Overlay
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    RubikTheme.colors.borderSubtle,
                    RoundedCornerShape(16.dp)
                )
                .background(RubikTheme.colors.backgroundPrimary),
            contentAlignment = Alignment.Center
        ) {
            if (faceImageBitmap != null) {
                Image(
                    bitmap = faceImageBitmap,
                    contentDescription = "Yüz Fotoğrafı",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Canvas Grid overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val gridW = w * currentScale
                val gridH = h * currentScale

                val left = (w - gridW) / 2f + currentOffsetX * w
                val top = (h - gridH) / 2f + currentOffsetY * h

                val stepW = gridW / 3f
                val stepH = gridH / 3f

                drawRect(
                    color = Color.Green,
                    topLeft = androidx.compose.ui.geometry.Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(gridW, gridH),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.dp.toPx()
                    )
                )

                for (i in 1..2) {
                    drawLine(
                        color = Color.Green,
                        start = androidx.compose.ui.geometry.Offset(
                            left + i * stepW,
                            top
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            left + i * stepW,
                            top + gridH
                        ),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.Green,
                        start = androidx.compose.ui.geometry.Offset(
                            left,
                            top + i * stepH
                        ),
                        end = androidx.compose.ui.geometry.Offset(
                            left + gridW,
                            top + i * stepH
                        ),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                for (r in 0..2) {
                    for (c in 0..2) {
                        val cx = left + (c + 0.5f) * stepW
                        val cy = top + (r + 0.5f) * stepH
                        drawCircle(
                            color = Color.Red,
                            radius = 2.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(cx, cy)
                        )
                    }
                }
            }
        }

        // Right Box: Color Preview Grid
        val displayGrid = appState.scannedGrids[currentFace]
        val displayRawGrid = appState.scannedRawRGBs[currentFace]

        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    1.dp,
                    RubikTheme.colors.borderSubtle,
                    RoundedCornerShape(16.dp)
                )
                .background(RubikTheme.colors.backgroundPrimary)
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPreviewPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                },
            contentAlignment = Alignment.Center
        ) {
            if (displayGrid != null && displayRawGrid != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize().padding(10.dp)
                ) {
                    Text(
                        text = appState.strings.colorPreview,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.padding(2.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (row in 0..2) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (col in 0..2) {
                                    val cellColor = displayGrid[row][col]
                                    val isCenter = row == 1 && col == 1

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color(cellColor.rgb))
                                            .border(
                                                width = if (isCenter) 1.5.dp else 0.5.dp,
                                                color = if (isCenter) RubikTheme.colors.textPrimary else RubikTheme.colors.borderSubtle,
                                                shape = RoundedCornerShape(5.dp)
                                            )
                                            .clickable {
                                                val actualGrid = appState.scannedGrids[currentFace]
                                                val actualRawGrid = appState.scannedRawRGBs[currentFace]

                                                val colorsList = listOf(
                                                    CubeColor.ORANGE,
                                                    CubeColor.RED,
                                                    CubeColor.YELLOW,
                                                    CubeColor.WHITE,
                                                    CubeColor.GREEN,
                                                    CubeColor.BLUE
                                                )
                                                val currentIndex = colorsList.indexOf(cellColor)
                                                val nextIndex = (currentIndex + 1) % colorsList.size
                                                val targetColor = colorsList[nextIndex]

                                                val defaultReferences = mapOf(
                                                    CubeColor.ORANGE to IntVector3(255, 130, 0),
                                                    CubeColor.RED to IntVector3(220, 20, 20),
                                                    CubeColor.YELLOW to IntVector3(240, 240, 0),
                                                    CubeColor.WHITE to IntVector3(230, 230, 230),
                                                    CubeColor.GREEN to IntVector3(0, 160, 0),
                                                    CubeColor.BLUE to IntVector3(0, 0, 200)
                                                )
                                                val refRGB = defaultReferences[targetColor] ?: IntVector3(0, 0, 0)

                                                if (!isCenter) {
                                                    val updatedRawGrid = (actualRawGrid ?: displayRawGrid).map { it.copyOf() }
                                                        .toTypedArray()
                                                    updatedRawGrid[row][col] = refRGB

                                                    val updatedRaw = appState.scannedRawRGBs.toMutableMap()
                                                    updatedRaw[currentFace] = updatedRawGrid
                                                    appState.updateScannedRawRGBs(updatedRaw)
                                                }

                                                val updatedGrid = (actualGrid ?: displayGrid).map { it.copyOf() }
                                                    .toTypedArray()
                                                updatedGrid[row][col] = targetColor
                                                val updatedGrids = appState.scannedGrids.toMutableMap()
                                                updatedGrids[currentFace] = updatedGrid
                                                appState.updateScannedGrids(updatedGrids)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isCenter) {
                                            Text("🔒", fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                CircularProgressIndicator(
                    color = RubikTheme.colors.accentBlue,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
            val viewport = viewportBounds
            val isPreviewVisible = appState.scannerShowcaseStep == 5 &&
                    !appState.isScannerShowcaseCompleted &&
                    viewport != null &&
                    scannerTargetBounds != null &&
                    scannerTargetBounds.bottom >= viewport.top + 10f &&
                    scannerTargetBounds.top <= viewport.bottom - 10f &&
                    !isScrollInProgress

            AuraBalloon(
                text = appState.strings.showcaseScannerPreview,
                isVisible = isPreviewVisible,
                isBelow = false,
                onDismiss = { appState.advanceScannerShowcase() }
            )
        }
    }
}
