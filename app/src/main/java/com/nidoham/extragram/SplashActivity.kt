package com.nidoham.extragram

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.google.firebase.auth.FirebaseAuth
import com.nidoham.extragram.ui.theme.ExtragramTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ExtragramTheme {
                SplashScreenContent {
                    navigateNext()
                }
            }
        }
    }

    private fun navigateNext() {
        val target = if (FirebaseAuth.getInstance().currentUser != null) {
            MainActivity::class.java
        } else {
            OnboardActivity::class.java
        }

        startActivity(
            Intent(this, target).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )

        // smooth activity fade (system handles it)
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        finish()
    }
}

@Composable
private fun SplashScreenContent(
    onAnimationFinished: () -> Unit
) {
    // animation state
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(-45f) }

    LaunchedEffect(Unit) {

        launch {
            alpha.animateTo(
                1f,
                tween(400, easing = FastOutSlowInEasing)
            )
        }

        launch {
            delay(100)
            rotation.animateTo(
                0f,
                tween(900, easing = FastOutSlowInEasing)
            )
        }

        launch {
            delay(100)
            scale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        // splash visible time
        delay(1800)

        // ❌ no fade-out here (avoids blank screen)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .alpha(alpha.value),
        contentAlignment = Alignment.Center
    ) {
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
