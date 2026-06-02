package com.vahitkeskin.rubiksync.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeColor
import com.vahitkeskin.rubiksync.cube.FaceName
import com.vahitkeskin.rubiksync.ui.components.FaceGrid
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.utils.parseDetectedState

@Composable
fun EditorDialog(
    show: Boolean,
    appState: RubikAppState,
    onDismiss: () -> Unit,
    onStartScanWizard: () -> Unit
) {
    if (!show) return

    var showJsonImportDialog by remember { mutableStateOf(false) }
    var jsonInputText by remember { mutableStateOf("") }

    val cubeState = appState.cubeState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60A0D14))
            .clickable(enabled = false) {}
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2633)),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f)
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Küp Tasarımcısı",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showJsonImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.LightGray),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("JSON Yükle", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Küpünüzün yüzey renklerini boyayın veya fotoğraflardan algılayın",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                // Fotoğraf Tarama Sihirbazı Başlatma Kartı
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "📸 Fotoğraf ile Hızlı Tarama",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Küpünüzün 6 yüzünü sırayla fotoğraflayıp saniyeler içinde renkleri otomatik algılatın.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                        
                        Button(
                            onClick = onStartScanWizard,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF448AFF), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Taramayı Başlat", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        FaceGrid(FaceName.U, appState.editorFaces) { face, row, col ->
                            val updated = appState.editorFaces.toMutableMap()
                            val grid = updated[face]!!.map { it.copyOf() }.toTypedArray()
                            grid[row][col] = appState.selectedColor
                            updated[face] = grid
                            appState.editorFaces = updated
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FaceGrid(FaceName.L, appState.editorFaces) { face, row, col ->
                            val updated = appState.editorFaces.toMutableMap()
                            val grid = updated[face]!!.map { it.copyOf() }.toTypedArray()
                            grid[row][col] = appState.selectedColor
                            updated[face] = grid
                            appState.editorFaces = updated
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FaceGrid(FaceName.F, appState.editorFaces) { face, row, col ->
                            val updated = appState.editorFaces.toMutableMap()
                            val grid = updated[face]!!.map { it.copyOf() }.toTypedArray()
                            grid[row][col] = appState.selectedColor
                            updated[face] = grid
                            appState.editorFaces = updated
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FaceGrid(FaceName.R, appState.editorFaces) { face, row, col ->
                            val updated = appState.editorFaces.toMutableMap()
                            val grid = updated[face]!!.map { it.copyOf() }.toTypedArray()
                            grid[row][col] = appState.selectedColor
                            updated[face] = grid
                            appState.editorFaces = updated
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FaceGrid(FaceName.B, appState.editorFaces) { face, row, col ->
                            val updated = appState.editorFaces.toMutableMap()
                            val grid = updated[face]!!.map { it.copyOf() }.toTypedArray()
                            grid[row][col] = appState.selectedColor
                            updated[face] = grid
                            appState.editorFaces = updated
                        }
                    }

                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        FaceGrid(FaceName.D, appState.editorFaces) { face, row, col ->
                            val updated = appState.editorFaces.toMutableMap()
                            val grid = updated[face]!!.map { it.copyOf() }.toTypedArray()
                            grid[row][col] = appState.selectedColor
                            updated[face] = grid
                            appState.editorFaces = updated
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Renk Paleti", color = Color.LightGray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val paletteColors = listOf(
                            CubeColor.ORANGE, CubeColor.RED, CubeColor.YELLOW,
                            CubeColor.WHITE, CubeColor.GREEN, CubeColor.BLUE
                        )
                        paletteColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(color.rgb))
                                    .border(
                                        width = if (appState.selectedColor == color) 3.dp else 1.dp,
                                        color = if (appState.selectedColor == color) Color.White else Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { appState.selectedColor = color }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal")
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FF3B30), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Temizle")
                    }

                    Button(
                        onClick = {
                            val success = cubeState.setCustomState(appState.editorFaces)
                            if (success) {
                                onDismiss()
                                appState.activeSolution = null
                                appState.errorMessage = null
                            } else {
                                appState.errorMessage = "Geçersiz küp tasarımı! Lütfen tüm parçaları doğru renklendirdiğinizden emin olun."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Uygula")
                    }
                }
            }
        }

        if (showJsonImportDialog) {
            AlertDialog(
                onDismissRequest = { showJsonImportDialog = false },
                title = { Text("Küp Durumu İçe Aktar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Lütfen küp JSON durum metnini yapıştırın. Örnek:\n{\"U\":\"OOOOOOOOO\", \"D\":\"RRRRRRRRR\", \"L\":\"YYYYYYYYY\", \"R\":\"WWWWWWWWW\", \"F\":\"GGGGGGGGG\", \"B\":\"BBBBBBBBB\"}",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        OutlinedTextField(
                            value = jsonInputText,
                            onValueChange = { jsonInputText = it },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF8A00),
                                unfocusedBorderColor = Color(0x33FFFFFF)
                            )
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
                                appState.errorMessage = "Hatalı JSON formatı! Lütfen kontrol edip tekrar deneyin."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A00))
                    ) {
                        Text("Aktar", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            showJsonImportDialog = false
                            jsonInputText = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF))
                    ) {
                        Text("İptal", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E2633),
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
