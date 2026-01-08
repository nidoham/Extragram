package com.nidoham.extragram

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.nidoham.extragram.service.PresenceService
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Production-ready Application class for Extragram.
 *
 * Features:
 * - Firebase initialization
 * - WorkManager setup for background sync
 * - Notification channels (Android 8+)
 * - Presence management via ProcessLifecycleOwner
 * - FCM token retrieval
 * - Timber logging (debug builds only)
 */
@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.work.HiltWorkerFactory

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Logging (Debug only)
        initializeLogging()

        // 2. Initialize Firebase
        initializeFirebase()

        // 3. Create Notification Channels
        createNotificationChannels()

        // 4. Initialize WorkManager
        initializeWorkManager()

        // 5. Setup Global Lifecycle Observer for Presence
        setupLifecycleObserver()

        // 6. Retrieve FCM Token (if user logged in)
        retrieveFcmToken()

        Timber.d("Extragram initialized successfully")
    }

    /**
     * WorkManager configuration with Hilt integration
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

    /**
     * Initialize Timber for logging (Debug builds only)
     */
    private fun initializeLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.tag("Extragram")
        }
    }

    /**
     * Initialize Firebase services
     */
    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)
            Timber.d("Firebase initialized")
        } catch (e: Exception) {
            Timber.e(e, "Firebase initialization error")
        }
    }

    /**
     * Create notification channels for Android 8.0+
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Messages Channel (High Priority)
            val messagesChannel = NotificationChannel(
                "chat_messages",
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new messages"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            // Calls Channel (Max Priority)
            val callsChannel = NotificationChannel(
                "voice_calls",
                "Voice & Video Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming call notifications"
                enableVibration(true)
                enableLights(true)
                setBypassDnd(true)
            }

            // Service Channel (Low Priority)
            val serviceChannel = NotificationChannel(
                "service_channel",
                "Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the app running for presence status"
                setShowBadge(false)
            }

            // Groups & Mentions (High Priority)
            val groupChannel = NotificationChannel(
                "group_messages",
                "Group Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for group messages and mentions"
                enableVibration(true)
            }

            notificationManager?.apply {
                createNotificationChannel(messagesChannel)
                createNotificationChannel(callsChannel)
                createNotificationChannel(serviceChannel)
                createNotificationChannel(groupChannel)
            }

            Timber.d("Notification channels created")
        }
    }

    /**
     * Initialize WorkManager for background tasks
     */
    private fun initializeWorkManager() {
        try {
            WorkManager.initialize(this, workManagerConfiguration)

            // Schedule periodic message sync (every 15 minutes when connected)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<MessageSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                    "message_sync",
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncWorkRequest
                )

            Timber.d("WorkManager initialized with message sync")
        } catch (e: Exception) {
            Timber.e(e, "WorkManager initialization error")
        }
    }

    /**
     * Setup ProcessLifecycleOwner to manage presence service
     */
    private fun setupLifecycleObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                // App enters foreground
                Timber.d("App entered foreground")
                startPresenceService()
            }

            override fun onStop(owner: LifecycleOwner) {
                // App goes to background
                Timber.d("App entered background")
                val userId = firebaseAuth.currentUser?.uid
                if (userId != null) {
                    stopPresenceService()
                }
            }
        })
    }

    /**
     * Start the PresenceService if user is logged in
     */
    private fun startPresenceService() {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val intent = Intent(this, PresenceService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                Timber.d("PresenceService started")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error starting PresenceService")
        }
    }

    /**
     * Stop the PresenceService
     */
    private fun stopPresenceService() {
        try {
            val intent = Intent(this, PresenceService::class.java)
            stopService(intent)
            Timber.d("PresenceService stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping PresenceService")
        }
    }

    /**
     * Retrieve and log FCM token for push notifications
     */
    private fun retrieveFcmToken() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Timber.d("FCM Token: $token")
                    // TODO: Send token to your backend server
                    saveFcmTokenToFirestore(currentUser.uid, token)
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Failed to retrieve FCM token")
                }
        }
    }

    /**
     * Save FCM token to Firestore for targeted notifications
     */
    private fun saveFcmTokenToFirestore(userId: String, token: String) {
        // TODO: Implement Firestore logic
        // Example:
        // FirebaseFirestore.getInstance()
        //     .collection("users")
        //     .document(userId)
        //     .update("fcmToken", token)
    }

    /**
     * Public helper to restart presence service (e.g., after login)
     */
    fun restartPresenceService() {
        stopPresenceService()
        startPresenceService()
    }
}

/**
 * Placeholder for MessageSyncWorker
 * Create this in a separate file: com/nidoham/extragram/worker/MessageSyncWorker.kt
 */
// @HiltWorker
// class MessageSyncWorker @AssistedInject constructor(
//     @Assisted appContext: Context,
//     @Assisted workerParams: WorkerParameters
// ) : CoroutineWorker(appContext, workerParams) {
//     override suspend fun doWork(): Result {
//         // Sync messages logic
//         return Result.success()
//     }
// }