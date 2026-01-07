package com.nidoham.extragram.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.pow

/**
 * Telegram-specific semantic colors for chat and UI elements
 * These colors are separate from Material Design color roles and provide
 * app-specific meaning (e.g., chat bubbles, online indicators)
 */
data class TelegramColors(
    val chatBackground: Color,
    val chatBubbleOutgoing: Color,
    val chatBubbleIncoming: Color,
    val divider: Color,
    val success: Color,
    val warning: Color,
    val accent: Color,
    val onlineIndicator: Color,
    val readIndicator: Color,
    val unreadBadge: Color,
    val attachmentIcon: Color
)

/**
 * Extension property to access Telegram-specific colors based on current theme
 * Usage: MaterialTheme.colorScheme.telegram.chatBackground
 */
val ColorScheme.telegram: TelegramColors
    @Composable
    @ReadOnlyComposable
    get() = if (this.isLight()) {
        TelegramColors(
            chatBackground = LightBackground,
            chatBubbleOutgoing = LightPrimaryContainer,
            chatBubbleIncoming = LightSurfaceVariant,
            divider = TelegramDividerLight,
            success = TelegramGreen,
            warning = TelegramOrange,
            accent = TelegramBlue,
            onlineIndicator = TelegramGreen,
            readIndicator = TelegramBlue,
            unreadBadge = TelegramBlue,
            attachmentIcon = TelegramBlueDark
        )
    } else {
        TelegramColors(
            chatBackground = DarkBackground,
            chatBubbleOutgoing = DarkPrimaryContainer,
            chatBubbleIncoming = DarkSurfaceVariant,
            divider = TelegramDividerDark,
            success = TelegramGreen,
            warning = TelegramOrange,
            accent = DarkPrimary,
            onlineIndicator = TelegramGreen,
            readIndicator = DarkPrimary,
            unreadBadge = DarkPrimary,
            attachmentIcon = DarkPrimary
        )
    }

/**
 * Standard alpha values for consistent opacity throughout the app
 * Based on Material Design opacity guidelines
 */
object TelegramAlpha {
    const val DISABLED = 0.38f
    const val MEDIUM = 0.60f
    const val HIGH = 0.87f
    const val DIVIDER = 0.12f
    const val HOVER = 0.04f
    const val SELECTED = 0.08f
    const val PRESSED = 0.12f
    const val DRAG = 0.16f
}

/**
 * Standard elevation values for consistent depth and shadows
 * Aligned with Material Design 3 elevation system
 */
object TelegramElevation {
    val NONE = 0.dp
    val LEVEL1 = 1.dp
    val LEVEL2 = 3.dp
    val LEVEL3 = 6.dp
    val LEVEL4 = 8.dp
    val LEVEL5 = 12.dp
}

/**
 * Consistent spacing values for padding and margins
 * Uses 4dp baseline grid system
 */
object TelegramSpacing {
    val EXTRA_SMALL = 4.dp
    val SMALL = 8.dp
    val MEDIUM = 12.dp
    val LARGE = 16.dp
    val EXTRA_LARGE = 24.dp
    val XXL = 32.dp
    val XXXL = 48.dp
}

/**
 * Standard icon sizes for consistent visual hierarchy
 */
object TelegramIconSize {
    val EXTRA_SMALL = 12.dp
    val SMALL = 16.dp
    val MEDIUM = 24.dp
    val LARGE = 32.dp
    val EXTRA_LARGE = 48.dp
    val HUGE = 64.dp
}

/**
 * Avatar sizes for user profile pictures
 */
object TelegramAvatarSize {
    val TINY = 24.dp
    val SMALL = 32.dp
    val MEDIUM = 40.dp
    val LARGE = 56.dp
    val EXTRA_LARGE = 72.dp
    val PROFILE = 100.dp
    val HERO = 140.dp
}

/**
 * Animation duration constants in milliseconds
 * Aligned with Material Design motion guidelines
 */
object TelegramAnimation {
    const val INSTANT = 50
    const val FAST = 150
    const val NORMAL = 300
    const val SLOW = 450
    const val VERY_SLOW = 600
}

// ========== Extension Functions ==========

/**
 * Creates a new color with the specified alpha value
 * @param alpha Alpha value between 0f (fully transparent) and 1f (fully opaque)
 * @return New color with applied alpha
 */
fun Color.withAlpha(alpha: Float): Color {
    return this.copy(alpha = alpha.coerceIn(0f, 1f))
}

/**
 * Checks if the current color scheme is dark themed
 * Uses luminance calculation for accurate detection
 */
fun ColorScheme.isDark(): Boolean {
    return this.background.luminance() < 0.5f
}

/**
 * Checks if the current color scheme is light themed
 * Uses luminance calculation for accurate detection
 */
fun ColorScheme.isLight(): Boolean {
    return this.background.luminance() >= 0.5f
}

/**
 * Determines appropriate text color for a given background color
 * Ensures sufficient contrast for readability (WCAG compliant)
 *
 * @param backgroundColor The background color to check against
 * @return Color.Black for light backgrounds, Color.White for dark backgrounds
 */
@Composable
@ReadOnlyComposable
fun getTextColorForBackground(backgroundColor: Color): Color {
    val luminance = backgroundColor.luminance()
    return if (luminance > 0.5f) {
        Color.Black.copy(alpha = 0.87f) // High emphasis on light backgrounds
    } else {
        Color.White.copy(alpha = 0.87f) // High emphasis on dark backgrounds
    }
}

/**
 * Calculates the relative luminance of a color
 * Based on WCAG 2.1 specification for contrast calculations
 *
 * @return Float value between 0 (darkest) and 1 (lightest)
 */
fun Color.luminance(): Float {
    // Convert sRGB to linear RGB
    val r = if (red <= 0.03928f) {
        red / 12.92f
    } else {
        ((red + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    val g = if (green <= 0.03928f) {
        green / 12.92f
    } else {
        ((green + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    val b = if (blue <= 0.03928f) {
        blue / 12.92f
    } else {
        ((blue + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

    // Calculate relative luminance
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

/**
 * Calculates contrast ratio between two colors
 * Useful for accessibility checking
 *
 * @param other The color to compare against
 * @return Contrast ratio (1:1 to 21:1)
 */
fun Color.contrastRatio(other: Color): Float {
    val l1 = this.luminance()
    val l2 = other.luminance()
    val lighter = maxOf(l1, l2)
    val darker = minOf(l1, l2)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/**
 * Checks if the color provides sufficient contrast for normal text (WCAG AA)
 * Minimum ratio: 4.5:1
 *
 * @param backgroundColor Background color to check against
 * @return true if contrast is sufficient, false otherwise
 */
fun Color.hasGoodContrastWith(backgroundColor: Color): Boolean {
    return this.contrastRatio(backgroundColor) >= 4.5f
}