package com.vahitkeskin.rubiksync.ui.screens.scanner.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.vahitkeskin.rubiksync.CameraCaptureOrPicker
import com.vahitkeskin.rubiksync.cube.CubeRotationGuide
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.cube.RubikImageProcessor
import com.vahitkeskin.rubiksync.ui.components.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun UnscannedWorkspace(
    appState: RubikAppState,
    currentFace: FaceName,
    isGuideExpanded: Boolean,
    onGuideExpandedChange: (Boolean) -> Unit,
    guidanceMessage: String,
    faceNameLocalized: Map<FaceName, String>,
    onGuidePositioned: (Rect) -> Unit,
    onCapturePositioned: (Rect) -> Unit
) {
    val coroutineScope = appState.coroutineScope

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Not scanned yet - display guide card statically at the top
        Box {
            CubeRotationGuide(
                appState = appState,
                currentFace = currentFace,
                isExpanded = isGuideExpanded,
                onExpandedChange = onGuideExpandedChange,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInRoot()
                        val size = coords.size
                        onGuidePositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                    }
            )
            AuraBalloon(
                text = appState.strings.showcaseScannerGuide,
                isVisible = appState.scannerShowcaseStep == 2 && !appState.isScannerShowcaseCompleted,
                isBelow = true,
                onDismiss = { appState.advanceScannerShowcase() }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            if (appState.isRecalculating) {
                CircularProgressIndicator(
                    color = RubikTheme.colors.accentBlue,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CameraCaptureOrPicker(
                        faceName = currentFace.name,
                        takePhotoLabel = appState.strings.takePhotoLabel,
                        chooseGalleryLabel = appState.strings.chooseGalleryLabel,
                        selectImageLabel = appState.strings.selectImageLabel,
                        guidanceText = guidanceMessage,
                        onImageSelected = { filePath ->
                            coroutineScope.launch {
                                appState.updateRecalculating(true)
                                appState.updateErrorMessage(null)
                                appState.updateInfoMessage(null)
                                val detectedFace = withContext(Dispatchers.Default) {
                                    RubikImageProcessor().detectFaceFromImage(filePath)
                                }
                                appState.updateRecalculating(false)
                                if (detectedFace != null) {
                                    val updatedPaths = appState.scannedFilePaths.toMutableMap()
                                    updatedPaths[detectedFace] = filePath
                                    appState.updateScannedFilePaths(updatedPaths)

                                    val updatedScales = appState.gridScales.toMutableMap()
                                    updatedScales[detectedFace] = 0.55f
                                    appState.updateGridScales(updatedScales)

                                    val updatedOffsetsX = appState.gridOffsetsX.toMutableMap()
                                    updatedOffsetsX[detectedFace] = 0f
                                    appState.updateGridOffsetsX(updatedOffsetsX)

                                    val updatedOffsetsY = appState.gridOffsetsY.toMutableMap()
                                    updatedOffsetsY[detectedFace] = 0f
                                    appState.updateGridOffsetsY(updatedOffsetsY)

                                    appState.updateScannerStep(detectedFace.ordinal)

                                    val faceDisplayName = faceNameLocalized[detectedFace] ?: detectedFace.name
                                    val centerColorLocalized = when (detectedFace) {
                                        FaceName.U -> appState.strings.colorOrange
                                        FaceName.D -> appState.strings.colorRed
                                        FaceName.L -> appState.strings.colorYellow
                                        FaceName.R -> appState.strings.colorWhite
                                        FaceName.F -> appState.strings.colorGreen
                                        FaceName.B -> appState.strings.colorBlue
                                    }
                                    appState.updateInfoMessage(
                                        appState.strings.faceDetectedMessage
                                            .replaceFirst("%s", centerColorLocalized)
                                            .replaceFirst("%s", faceDisplayName)
                                    )
                                } else {
                                    appState.updateErrorMessage(appState.strings.faceNotDetected)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                val size = coords.size
                                onCapturePositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                            }
                    )
                    AuraBalloon(
                        text = appState.strings.showcaseScannerCapture,
                        isVisible = appState.scannerShowcaseStep == 3 && !appState.isScannerShowcaseCompleted,
                        isBelow = true,
                        onDismiss = { appState.advanceScannerShowcase() }
                    )
                }
            }
        }
    }
}
