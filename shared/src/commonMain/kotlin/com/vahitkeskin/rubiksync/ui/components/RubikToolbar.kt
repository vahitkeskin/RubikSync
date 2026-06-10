package com.vahitkeskin.rubiksync.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.rubiksync.ui.icons.ArrowBackIcon
import com.vahitkeskin.rubiksync.ui.state.RubikTheme

/**
 * A highly customizable, reusable top toolbar component for RubikSync screens.
 * Designed with modern premium aesthetics and clean architecture in mind.
 *
 * @param title The main title text of the toolbar.
 * @param modifier The modifier to apply to the toolbar container.
 * @param subtitle The optional subtitle text of the toolbar.
 * @param showBackButton Whether the back button should be visible on the left.
 * @param onBackClick Callback invoked when the back button is clicked.
 * @param titleFontSize The font size for the main title text.
 * @param subtitleFontSize The font size for the subtitle text.
 * @param rightContent Slot for placing custom components on the far right (e.g., action buttons).
 */
@Composable
fun RubikToolbar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showBackButton: Boolean = true,
    onBackClick: (() -> Unit)? = null,
    titleFontSize: TextUnit = 18.sp,
    subtitleFontSize: TextUnit = 10.sp,
    rightContent: (@Composable RowScope.() -> Unit)? = null
) {
    val textPrimary = RubikTheme.colors.textPrimary
    val textSecondary = RubikTheme.colors.textSecondary
    val bgSecondary = RubikTheme.colors.backgroundSecondary
    val cardBorder = RubikTheme.colors.cardBorder

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgSecondary)
                    .border(0.5.dp, cardBorder, RoundedCornerShape(12.dp))
                    .clickable { onBackClick?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ArrowBackIcon,
                    contentDescription = "Geri",
                    tint = textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = textPrimary,
                fontSize = titleFontSize,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = textSecondary,
                    fontSize = subtitleFontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (rightContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            rightContent()
        }
    }
}
