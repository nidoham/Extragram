package com.nidoham.extragram.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nidoham.extragram.MainActivity
import com.nidoham.extragram.R
import timber.log.Timber

/**
 * Firebase Cloud Messaging Service for handling push notifications.
 *
 * Handles:
 * - New message notifications
 * - Incoming call notifications
 * - Token refresh
 */
class ExtragramMessagingService : FirebaseMessagingService() {

    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    /**
     * Called when a new FCM token is generated
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("New FCM token: $token")

        // Save token to Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            saveFcmTokenToFirestore(userId, token)
        }
    }

    /**
     * Called when a message is received
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Timber.d("FCM message received from: ${message.from}")

        // Extract notification data
        val title = message.notification?.title ?: message.data["title"] ?: "New Message"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val chatId = message.data["chatId"]
        val senderId = message.data["senderId"]
        val type = message.data["type"] ?: "message" // message, call, group, etc.

        // Handle different notification types
        when (type) {
            "message" -> handleMessageNotification(title, body, chatId, senderId)
            "call" -> handleCallNotification(title, body, chatId, senderId)
            "group" -> handleGroupNotification(title, body, chatId, senderId)
            else -> handleGenericNotification(title, body)
        }
    }

    /**
     * Handle regular chat message notifications
     */
    private fun handleMessageNotification(
        title: String,
        body: String,
        chatId: String?,
        senderId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("chatId", chatId)
            putExtra("senderId", senderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "chat_messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setColor(getColor(R.color.notification_color))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        notificationManager?.notify(chatId?.hashCode() ?: 1, notification)
    }

    /**
     * Handle incoming call notifications
     */
    private fun handleCallNotification(
        title: String,
        body: String,
        callId: String?,
        callerId: String?
    ) {
        // Intent to answer call
        val answerIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_ANSWER_CALL"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("callId", callId)
            putExtra("callerId", callerId)
        }

        val answerPendingIntent = PendingIntent.getActivity(
            this,
            1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to decline call
        val declineIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_DECLINE_CALL"
            putExtra("callId", callId)
        }

        val declinePendingIntent = PendingIntent.getActivity(
            this,
            2,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "voice_calls")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(answerPendingIntent, true)
            .addAction(R.drawable.ic_call_answer, "Answer", answerPendingIntent)
            .addAction(R.drawable.ic_call_decline, "Decline", declinePendingIntent)
            .setOngoing(true)
            .build()

        notificationManager?.notify(callId?.hashCode() ?: 2, notification)
    }

    /**
     * Handle group message notifications
     */
    private fun handleGroupNotification(
        title: String,
        body: String,
        groupId: String?,
        senderId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("groupId", groupId)
            putExtra("senderId", senderId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "group_messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setGroup("group_messages")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        notificationManager?.notify(groupId?.hashCode() ?: 3, notification)
    }

    /**
     * Handle generic notifications
     */
    private fun handleGenericNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            4,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "chat_messages")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager?.notify(System.currentTimeMillis().toInt(), notification)
    }

    /**
     * Save FCM token to Firestore
     */
    private fun saveFcmTokenToFirestore(userId: String, token: String) {
        // TODO: Implement Firestore save logic
        // Example:
        // FirebaseFirestore.getInstance()
        //     .collection("users")
        //     .document(userId)
        //     .update(mapOf(
        //         "fcmToken" to token,
        //         "lastTokenUpdate" to FieldValue.serverTimestamp()
        //     ))
        //     .addOnSuccessListener { Timber.d("FCM token saved") }
        //     .addOnFailureListener { e -> Timber.e(e, "Failed to save FCM token") }
    }
}