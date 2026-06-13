package com.vahitkeskin.rubiksync

import com.vahitkeskin.rubiksync.ui.strings.AppLanguage
import com.vahitkeskin.rubiksync.ui.strings.AppStringsMap
import com.vahitkeskin.rubiksync.ui.strings.EnStrings
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
        // Resolve localized strings based on device's active system language
        val sysCode = getSystemLanguageCode().lowercase()
        val currentLang = AppLanguage.entries.find { it.code == sysCode } ?: AppLanguage.EN
        val strings = AppStringsMap[currentLang] ?: EnStrings

        // 1. Wait for theme initialization and verify the app title is displayed on the splash screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText(strings.appTitle, substring = true, ignoreCase = true).assertIsDisplayed()
                true
            } catch (_: AssertionError) {
                false
            }
        }

        // 2. Wait for the splash screen duration (around 3.3s total) to transition to the Dashboard
        composeTestRule.waitForIdle()
        Thread.sleep(4000)

        // 3. Skip tutorial/showcase if active to prevent overlay from intercepting or obscuring elements
        try {
            composeTestRule.onNodeWithText(strings.skipShowcase).performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(1500) // Allow tutorial exit animation to settle fully
        } catch (_: AssertionError) {
            // Showcase already completed or not active
        }

        // 4. Verify Dashboard components are rendered (e.g., Moves tab)
        composeTestRule.onNodeWithText(strings.tabMoves).assertIsDisplayed()

        // 5. Act: Click on the Actions tab to show solve/scramble buttons
        val actionsTab = composeTestRule.onNodeWithText(strings.tabActions)
        actionsTab.assertIsDisplayed()
        actionsTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1500) // Allow page scroll animation to settle fully

        // 6. Verify the Scramble button is displayed under the actions tab
        composeTestRule.onNodeWithText(strings.scrambleButton).assertIsDisplayed()

        // 7. Act: Click on the AI tab to show design/solve buttons
        val aiTab = composeTestRule.onNodeWithText(strings.tabAI)
        aiTab.assertIsDisplayed()
        aiTab.performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1500) // Allow page scroll animation to settle fully

        // 8. Verify the design button is visible
        composeTestRule.onNodeWithText(strings.designButton).assertIsDisplayed()
    }
}
