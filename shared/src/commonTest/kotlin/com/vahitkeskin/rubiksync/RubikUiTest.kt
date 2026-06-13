package com.vahitkeskin.rubiksync

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.vahitkeskin.rubiksync.ui.state.PreviewRubikTheme
import kotlin.test.Test

/**
 * World-class Compose Multiplatform UI Test Suite for RubikSync.
 *
 * This test file verifies that components compile and render correctly using the Compose Multiplatform UI testing framework.
 *
 * --- HOW TO RUN UI TESTS ---
 *
 * Run from the command line:
 *    ./gradlew :shared:jvmTest --tests "com.vahitkeskin.rubiksync.RubikUiTest"
 */
@OptIn(ExperimentalTestApi::class)
class RubikUiTest {

    @Test
    fun testMockButtonRenderingAndClick() = runComposeUiTest {
        var clicked = false

        // Set the content with the premium app theme and a sample interactive button
        setContent {
            PreviewRubikTheme(isDark = true) {
                Button(onClick = { clicked = true }) {
                    Text("ÇÖZ / SOLVE")
                }
            }
        }

        // Assert that the text is rendered correctly on screen
        onNodeWithText("ÇÖZ / SOLVE").assertIsDisplayed()

        // Act: Perform a click interaction
        onNodeWithText("ÇÖZ / SOLVE").performClick()

        // Assert: Verify state update following the click action
        kotlin.test.assertTrue(clicked, "Clicking the button should invoke the registered callback and toggle state")
    }

    @Test
    fun testSimpleThemeSwitchRendering() = runComposeUiTest {
        setContent {
            PreviewRubikTheme(isDark = false) {
                Text("Tema Modu / Theme Mode: Açık / Light")
            }
        }

        onNodeWithText("Tema Modu / Theme Mode: Açık / Light").assertIsDisplayed()
    }
}
