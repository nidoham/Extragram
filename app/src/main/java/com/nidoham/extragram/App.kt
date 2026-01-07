package com.nidoham.extragram

import android.app.Application
import android.content.Intent
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.nidoham.extragram.service.PresenceService

/**
 * Professional Application class for Extragram.
 * Handles Firebase initialization and global lifecycle monitoring for presence management.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // Firebase already initialized or error
        }

        // 2. Setup Global Lifecycle Observer
        // This ensures the PresenceService starts/stops based on app foreground/background state
        setupLifecycleObserver()
    }

    private fun setupLifecycleObserver() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                // App enters foreground
                startPresenceService()
            }

            override fun onStop(owner: LifecycleOwner) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    stopPresenceService()
                }
            }
        })
    }

    /**
     * Starts the PresenceService if a user is logged in.
     */
    private fun startPresenceService() {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val intent = Intent(this, PresenceService::class.java)
                startService(intent)
            }
        } catch (e: Exception) {
            // Service not available or already running
        }
    }

    /**
     * Stops the PresenceService (e.g., during logout or app background).
     */
    private fun stopPresenceService() {
        try {
            val intent = Intent(this, PresenceService::class.java)
            stopService(intent)
        } catch (e: Exception) {
            // Service not running or error stopping
        }
    }
}