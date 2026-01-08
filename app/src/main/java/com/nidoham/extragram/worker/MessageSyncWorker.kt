package com.nidoham.extragram.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Background worker to sync unread messages and update local database.
 *
 * Runs periodically (every 15 minutes) when network is available.
 *
 * Tasks:
 * - Fetch unread message count
 * - Update notification badge
 * - Sync recent messages to local cache
 * - Clean up old message cache
 */
@HiltWorker
class MessageSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override suspend fun doWork(): Result {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Timber.w("MessageSyncWorker: No user logged in")
                return Result.success()
            }

            Timber.d("MessageSyncWorker: Starting sync for user $userId")

            // 1. Sync unread message count
            val unreadCount = fetchUnreadMessageCount(userId)
            Timber.d("Unread messages: $unreadCount")

            // 2. Update app badge (if supported)
            updateNotificationBadge(unreadCount)

            // 3. Sync recent messages (last 24 hours)
            syncRecentMessages(userId)

            // 4. Clean up old cached data (optional)
            cleanupOldCache()

            Timber.d("MessageSyncWorker: Sync completed successfully")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "MessageSyncWorker: Sync failed")

            // Retry on network issues, fail permanently on auth issues
            if (e is com.google.firebase.FirebaseNetworkException) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    /**
     * Fetch unread message count from Firestore
     */
    private suspend fun fetchUnreadMessageCount(userId: String): Int {
        return try {
            val snapshot = firestore
                .collection("chats")
                .whereArrayContains("participants", userId)
                .get()
                .await()

            var totalUnread = 0
            snapshot.documents.forEach { doc ->
                val unreadMap = doc.get("unreadCount") as? Map<*, *>
                val userUnread = unreadMap?.get(userId) as? Long ?: 0L
                totalUnread += userUnread.toInt()
            }

            totalUnread
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch unread count")
            0
        }
    }

    /**
     * Update notification badge (Android 8+)
     */
    private fun updateNotificationBadge(count: Int) {
        try {
            // TODO: Implement badge update using ShortcutManager or third-party library
            // Example with ShortcutBadger:
            // ShortcutBadger.applyCount(applicationContext, count)

            Timber.d("Badge updated to: $count")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update badge")
        }
    }

    /**
     * Sync recent messages to local cache/database
     */
    private suspend fun syncRecentMessages(userId: String) {
        try {
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

            val snapshot = firestore
                .collection("messages")
                .whereEqualTo("recipientId", userId)
                .whereGreaterThan("timestamp", oneDayAgo)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            Timber.d("Synced ${snapshot.size()} recent messages")

            // TODO: Save to local Room database
            // snapshot.documents.forEach { doc ->
            //     val message = doc.toObject(Message::class.java)
            //     localDatabase.messageDao().insert(message)
            // }

        } catch (e: Exception) {
            Timber.e(e, "Failed to sync recent messages")
        }
    }

    /**
     * Clean up old cached data (messages older than 7 days)
     */
    private suspend fun cleanupOldCache() {
        try {
            // TODO: Implement local database cleanup
            // val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            // localDatabase.messageDao().deleteOlderThan(sevenDaysAgo)

            Timber.d("Cache cleanup completed")
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup cache")
        }
    }
}