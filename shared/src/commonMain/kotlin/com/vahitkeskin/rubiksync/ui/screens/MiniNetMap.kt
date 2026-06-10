package com.vahitkeskin.rubiksync.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.state.*

@Composable
internal fun MiniNetMap(
    appState: RubikAppState,
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    activeFace: FaceName,
    onFaceSelect: (FaceName) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(RubikTheme.colors.backgroundPanel)
            .border(1.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = appState.strings.cubeMapTitle,
            color = RubikTheme.colors.textSecondary,
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // U face
            Row {
                Spacer(modifier = Modifier.width(26.dp))
                MiniFaceGrid(FaceName.U, faces, activeFace == FaceName.U, onFaceSelect)
            }
            // L, F, R, B faces
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                MiniFaceGrid(FaceName.L, faces, activeFace == FaceName.L, onFaceSelect)
                MiniFaceGrid(FaceName.F, faces, activeFace == FaceName.F, onFaceSelect)
                MiniFaceGrid(FaceName.R, faces, activeFace == FaceName.R, onFaceSelect)
                MiniFaceGrid(FaceName.B, faces, activeFace == FaceName.B, onFaceSelect)
            }
            // D face
            Row {
                Spacer(modifier = Modifier.width(26.dp))
                MiniFaceGrid(FaceName.D, faces, activeFace == FaceName.D, onFaceSelect)
            }
        }
    }
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview
@Composable
private fun MiniFaceGridDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val dummyFaces = FaceName.values().associateWith { Array(3) { Array(3) { CubeColor.GREEN } } }
        Box(
            modifier = Modifier
                .background(RubikTheme.colors.backgroundPanel)
                .padding(16.dp)
        ) {
            MiniFaceGrid(
                face = FaceName.F,
                faces = dummyFaces,
                isActive = true,
                onFaceSelect = {}
            )
        }
    }
}

@Preview
@Composable
private fun MiniNetMapDarkPreview() {
    PreviewRubikTheme(isDark = true) {
        val appState = rememberPreviewRubikAppState()
        MiniNetMap(
            appState = appState,
            faces = appState.editorFaces,
            activeFace = FaceName.F,
            onFaceSelect = {}
        )
    }
}

@Preview
@Composable
private fun MiniNetMapLightPreview() {
    PreviewRubikTheme(isDark = false) {
        val appState = rememberPreviewRubikAppState()
        MiniNetMap(
            appState = appState,
            faces = appState.editorFaces,
            activeFace = FaceName.U,
            onFaceSelect = {}
        )
    }
}
