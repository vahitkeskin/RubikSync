package com.vahitkeskin.rubiksync.ui.components

import com.vahitkeskin.rubiksync.ui.state.*

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.cube.CubeRenderer
import com.vahitkeskin.rubiksync.cube.MoveType
import com.vahitkeskin.rubiksync.cube.RubikCubeState
import com.vahitkeskin.rubiksync.ui.state.RubikAppState
import com.vahitkeskin.rubiksync.ui.state.RubikTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreen(
    appState: RubikAppState,
    modifier: Modifier = Modifier
) {
    // 1. Separate state for the preview cube
    val splashCubeState = remember { RubikCubeState() }

    // 2. Camera angles for the orbit animation
    var yaw by remember { mutableStateOf(-0.55f) }
    var pitch by remember { mutableStateOf(0.40f) }

    // 3. Progress of the loading bar
    var progress by remember { mutableStateOf(0f) }

    // 4. Logo visibility state for fade-in effect
    var isLogoVisible by remember { mutableStateOf(false) }

    // Automatic moves for the cube
    LaunchedEffect(Unit) {
        // Scramble slightly at start
        repeat(5) {
            val nextMove = MoveType.values().random()
            splashCubeState.executeMove(nextMove, skipAnimation = true)
        }
        
        // Continuously turn layers
        while (true) {
            delay(200)
            val nextMove = MoveType.values().random()
            splashCubeState.executeMove(nextMove)
        }
    }

    // Camera orbit loop
    LaunchedEffect(Unit) {
        val startFrameTime = withFrameMillis { it }
        while (true) {
            val now = withFrameMillis { it }
            val elapsedSeconds = (now - startFrameTime) / 1000f
            yaw = -0.55f + elapsedSeconds * 0.35f // slowly rotate horizontally
            pitch = 0.40f + 0.12f * sin(elapsedSeconds * 0.7f) // gently bob up/down
        }
    }

    // Loader progress and auto dismiss
    LaunchedEffect(Unit) {
        delay(150)
        isLogoVisible = true

        val durationMs = 3000f
        val startTime = withFrameMillis { it }
        var elapsed = 0f
        
        while (elapsed < durationMs) {
            val now = withFrameMillis { it }
            elapsed = (now - startTime).toFloat()
            progress = (elapsed / durationMs).coerceIn(0f, 1f)
        }
        
        progress = 1f
        delay(300) // small pause at 100%
        appState.showSplashScreen = false
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient radial flare behind the cube
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RubikTheme.colors.glowOrange,
                            RubikTheme.colors.glowBlue,
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        // Skip / Entry Button removed since Giriş Yap is not needed

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 3D Rotating Cube Canvas
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val renderer = CubeRenderer(
                        state = splashCubeState,
                        yaw = yaw,
                        pitch = pitch,
                        cameraDistance = 7.2f
                    )
                    renderer.draw(this, size.width, size.height)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Logo & Titles
            AnimatedVisibility(
                visible = isLogoVisible,
                enter = fadeIn(tween(1000)) + scaleIn(tween(1000, delayMillis = 100)),
                exit = fadeOut(tween(500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Title with orange dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color_FFFF8A00, Color_FFFF5252)
                                    )
                                )
                        )
                        Text(
                            text = appState.strings.appTitle,
                            color = RubikTheme.colors.textPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 6.sp
                        )
                    }

                    Text(
                        text = appState.strings.splashSubtitle,
                        color = RubikTheme.colors.textSecondary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Sleek animated horizontal progress loader
            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(RubikTheme.colors.borderSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color_FFFF8A00,
                                    Color_FFFF5252
                                )
                            )
                        )
                )
            }
        }
    }
}
