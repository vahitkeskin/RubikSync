package com.vahitkeskin.rubiksync

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Professional Android Instrumented UI Test Suite for RubikSync.
 *
 * This test suite runs directly on connected physical devices or emulators,
 * launching the real MainActivity and interacting with the UI like a human user.
 *
 * --- HOW TO RUN ---
 * Ensure an emulator is running or a physical device is connected, then execute:
 *    ./gradlew :androidApp:connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class RubikAndroidUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testAppLaunchAndInteractions() {
        // 1. Wait for Compose initialization and verify the app title is displayed on splash screen
        composeTestRule.onNodeWithText("RubikSync", substring = true).assertIsDisplayed()

        // 2. Wait for the splash screen duration (around 3.3s total) to transition to the Dashboard
        // We use composeTestRule's idle synchronization or a small delay
        composeTestRule.waitForIdle()
        Thread.sleep(4000)

        // 3. Verify Dashboard components are rendered:
        // We look for tab titles which are always displayed: "Hamleler" or "Moves" depending on default language
        val movesTab = try {
            composeTestRule.onNodeWithText("HAMLELER")
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("MOVES")
        }
        movesTab.assertIsDisplayed()

        // 4. Act: Click on the Actions tab to show solve/scramble buttons
        val actionsTab = try {
            composeTestRule.onNodeWithText("AKSİYONLAR")
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("ACTIONS")
        }
        actionsTab.assertIsDisplayed()
        actionsTab.performClick()

        // 5. Verify the Scramble and Reset buttons are displayed under the actions tab
        val scrambleButton = try {
            composeTestRule.onNodeWithText("KARIŞTIR")
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("SCRAMBLE")
        }
        scrambleButton.assertIsDisplayed()

        // 6. Act: Click on the AI tab to show design/solve buttons
        val aiTab = try {
            composeTestRule.onNodeWithText("YAPAY ZEKA")
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("AI")
        }
        aiTab.assertIsDisplayed()
        aiTab.performClick()

        // 7. Verify the design and solve buttons are visible
        val designButton = try {
            composeTestRule.onNodeWithText("TASARLA")
        } catch (_: AssertionError) {
            composeTestRule.onNodeWithText("DESIGN")
        }
        designButton.assertIsDisplayed()
    }
}
