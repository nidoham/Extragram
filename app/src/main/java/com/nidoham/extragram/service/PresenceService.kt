package com.nidoham.extragram.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.nidoham.extragram.R
import timber.log.Timber

/**
 * Foreground service to manage user presence status (online/offline/away).
 *
 * Features:
 * - Real-time presence updates using Firebase Realtime Database
 * - Automatic offline status on app close
 * - "Last seen" timestamp tracking
 * - Typing indicators support
 */
class PresenceService : Service() {

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    private var userId: String? = null
    private val presenceRef by lazy {
        userId?.let { database.getReference("presence/$it") }
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            setupPresenceTracking()
            startForeground(NOTIFICATION_ID, createNotification())
            Timber.d("PresenceService created for user: $userId")
        } else {
            Timber.w("PresenceService started without logged-in user")
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (userId == null) {
            Timber.w("No user logged in, stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        return START_STICKY // Service will restart if killed
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        setUserOffline()
        Timber.d("PresenceService destroyed")
    }

    /**
     * Setup Firebase Realtime Database presence tracking
     */
    private fun setupPresenceTracking() {
        val userId = this.userId ?: return

        // Create presence reference
        val presenceData = mapOf(
            "status" to "online",
            "lastSeen" to ServerValue.TIMESTAMP
        )

        // Set user online
        presenceRef?.setValue(presenceData)
            ?.addOnSuccessListener {
                Timber.d("User presence set to online")
                setupDisconnectHandler()
            }
            ?.addOnFailureListener { e ->
                Timber.e(e, "Failed to set presence")
            }

        // Monitor connection state
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Timber.d("Connected to Firebase")
                    presenceRef?.setValue(presenceData)
                    setupDisconnectHandler()
                } else {
                    Timber.d("Disconnected from Firebase")
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Timber.e("Connection monitoring error: ${error.message}")
            }
        })
    }

    /**
     * Setup automatic offline status on disconnect
     */
    private fun setupDisconnectHandler() {
        presenceRef?.onDisconnect()?.setValue(
            mapOf(
                "status" to "offline",
                "lastSeen" to ServerValue.TIMESTAMP
            )
        )?.addOnSuccessListener {
            Timber.d("Disconnect handler set")
        }
    }

    /**
     * Manually set user offline
     */
    private fun setUserOffline() {
        presenceRef?.setValue(
            mapOf(
                "status" to "offline",
                "lastSeen" to ServerValue.TIMESTAMP
            )
        )?.addOnSuccessListener {
            Timber.d("User status set to offline")
        }
    }

    /**
     * Create foreground notification (required for Android 8+)
     */
    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Extragram")
        .setContentText("Connected")
        .setSmallIcon(R.drawable.ic_notification)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true)
        .setShowWhen(false)
        .setCategory(NotificationCompat.CATEGORY_SERVICE)
        .build()

    /**
     * Public helper to update status (e.g., "typing", "recording")
     */
    fun updateStatus(status: String) {
        presenceRef?.child("status")?.setValue(status)
    }

    /**
     * Update typing indicator for specific chat
     */
    fun setTypingStatus(chatId: String, isTyping: Boolean) {
        val userId = this.userId ?: return
        database.getReference("typing/$chatId/$userId")
            .setValue(if (isTyping) ServerValue.TIMESTAMP else null)
    }
}