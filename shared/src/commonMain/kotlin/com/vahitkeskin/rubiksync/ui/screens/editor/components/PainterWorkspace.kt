package com.vahitkeskin.rubiksync.ui.screens.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.cube.FaceGrid
import com.vahitkeskin.rubiksync.ui.components.balloon.AuraBalloon
import com.vahitkeskin.rubiksync.ui.state.RubikAppState

@Composable
internal fun PainterWorkspace(
    appState: RubikAppState,
    activeFace: FaceName,
    onFaceSelect: (FaceName) -> Unit,
    onPositioned: (Rect) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            FaceGrid(
                face = activeFace,
                faces = appState.editorFaces,
                cellSize = 48.dp,
                spacing = 3.dp,
                isClickable = true,
                modifier = Modifier.onGloballyPositioned { coords ->
                    val pos = coords.positionInRoot()
                    val size = coords.size
                    onPositioned(Rect(pos.x, pos.y, pos.x + size.width, pos.y + size.height))
                },
                onCellClick = { face, row, col ->
                    appState.updateEditorFaceCell(face, row, col, appState.selectedColor)
                }
            )
            AuraBalloon(
                text = appState.strings.showcaseEditorGrid,
                isVisible = appState.editorShowcaseStep == 2 && !appState.isEditorShowcaseCompleted,
                isBelow = false,
                onDismiss = { appState.advanceEditorShowcase() }
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        MiniNetMap(
            appState = appState,
            faces = appState.editorFaces,
            activeFace = activeFace,
            onFaceSelect = onFaceSelect
        )
    }
}
