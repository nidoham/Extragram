package com.nidoham.extragram.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Telegram-inspired shape system with rounded corners
 * Following Material Design 3 shape scale
 *
 * Shape Scale Usage:
 * - Extra Small: Chips, small buttons, text fields
 * - Small: Small cards, tooltips, snackbars
 * - Medium: Cards, dialogs, most UI components (default)
 * - Large: Bottom sheets, large cards, navigation drawers
 * - Extra Large: Modal sheets, full-screen dialogs
 */
val Shapes = Shapes(
    /**
     * Extra Small: 4dp corner radius
     * Used for: Small chips, compact buttons, badges
     */
    extraSmall = RoundedCornerShape(4.dp),

    /**
     * Small: 8dp corner radius
     * Used for: Small cards, tooltips, snackbars, text fields
     */
    small = RoundedCornerShape(8.dp),

    /**
     * Medium: 12dp corner radius (Default)
     * Used for: Cards, dialogs, most standard UI components
     * This is the most commonly used shape in the app
     */
    medium = RoundedCornerShape(12.dp),

    /**
     * Large: 16dp corner radius
     * Used for: Bottom sheets, large cards, navigation components
     */
    large = RoundedCornerShape(16.dp),

    /**
     * Extra Large: 20dp corner radius
     * Used for: Modal bottom sheets, full-screen dialogs
     */
    extraLarge = RoundedCornerShape(20.dp)
)

/**
 * Additional custom shapes for specific use cases
 */
object TelegramShapes {
    /**
     * Shape for chat bubbles - rounded on three corners
     * Use topStart variant for incoming messages
     */
    val ChatBubbleIncoming = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 12.dp,
        bottomEnd = 12.dp,
        bottomStart = 12.dp
    )

    /**
     * Shape for chat bubbles - rounded on three corners
     * Use topEnd variant for outgoing messages
     */
    val ChatBubbleOutgoing = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 4.dp,
        bottomEnd = 12.dp,
        bottomStart = 12.dp
    )

    /**
     * Fully rounded shape for circular elements
     */
    val Circle = RoundedCornerShape(50)

    /**
     * Semi-rounded shape for pills and tags
     */
    val Pill = RoundedCornerShape(100.dp)

    /**
     * Subtle rounding for containers
     */
    val Subtle = RoundedCornerShape(6.dp)
}