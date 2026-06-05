package com.vahitkeskin.rubiksync.ui.dialogs

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import androidx.compose.foundation.BorderStroke
import com.vahitkeskin.rubiksync.ui.components.FaceGrid
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import com.vahitkeskin.rubiksync.ui.icons.ArrowBackIcon
import com.vahitkeskin.rubiksync.utils.parseDetectedState
import kotlinx.coroutines.launch

@Composable
private fun MiniFaceGrid(
    face: FaceName,
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    isActive: Boolean,
    onFaceSelect: (FaceName) -> Unit
) {
    val grid = faces[face]!!
    Column(
        modifier = Modifier
            .border(
                width = if (isActive) 1.5.dp else 0.5.dp,
                color = if (isActive) RubikTheme.colors.accentBlue else RubikTheme.colors.borderSubtle,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onFaceSelect(face) }
            .background(
                if (isActive) RubikTheme.colors.accentBlue.copy(alpha = 0.08f) else BlackAlpha02,
                RoundedCornerShape(4.dp)
            )
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(0.5.dp)
    ) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.5.dp)) {
                for (c in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(Color(grid[r][c].rgb))
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniNetMap(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDialog(
    show: Boolean,
    appState: RubikAppState,
    onDismiss: () -> Unit,
    onStartScanWizard: () -> Unit
) {
    if (!show) return

    var activeFace by remember { mutableStateOf(FaceName.F) }
    var showJsonImportDialog by remember { mutableStateOf(false) }
    var jsonInputText by remember { mutableStateOf("") }

    val cubeState = appState.cubeState

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = RubikTheme.colors.backgroundPrimary,
        dragHandle = {
            // Custom drag handle with gradient accent bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentOrange, AccentBlue)
                            )
                        )
                )
            }
        },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Geri butonu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RubikTheme.colors.backgroundSecondary)
                        .border(0.5.dp, RubikTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ArrowBackIcon,
                        contentDescription = "Geri",
                        tint = RubikTheme.colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appState.strings.editorTitle,
                        color = RubikTheme.colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = appState.strings.editorSubtitle,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { showJsonImportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RubikTheme.colors.backgroundSecondary,
                        contentColor = RubikTheme.colors.textSecondary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("📋 JSON", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Scan Card — glassmorphism style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (RubikTheme.colors.isDark) {
                                listOf(DarkGradientBg2, DarkGradientBg4)
                            } else {
                                listOf(AccentBlueFaintBg, AccentBlueSoftBg)
                            }
                        )
                    )
                    .border(1.dp, RubikTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = appState.strings.cameraScanTitle,
                        color = RubikTheme.colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = appState.strings.cameraScanSubtitle,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onStartScanWizard,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RubikTheme.colors.accentBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(appState.strings.scanAction, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Face selector tabs — pill chips with face colors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaceName.values().forEach { face ->
                    val isSelected = face == activeFace
                    val centerColor = when (face) {
                        FaceName.U -> AccentOrangeDark
                        FaceName.D -> AccentRedMaterial
                        FaceName.L -> AccentYellowMaterial
                        FaceName.R -> LightCardBg
                        FaceName.F -> AccentGreenMaterial
                        FaceName.B -> AccentBlueMedium
                    }
                    val label = when (face) {
                        FaceName.U -> appState.strings.faceU
                        FaceName.D -> appState.strings.faceD
                        FaceName.L -> appState.strings.faceL
                        FaceName.R -> appState.strings.faceR
                        FaceName.F -> appState.strings.faceF
                        FaceName.B -> appState.strings.faceB
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) {
                                    if (RubikTheme.colors.isDark) DarkBorderPrimary else AccentBlueFaintBg
                                } else {
                                    RubikTheme.colors.backgroundSecondary
                                }
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) RubikTheme.colors.accentBlue else RubikTheme.colors.borderSubtle,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { activeFace = face }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(centerColor)
                            )
                            Text(
                                text = label,
                                color = if (isSelected) {
                                    if (RubikTheme.colors.isDark) Color.White else AccentBlueNavy
                                } else {
                                    RubikTheme.colors.textSecondary
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Painter Workspace — Active Face Grid + Mini Map side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaceGrid(
                    face = activeFace,
                    faces = appState.editorFaces,
                    cellSize = 48.dp,
                    spacing = 3.dp,
                    isClickable = true,
                    onCellClick = { face, row, col ->
                        val updated = appState.editorFaces.toMutableMap()
                        val grid = updated[face]!!.map { it.copyOf() }.toTypedArray()
                        grid[row][col] = appState.selectedColor
                        updated[face] = grid
                        appState.updateEditorFaces(updated)
                    }
                )

                Spacer(modifier = Modifier.width(14.dp))

                MiniNetMap(
                    appState = appState,
                    faces = appState.editorFaces,
                    activeFace = activeFace,
                    onFaceSelect = { activeFace = it }
                )
            }

            // Color Palette with selected color name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                val colorName = when (appState.selectedColor) {
                    CubeColor.ORANGE -> appState.strings.colorOrange
                    CubeColor.RED -> appState.strings.colorRed
                    CubeColor.YELLOW -> appState.strings.colorYellow
                    CubeColor.WHITE -> appState.strings.colorWhite
                    CubeColor.GREEN -> appState.strings.colorGreen
                    CubeColor.BLUE -> appState.strings.colorBlue
                    else -> ""
                }

                Text(
                    text = "${appState.strings.brushColorPrefix}$colorName",
                    color = RubikTheme.colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp),
                    maxLines = 1
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val paletteColors = listOf(
                        CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
                        CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
                    )
                    paletteColors.forEach { color ->
                        val isSelected = appState.selectedColor == color

                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 36.dp else 32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(color.rgb))
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.5.dp,
                                    color = if (isSelected) RubikTheme.colors.textPrimary else RubikTheme.colors.borderSubtle,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { appState.updateSelectedColor(color) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (color == CubeColor.WHITE) Color.Black else Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Buttons with icons
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = appState.strings.cancelButton,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        appState.updateEditorFaces(mapOf(
                            FaceName.U to Array(3) { Array(3) { CubeColor.ORANGE } },
                            FaceName.D to Array(3) { Array(3) { CubeColor.RED } },
                            FaceName.L to Array(3) { Array(3) { CubeColor.YELLOW } },
                            FaceName.R to Array(3) { Array(3) { CubeColor.WHITE } },
                            FaceName.F to Array(3) { Array(3) { CubeColor.GREEN } },
                            FaceName.B to Array(3) { Array(3) { CubeColor.BLUE } }
                        ))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (RubikTheme.colors.isDark) SelectionDarkMagenta else AccentRedFaintBg,
                        contentColor = RubikTheme.colors.accentRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    border = BorderStroke(1.dp, if (RubikTheme.colors.isDark) SelectionMediumMagenta else AccentRedSoftBg),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(
                        text = appState.strings.clearButton,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        onDismiss()
                        appState.coroutineScope.launch {
                            val success = cubeState.setCustomStateAnimated(appState.editorFaces)
                            if (success) {
                                appState.clearManualMoves()
                                appState.saveCurrentState()
                                appState.updateActiveSolution(null)
                                appState.updateErrorMessage(null)
                                appState.updateSuccessMessage(appState.strings.cubeStateApplied)
                            } else {
                                appState.updateErrorMessage(appState.strings.invalidCubeDesign)
                            }
                        }
                    },
                    enabled = !cubeState.isAnimating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RubikTheme.colors.accentOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1.1f).height(42.dp)
                ) {
                    Text(
                        text = appState.strings.applyButton,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }
        }

        // JSON Import Dialog
        if (showJsonImportDialog) {
            AlertDialog(
                onDismissRequest = { showJsonImportDialog = false },
                title = {
                    Text(
                        appState.strings.importJsonTitle,
                        color = RubikTheme.colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = appState.strings.pasteJsonDesc,
                            color = RubikTheme.colors.textSecondary,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                        OutlinedTextField(
                            value = jsonInputText,
                            onValueChange = { jsonInputText = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = RubikTheme.colors.textPrimary,
                                unfocusedTextColor = RubikTheme.colors.textPrimary,
                                focusedBorderColor = RubikTheme.colors.accentOrange,
                                unfocusedBorderColor = RubikTheme.colors.borderPrimary
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = parseDetectedState(jsonInputText)
                            if (parsed != null) {
                                appState.updateEditorFaces(parsed)
                                showJsonImportDialog = false
                                jsonInputText = ""
                                appState.updateSuccessMessage(appState.strings.jsonImportSuccess)
                            } else {
                                appState.updateErrorMessage(appState.strings.jsonImportError)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RubikTheme.colors.accentOrange),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(appState.strings.importJsonButton, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showJsonImportDialog = false
                            jsonInputText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RubikTheme.colors.backgroundSecondary),
                        border = BorderStroke(1.dp, RubikTheme.colors.buttonBorder),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(appState.strings.cancelAction, color = RubikTheme.colors.textPrimary, fontSize = 12.sp, maxLines = 1)
                    }
                },
                containerColor = RubikTheme.colors.cardBackground,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}