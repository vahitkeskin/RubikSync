package com.vahitkeskin.rubiksync.utils

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role
import com.vahitkeskin.rubiksync.currentTimeMillis

/**
 * A utility object that manages the timing threshold for click actions.
 * It ensures that click-spamming or rapid double-tapping doesn't trigger
 * duplicate actions (e.g. launching the same screen multiple times).
 */
object ClickDebouncer {
    private var lastClickTime = 0L
    private const val DEFAULT_DEBOUNCE_INTERVAL_MS = 600L

    /**
     * Checks if the required time interval has elapsed since the last recorded click.
     * If yes, updates the timestamp and returns true. Otherwise, returns false.
     *
     * @param intervalMs The debounce time window in milliseconds.
     */
    fun canClick(intervalMs: Long = DEFAULT_DEBOUNCE_INTERVAL_MS): Boolean {
        val currentTime = currentTimeMillis()
        val elapsedTime = currentTime - lastClickTime
        // Detect system clock jumps/rollbacks by checking if elapsed is negative
        if (elapsedTime < 0 || elapsedTime >= intervalMs) {
            lastClickTime = currentTime
            return true
        }
        return false
    }
}

/**
 * Returns a throttled version of the given click lambda.
 * Subsequent invocations within [debounceIntervalMs] will be ignored.
 *
 * Example usage:
 * ```
 * Button(onClick = { navController.navigate("settings") }.safe()) { ... }
 * ```
 *
 * @param debounceIntervalMs The time window in milliseconds during which clicks are throttled.
 */
fun (() -> Unit).safe(debounceIntervalMs: Long = 600L): () -> Unit {
    return {
        if (ClickDebouncer.canClick(debounceIntervalMs)) {
            this()
        }
    }
}

/**
 * A Modifier extension that prevents double-clicking by debouncing the click event.
 * Uses Compose standard [clickable] under the hood.
 *
 * @param debounceIntervalMs The time window in milliseconds during which clicks are throttled.
 * @param enabled Controls the enabled state of the click handler.
 * @param onClickLabel An optional accessibility label for screen readers.
 * @param role The type of user interface element (e.g. Button, RadioButton, etc.).
 * @param interactionSource Custom [MutableInteractionSource] to dispatch interactions.
 * @param indication Custom [Indication] to draw visual feedback (e.g. ripple).
 * @param onClick Will be called when the user clicks the element, throttled by [debounceIntervalMs].
 */
fun Modifier.clickableSingle(
    debounceIntervalMs: Long = 600L,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = composed {
    val localInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val localIndication = if (indication == null && interactionSource == null) {
        LocalIndication.current
    } else {
        indication
    }
    Modifier.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        interactionSource = localInteractionSource,
        indication = localIndication,
        onClick = onClick.safe(debounceIntervalMs)
    )
}

/**
 * A Modifier extension that prevents double-clicking without showing a visual ripple indication.
 *
 * @param debounceIntervalMs The time window in milliseconds during which clicks are throttled.
 * @param enabled Controls the enabled state of the click handler.
 * @param onClickLabel An optional accessibility label for screen readers.
 * @param role The type of user interface element.
 * @param onClick Will be called when the user clicks the element, throttled by [debounceIntervalMs].
 */
fun Modifier.clickableSingleNoIndication(
    debounceIntervalMs: Long = 600L,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    Modifier.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick.safe(debounceIntervalMs)
    )
}

/**
 * A Modifier extension for [combinedClickable] that debounces only the short click action,
 * preventing rapid navigation triggers while leaving long click events untouched.
 *
 * @param debounceIntervalMs The time window in milliseconds during which click events are throttled.
 * @param enabled Controls the enabled state of the click handler.
 * @param onClickLabel An optional accessibility label for screen readers.
 * @param role The type of user interface element.
 * @param onLongClickLabel An optional accessibility label for long click action.
 * @param onLongClick Will be called when the user long-clicks the element.
 * @param onDoubleClick Will be called when the user double-clicks the element.
 * @param interactionSource Custom [MutableInteractionSource] to dispatch interactions.
 * @param indication Custom [Indication] to draw visual feedback (e.g. ripple).
 * @param onClick Will be called when the user short-clicks the element, throttled by [debounceIntervalMs].
 */
fun Modifier.combinedClickableSingle(
    debounceIntervalMs: Long = 600L,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    onClick: () -> Unit
): Modifier = composed {
    val localInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val localIndication = if (indication == null && interactionSource == null) {
        LocalIndication.current
    } else {
        indication
    }
    Modifier.combinedClickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        interactionSource = localInteractionSource,
        indication = localIndication,
        onClick = onClick.safe(debounceIntervalMs)
    )
}
