package com.vahitkeskin.rubiksync

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vahitkeskin.rubiksync.persistence.AndroidRubikPersistence
import com.vahitkeskin.rubiksync.ui.strings.AppLanguage
import com.vahitkeskin.rubiksync.ui.strings.AppStringsMap
import com.vahitkeskin.rubiksync.ui.strings.EnStrings
import kotlinx.coroutines.runBlocking
import org.junit.Before
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

    @Before
    fun setUp() {
        // Reset showcase persistence before the test starts to guarantee it runs
        val context = ApplicationProvider.getApplicationContext<Context>()
        val persistence = AndroidRubikPersistence(context)
        runBlocking {
            persistence.saveShowcaseCompleted(false)
            persistence.saveEditorShowcaseCompleted(false)
            persistence.saveScannerShowcaseCompleted(false)
        }
    }

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

        // 3. Traverse through all showcase balloons step-by-step with natural delays and animations
        val showcaseSteps = listOf(
            strings.showcaseEditableText,
            strings.showcaseShakeToScrambleText,
            strings.showcaseSoundText,
            strings.showcaseSettingsText,
            strings.showcaseInteractiveCubeText,
            strings.showcaseMovesText,
            strings.showcaseScrambleText,
            strings.showcaseUndoText,
            strings.showcaseResetText,
            strings.showcaseDesignText,
            strings.showcaseSolveText
        )

        showcaseSteps.forEachIndexed { index, stepText ->
            // Wait for the specific showcase balloon text to be displayed
            composeTestRule.waitUntil(timeoutMillis = 8000) {
                try {
                    composeTestRule.onNodeWithText(stepText).assertIsDisplayed()
                    true
                } catch (_: AssertionError) {
                    false
                }
            }

            // Click the balloon text itself to dismiss and advance to the next step
            composeTestRule.onNodeWithText(stepText).performClick()
            composeTestRule.waitForIdle()

            // Wait for transitions and page scroll animations to fully settle
            // App has a 1050ms delay inside advanceShowcase, and pager scroll animation is 1200ms.
            // 2000ms sleep is extremely safe and natural.
            Thread.sleep(2000)
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
