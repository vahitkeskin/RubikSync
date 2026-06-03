package com.vahitkeskin.rubiksync.ui.dialogs

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.components.FaceGrid
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.utils.parseDetectedState

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
                color = if (isActive) Color(0xFF448AFF) else Color(0x18FFFFFF),
                shape = RoundedCornerShape(3.dp)
            )
            .clickable { onFaceSelect(face) }
            .background(Color(0x08000000), RoundedCornerShape(3.dp))
            .padding(1.5.dp),
        verticalArrangement = Arrangement.spacedBy(0.5.dp)
    ) {
        for (r in 0..2) {
            Row(horizontalArrangement = Arrangement.spacedBy(0.5.dp)) {
                for (c in 0..2) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color(grid[r][c].rgb))
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniNetMap(
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    activeFace: FaceName,
    onFaceSelect: (FaceName) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161D2A))
            .border(1.dp, Color(0x0CFFFFFF), RoundedCornerShape(10.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "KÜP HARİTASI",
            color = Color(0xFF5A6A7D),
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
                Spacer(modifier = Modifier.width(24.dp))
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
                Spacer(modifier = Modifier.width(24.dp))
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
        containerColor = Color(0xFF131A26),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Küp Tasarımcısı",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Renkleri boyayın veya taratın",
                        color = Color(0xFF5A6A7D),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { showJsonImportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2633),
                        contentColor = Color(0xFFAABBCC)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("JSON", fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Scan Card — compact
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF161D2A))
                    .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "Kamera ile Tarama",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "6 yüzü otomatik algılatın",
                        color = Color(0xFF5A6A7D),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onStartScanWizard,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF448AFF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Tara", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Face selector tabs — short labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaceName.values().forEach { face ->
                    val isSelected = face == activeFace
                    val centerColor = when (face) {
                        FaceName.U -> Color(0xFFFB8C00)
                        FaceName.D -> Color(0xFFE53935)
                        FaceName.L -> Color(0xFFFFEB3B)
                        FaceName.R -> Color(0xFFECEFF1)
                        FaceName.F -> Color(0xFF4CAF50)
                        FaceName.B -> Color(0xFF1E88E5)
                    }
                    val label = when (face) {
                        FaceName.U -> "Üst"
                        FaceName.D -> "Alt"
                        FaceName.L -> "Sol"
                        FaceName.R -> "Sağ"
                        FaceName.F -> "Ön"
                        FaceName.B -> "Arka"
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFF1E3050) else Color(0xFF161D2A))
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) Color(0xFF448AFF) else Color(0x0FFFFFFF),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { activeFace = face }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(centerColor)
                            )
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF8A99AD),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
                        appState.editorFaces = updated
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                MiniNetMap(
                    faces = appState.editorFaces,
                    activeFace = activeFace,
                    onFaceSelect = { activeFace = it }
                )
            }

            // Color Palette
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Boya Rengi",
                    color = Color(0xFF5A6A7D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(color.rgb))
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.5.dp,
                                    color = if (isSelected) Color.White else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { appState.selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(RoundedCornerShape(2.5.dp))
                                        .background(if (color == CubeColor.WHITE) Color.Black else Color.White)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E2633),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "İptal",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        appState.editorFaces = mapOf(
                            FaceName.U to Array(3) { Array(3) { CubeColor.ORANGE } },
                            FaceName.D to Array(3) { Array(3) { CubeColor.RED } },
                            FaceName.L to Array(3) { Array(3) { CubeColor.YELLOW } },
                            FaceName.R to Array(3) { Array(3) { CubeColor.WHITE } },
                            FaceName.F to Array(3) { Array(3) { CubeColor.GREEN } },
                            FaceName.B to Array(3) { Array(3) { CubeColor.BLUE } }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A1519),
                        contentColor = Color(0xFFFF453A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text(
                        text = "Temizle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        val success = cubeState.setCustomState(appState.editorFaces)
                        if (success) {
                            onDismiss()
                            appState.activeSolution = null
                            appState.errorMessage = null
                        } else {
                            appState.errorMessage = "Geçersiz küp tasarımı!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8A00),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1.1f).height(38.dp)
                ) {
                    Text(
                        text = "Uygula",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }

    // JSON Import Dialog
    if (showJsonImportDialog) {
        AlertDialog(
            onDismissRequest = { showJsonImportDialog = false },
            title = {
                Text(
                    "JSON İçe Aktar",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Küp JSON durumunu yapıştırın:",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        maxLines = 2
                    )
                    OutlinedTextField(
                        value = jsonInputText,
                        onValueChange = { jsonInputText = it },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF8A00),
                            unfocusedBorderColor = Color(0x33FFFFFF)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 10.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = parseDetectedState(jsonInputText)
                        if (parsed != null) {
                            appState.editorFaces = parsed
                            showJsonImportDialog = false
                            jsonInputText = ""
                        } else {
                            appState.errorMessage = "Hatalı JSON formatı!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Aktar", color = Color.White, fontSize = 11.sp, maxLines = 1)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showJsonImportDialog = false
                        jsonInputText = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2633)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("İptal", color = Color.White, fontSize = 11.sp, maxLines = 1)
                }
            },
            containerColor = Color(0xFF1E2633),
            shape = RoundedCornerShape(16.dp)
        )
    }
}