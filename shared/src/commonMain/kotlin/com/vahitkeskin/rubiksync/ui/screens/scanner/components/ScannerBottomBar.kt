package com.vahitkeskin.rubiksync.ui.screens.scanner.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.icons.ArrowBackIcon
import com.vahitkeskin.rubiksync.ui.icons.ArrowForwardIcon
import com.vahitkeskin.rubiksync.ui.icons.CheckIcon
import com.vahitkeskin.rubiksync.ui.icons.CloseIcon
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun ScannerBottomBar(
    appState: RubikAppState,
    currentFace: FaceName,
    onDismiss: () -> Unit,
    onComplete: (Map<FaceName, Array<Array<CubeColor>>>) -> Unit,
    onPositioned: (Rect) -> Unit
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                },
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RubikTheme.colors.backgroundSecondary,
                    contentColor = RubikTheme.colors.textPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = CloseIcon,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(14.dp),
                        tint = RubikTheme.colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appState.strings.cancelButton,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = {
                    appState.updateErrorMessage(null)
                    appState.updateInfoMessage(null)
                    if (appState.scannerStep > 0) appState.updateScannerStep(appState.scannerStep - 1)
                },
                enabled = appState.scannerStep > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RubikTheme.colors.backgroundSecondary,
                    contentColor = RubikTheme.colors.textPrimary,
                    disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                    disabledContentColor = RubikTheme.colors.buttonDisabledText
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                modifier = Modifier.weight(1f).height(42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = ArrowBackIcon,
                        contentDescription = "Back",
                        modifier = Modifier.size(14.dp),
                        tint = if (appState.scannerStep > 0) RubikTheme.colors.textPrimary else RubikTheme.colors.buttonDisabledText
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appState.strings.backButton,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            val hasCurrentScan = appState.scannedFilePaths.containsKey(currentFace)
            val isAllFacesScanned = appState.scannedFilePaths.size == 6

            if (isAllFacesScanned) {
                Button(
                    onClick = {
                        val completeGrids = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
                        var isValid = true
                        for (face in FaceName.entries) {
                            val gridVal = appState.scannedGrids[face]
                            val hasPath = appState.scannedFilePaths.containsKey(face)
                            if (gridVal != null && hasPath) {
                                completeGrids[face] = gridVal
                            } else {
                                isValid = false
                            }
                        }
                        if (isValid) {
                            onComplete(completeGrids)
                            appState.updateSuccessMessage(appState.strings.successScanComplete)
                        } else {
                            appState.updateErrorMessage(appState.strings.errorScanAllFaces)
                        }
                    },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RubikTheme.colors.accentOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1.1f).height(42.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = CheckIcon,
                            contentDescription = "Complete",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appState.strings.setButton,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            } else if (appState.scannerStep < 5) {
                Button(
                    onClick = {
                        appState.updateErrorMessage(null)
                        appState.updateInfoMessage(null)
                        var foundNext = false
                        for (i in 1..5) {
                            val nextIdx = (appState.scannerStep + i) % 6
                            val nextFace = FaceName.entries[nextIdx]
                            if (!appState.scannedFilePaths.containsKey(nextFace)) {
                                appState.updateScannerStep(nextIdx)
                                foundNext = true
                                break
                            }
                        }
                        if (!foundNext) {
                            appState.updateScannerStep((appState.scannerStep + 1).coerceAtMost(5))
                        }
                    },
                    enabled = hasCurrentScan,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasCurrentScan) RubikTheme.colors.accentBlue else RubikTheme.colors.backgroundSecondary,
                        contentColor = if (hasCurrentScan) Color.White else RubikTheme.colors.textSecondary,
                        disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                        disabledContentColor = RubikTheme.colors.buttonDisabledText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    border = if (hasCurrentScan) null else BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                    modifier = Modifier.weight(1.1f).height(42.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = appState.strings.nextButton,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = ArrowForwardIcon,
                            contentDescription = "Next",
                            modifier = Modifier.size(14.dp),
                            tint = if (hasCurrentScan) Color.White else RubikTheme.colors.textSecondary
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        val completeGrids = mutableMapOf<FaceName, Array<Array<CubeColor>>>()
                        var isValid = true
                        for (face in FaceName.entries) {
                            val gridVal = appState.scannedGrids[face]
                            val hasPath = appState.scannedFilePaths.containsKey(face)
                            if (gridVal != null && hasPath) {
                                completeGrids[face] = gridVal
                            } else {
                                isValid = false
                            }
                        }
                        if (isValid) {
                            onComplete(completeGrids)
                            appState.updateSuccessMessage(appState.strings.successScanComplete)
                        } else {
                            appState.updateErrorMessage(appState.strings.errorScanAllFaces)
                        }
                    },
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = RubikTheme.colors.buttonDisabledBg,
                        disabledContentColor = RubikTheme.colors.buttonDisabledText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                    modifier = Modifier.weight(1.1f).height(42.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = CheckIcon,
                            contentDescription = "Set",
                            modifier = Modifier.size(14.dp),
                            tint = RubikTheme.colors.buttonDisabledText
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appState.strings.setButton,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        AuraBalloon(
            text = appState.strings.showcaseScannerAction,
            isVisible = appState.scannerShowcaseStep == 6 && !appState.isScannerShowcaseCompleted,
            isBelow = false,
            onDismiss = { appState.advanceScannerShowcase() }
        )
    }
}
