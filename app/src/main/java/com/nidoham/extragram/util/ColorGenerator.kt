package com.nidoham.extragram.util

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

/**
 * Utility class for generating random colors for avatars and UI elements
 */
object ColorGenerator {

    // Predefined Material Design colors for avatars
    private val avatarColors = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF03A9F4), // Light Blue
        Color(0xFF00BCD4), // Cyan
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFF8BC34A), // Light Green
        Color(0xFFCDDC39), // Lime
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFFC107), // Amber
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B), // Blue Grey
        Color(0xFFF44336), // Red
        Color(0xFF5CA7F5), // Telegram Blue
        Color(0xFFE53935), // Material Red
        Color(0xFF7B1FA2), // Purple Dark
        Color(0xFF1976D2), // Blue Dark
        Color(0xFF388E3C), // Green Dark
        Color(0xFFF57C00), // Orange Dark
    )

    /**
     * Get a random color from the predefined palette
     */
    fun getRandomColor(): Color {
        return avatarColors.random()
    }

    /**
     * Get a consistent color based on a string (e.g., username, name)
     * Same string will always return the same color
     */
    fun getColorForString(text: String): Color {
        val hash = text.hashCode()
        val index = hash.absoluteValue % avatarColors.size
        return avatarColors[index]
    }

    /**
     * Get a consistent color based on an ID
     * Same ID will always return the same color
     */
    fun getColorForId(id: Int): Color {
        val index = id.absoluteValue % avatarColors.size
        return avatarColors[index]
    }

    /**
     * Get a random color with custom seed for reproducibility
     */
    fun getRandomColorWithSeed(seed: Int): Color {
        val random = Random(seed)
        return avatarColors[random.nextInt(avatarColors.size)]
    }

    /**
     * Get a list of random colors
     */
    fun getRandomColors(count: Int): List<Color> {
        return List(count) { getRandomColor() }
    }

    /**
     * Get a list of unique random colors (no duplicates)
     */
    fun getUniqueRandomColors(count: Int): List<Color> {
        require(count <= avatarColors.size) {
            "Cannot generate more unique colors than available in palette"
        }
        return avatarColors.shuffled().take(count)
    }

    /**
     * Generate a completely random color (not from palette)
     */
    fun generateRandomRGBColor(): Color {
        return Color(
            red = Random.nextFloat(),
            green = Random.nextFloat(),
            blue = Random.nextFloat(),
            alpha = 1f
        )
    }

    /**
     * Generate a random pastel color
     */
    fun generateRandomPastelColor(): Color {
        val hue = Random.nextFloat() * 360f
        val saturation = 0.25f + Random.nextFloat() * 0.25f // 25-50%
        val lightness = 0.75f + Random.nextFloat() * 0.15f  // 75-90%
        return hslToColor(hue, saturation, lightness)
    }

    /**
     * Get all available avatar colors
     */
    fun getAllColors(): List<Color> {
        return avatarColors.toList()
    }

    /**
     * Convert HSL to RGB Color
     */
    private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
        val h = hue / 360f
        val s = saturation
        val l = lightness

        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h * 6f) % 2f - 1f))
        val m = l - c / 2f

        val (r, g, b) = when {
            h < 1f / 6f -> Triple(c, x, 0f)
            h < 2f / 6f -> Triple(x, c, 0f)
            h < 3f / 6f -> Triple(0f, c, x)
            h < 4f / 6f -> Triple(0f, x, c)
            h < 5f / 6f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            red = r + m,
            green = g + m,
            blue = b + m,
            alpha = 1f
        )
    }

    private val Int.absoluteValue: Int
        get() = if (this < 0) -this else this
}