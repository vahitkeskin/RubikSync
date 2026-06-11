package com.vahitkeskin.rubiksync.ui.screens.readme

import com.vahitkeskin.rubiksync.ui.state.*
import com.vahitkeskin.rubiksync.BindBackHandler
import com.vahitkeskin.rubiksync.ui.components.RubikToolbar
import com.vahitkeskin.rubiksync.PlatformWebView
import com.vahitkeskin.rubiksync.utils.RubikConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ReadmeScreen(
    appState: RubikAppState,
    isDarkTheme: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    BindBackHandler(enabled = true) {
        navController.popBackStack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RubikTheme.colors.bgGradient1,
                        RubikTheme.colors.bgGradient2,
                        RubikTheme.colors.bgGradient3,
                        RubikTheme.colors.bgGradient4
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
        ) {
            RubikToolbar(
                title = appState.strings.readmeScreenTitle,
                onBackClick = { navController.popBackStack() },
                titleFontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )

            PlatformWebView(
                url = RubikConstants.README_URL,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp)
            )
        }
    }
}
