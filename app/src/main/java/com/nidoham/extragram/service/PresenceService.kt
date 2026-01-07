package com.nidoham.extragram.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.nidoham.extra.user.Presence
import com.nidoham.extra.user.UserRepository
import kotlinx.coroutines.*

class PresenceService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()
    private val rtdb = FirebaseDatabase.getInstance()

    private var lastConnectionState: Boolean? = null
    private var lastPresenceUpdate: String? = null
    private var lastPresenceUpdateTime: Long = 0

    private val listeners = mutableListOf<() -> Unit>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            setupPresence(userId)
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    private fun setupPresence(userId: String) {
        val presenceRef = rtdb.getReference("/status/$userId")
        val connectedRef = rtdb.getReference(".info/connected")

        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false

                if (connected == lastConnectionState) {
                    return
                }
                lastConnectionState = connected

                if (connected) {
                    presenceRef.setValue(
                        mapOf(
                            "state" to "online",
                            "last_changed" to ServerValue.TIMESTAMP
                        )
                    )
                    serviceScope.launch {
                        updatePresenceWithRetry(userId, Presence.ONLINE)
                    }
                    presenceRef.onDisconnect().setValue(
                        mapOf(
                            "state" to "offline",
                            "last_changed" to ServerValue.TIMESTAMP
                        )
                    )
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("PresenceService", "RTDB Connection error: ${error.message}")
            }
        }

        connectedRef.addValueEventListener(listener)
        listeners.add { connectedRef.removeEventListener(listener) }
    }

    private suspend fun updatePresenceWithRetry(userId: String, presence: Int, retryCount: Int = 0) {
        try {
            val now = System.currentTimeMillis()
            val key = "$userId:$presence"

            if (key == lastPresenceUpdate && (now - lastPresenceUpdateTime) < 5000) {
                Log.d("PresenceService", "Skipping duplicate presence update")
                return
            }

            lastPresenceUpdate = key
            lastPresenceUpdateTime = now

            val success = userRepository.updatePresence(userId, presence)

            if (!success && retryCount < 3) {
                val backoffMs = 1000L * (1 shl retryCount)
                Log.d("PresenceService", "Retrying presence update (attempt ${retryCount + 1}) after ${backoffMs}ms")
                delay(backoffMs)
                updatePresenceWithRetry(userId, presence, retryCount + 1)
            }
        } catch (e: Exception) {
            Log.e("PresenceService", "Error updating presence: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val userId = auth.currentUser?.uid
        if (userId != null) {
            serviceScope.launch {
                userRepository.updatePresence(userId, Presence.OFFLINE)
                listeners.forEach { it.invoke() }
                listeners.clear()
                serviceScope.cancel()
            }
        }
    }
}