package com.nidoham.extragram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.nidoham.extra.user.AccountStatus
import com.nidoham.extra.user.Presence
import com.nidoham.extra.user.UserInfo
import com.nidoham.extra.user.UserRepository
import com.nidoham.extragram.ui.theme.ExtragramTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Professional Google-Only Onboarding for ID-based Telegram Clone.
 * Simplified flow: One-tap Google Sign-In with automatic unique username generation.
 */
class OnboardActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository

    private val webClientId: String? by lazy {
        try {
            getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e("OnboardActivity", "Failed to load web client ID", e)
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository()

        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        setContent {
            ExtragramTheme {
                GoogleOnboardingScreen(
                    auth = auth,
                    userRepository = userRepository,
                    webClientId = webClientId,
                    onLoginSuccess = { navigateToMain() }
                )
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun GoogleOnboardingScreen(
    auth: FirebaseAuth,
    userRepository: UserRepository,
    webClientId: String?,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    /**
     * Syncs Google User with Firestore ID-based system.
     */
    suspend fun syncUserWithDatabase(firebaseUser: FirebaseUser) {
        try {
            val userId = firebaseUser.uid
            val existingUser = userRepository.getUser(userId)

            if (existingUser != null) {
                userRepository.updatePresence(userId, Presence.ONLINE)
            } else {
                val displayName = firebaseUser.displayName ?: "User"
                val nameParts = displayName.trim().split(" ", limit = 2)
                val firstName = nameParts.getOrElse(0) { "User" }
                val lastName = nameParts.getOrElse(1) { "" }.ifBlank { null }

                // Generate unique username
                val baseUsername = firebaseUser.email?.substringBefore("@") ?: "user_${userId.take(6)}"
                var uniqueUsername = baseUsername.replace(Regex("[^a-zA-Z0-9_]"), "").lowercase()

                if (!userRepository.isUsernameAvailable(uniqueUsername)) {
                    uniqueUsername += "_${userId.take(4)}"
                }

                val newUser = UserInfo(
                    id = userId,
                    username = uniqueUsername,
                    firstName = firstName,
                    lastName = lastName,
                    email = firebaseUser.email ?: "",
                    avatar = firebaseUser.photoUrl?.toString(),
                    presence = Presence.ONLINE,
                    lastActive = System.currentTimeMillis(),
                    accountStatus = AccountStatus.ACTIVE
                )

                val result = userRepository.registerUser(newUser)
                if (result.isFailure) {
                    errorMessage = "Profile creation failed: ${result.exceptionOrNull()?.message}"
                }
            }
        } catch (e: Exception) {
            errorMessage = "Sync error: ${e.message}"
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            scope.launch {
                isLoading = true
                errorMessage = null
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                authResult.user?.let { syncUserWithDatabase(it) }
                if (errorMessage == null) onLoginSuccess()
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = "Google Sign-In failed"
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF000000)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // App Branding
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Extragram",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Secure ID-based Messaging",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Error Display
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Google Sign-In Button
            Button(
                onClick = {
                    if (!isLoading) {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(webClientId ?: "")
                            .requestEmail()
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(client.signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF1A237E),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // You can add a Google Icon here if available in resources
                        Text(
                            text = "Continue with Google",
                            color = Color(0xFF1A237E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "By continuing, you agree to our Terms & Privacy Policy",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
