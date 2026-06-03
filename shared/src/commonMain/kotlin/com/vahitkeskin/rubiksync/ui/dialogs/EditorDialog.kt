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
import androidx.compose.ui.graphics.Brush
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
                color = if (isActive) Color(0xFF448AFF) else Color(0x15FFFFFF),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onFaceSelect(face) }
            .background(
                if (isActive) Color(0x0C448AFF) else Color(0x06000000),
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
    faces: Map<FaceName, Array<Array<CubeColor>>>,
    activeFace: FaceName,
    onFaceSelect: (FaceName) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF111827))
            .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "KÜP HARİTASI",
            color = Color(0xFF4A5568),
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
        containerColor = Color(0xFF0F1724),
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
                                listOf(Color(0xFFFF8A00), Color(0xFF448AFF))
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎨 Küp Tasarımcısı",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Renkleri boyayın veya kamera ile taratın",
                        color = Color(0xFF4A5568),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = { showJsonImportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF141B28),
                        contentColor = Color(0xFF8A99AD)
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
                            listOf(Color(0xFF0F1A2E), Color(0xFF111D32))
                        )
                    )
                    .border(1.dp, Color(0x0AFFFFFF), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "📸 Kamera ile Tarama",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "6 yüzü otomatik algılatın",
                        color = Color(0xFF4A5568),
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
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Tara", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Color(0xFF1A2D4D) else Color(0xFF111827)
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.5.dp,
                                color = if (isSelected) Color(0xFF448AFF) else Color(0x0AFFFFFF),
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
                                color = if (isSelected) Color.White else Color(0xFF6B7A8D),
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
                        appState.editorFaces = updated
                    }
                )

                Spacer(modifier = Modifier.width(14.dp))

                MiniNetMap(
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
                    CubeColor.ORANGE -> "Turuncu"
                    CubeColor.RED -> "Kırmızı"
                    CubeColor.YELLOW -> "Sarı"
                    CubeColor.WHITE -> "Beyaz"
                    CubeColor.GREEN -> "Yeşil"
                    CubeColor.BLUE -> "Mavi"
                    else -> ""
                }

                Text(
                    text = "🖌 Boya Rengi: $colorName",
                    color = Color(0xFF6B7A8D),
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
                                    color = if (isSelected) Color.White else Color(0x1AFFFFFF),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { appState.selectedColor = color },
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
                        containerColor = Color(0xFF141B28),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(
                        text = "✕ İptal",
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
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(
                        text = "🗑 Temizle",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        val success = cubeState.setCustomState(appState.editorFaces)
                        if (success) {
                            appState.manualMoves.clear()
                            appState.saveCurrentState()
                            onDismiss()
                            appState.activeSolution = null
                            appState.errorMessage = null
                            appState.successMessage = "Küp durumu uygulandı! ✅"
                        } else {
                            appState.errorMessage = "Geçersiz küp tasarımı!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF8A00),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1.1f).height(42.dp)
                ) {
                    Text(
                        text = "✓ Uygula",
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
                        "📋 JSON İçe Aktar",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Küp JSON durumunu yapıştırın:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                        OutlinedTextField(
                            value = jsonInputText,
                            onValueChange = { jsonInputText = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF8A00),
                                unfocusedBorderColor = Color(0xFF2A3548)
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
                                appState.editorFaces = parsed
                                showJsonImportDialog = false
                                jsonInputText = ""
                                appState.successMessage = "JSON başarıyla içe aktarıldı!"
                            } else {
                                appState.errorMessage = "Hatalı JSON formatı!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("✓ Aktar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showJsonImportDialog = false
                            jsonInputText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF141B28)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("İptal", color = Color.White, fontSize = 12.sp, maxLines = 1)
                    }
                },
                containerColor = Color(0xFF141B28),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}