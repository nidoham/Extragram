package com.nidoham.extragram

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.nidoham.extragram.ui.theme.ExtragramTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash screen activity that displays app branding while initializing
 * Implements custom splash screen with smooth animations
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    companion object {
        private const val SPLASH_DISPLAY_DURATION = 1800L
        private const val FADE_OUT_DURATION = 300L
        private const val TOTAL_DURATION = SPLASH_DISPLAY_DURATION + FADE_OUT_DURATION + 100L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set transparent system bars for immersive experience
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            ExtragramTheme {
                SplashScreenContent(
                    onAnimationFinished = ::navigateToMainActivity
                )
            }
        }
    }

    /**
     * Navigate to main activity with smooth transition
     * Clears splash activity from back stack
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        // Apply fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

/**
 * Splash screen content with animated logo
 *
 * @param onAnimationFinished Callback invoked when animation completes
 */
@Composable
private fun SplashScreenContent(onAnimationFinished: () -> Unit) {
    val isDark = isSystemInDarkTheme()

    // Animation state holders
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(-45f) }
    val glowScale = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        // Launch all animations in parallel
        launch {
            // Background fade in
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            // Glow pulse effect for visual interest
            delay(200)
            glowScale.animateTo(
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1500,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }

        launch {
            // Icon rotation with smooth easing
            delay(100)
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                )
            )
        }

        launch {
            // Icon scale with spring bounce effect
            delay(100)
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // Hold the splash screen for visibility
        delay(1800L)

        // Fade out animation
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        )

        // Small delay before navigation
        delay(100L)
        onAnimationFinished()
    }

    // Render splash screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .alpha(alpha.value),
        contentAlignment = Alignment.Center
    ) {
        // App logo with animations applied
        Image(
            painter = painterResource(id = R.drawable.ic_launcher),
            contentDescription = "Extragram Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(160.dp)
                .scale(scale.value)
                .rotate(rotation.value)
        )
    }
}