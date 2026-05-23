package com.solari.app.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Dispatches foreground push notifications as system notifications.
 * Maintains the currently visible ChatScreen conversation ID so that
 * NEW_MESSAGE pushes for that conversation are suppressed.
 */
object ForegroundNotificationDispatcher {

    private val activeConversationId = AtomicReference<String?>(null)
    private val notificationIdCounter = AtomicInteger(1000)

    fun setActiveConversationId(conversationId: String?) {
        activeConversationId.set(conversationId)
    }

    fun getActiveConversationId(): String? = activeConversationId.get()

    /**
     * Attempts to display a foreground notification for the given FCM message.
     * Returns true if the notification was shown, false if it was suppressed.
     */
    fun dispatchForegroundNotification(
        context: Context,
        message: RemoteMessage
    ): Boolean {
        val type = message.data["type"] ?: return false

        // Never display NEW_POST_PUBLISHED as a system notification
        if (type == "NEW_POST_PUBLISHED") return false

        // Suppress NEW_MESSAGE when user is viewing that conversation
        if (type == "NEW_MESSAGE") {
            val conversationId = message.data["conversationId"]
            if (conversationId != null && conversationId == activeConversationId.get()) {
                return false
            }
        }

        val notification = message.notification ?: return false
        val title = notification.title ?: return false
        val body = notification.body ?: return false

        val channelId = resolveChannelId(type)
        val priority = resolveNotificationPriority(type)
        val notificationId = notificationIdCounter.getAndIncrement()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return false

        notificationManager.notify(notificationId, builder.build())
        return true
    }

    private fun resolveChannelId(type: String): String = when (type) {
        "NEW_MESSAGE" -> "direct_messages"
        "NEW_MESSAGE_REACTION", "NEW_POST_REACTION" -> "reactions"
        "NEW_FRIEND_REQUEST", "FRIEND_REQUEST_ACCEPTED" -> "friend_activities"
        "STREAK_MILESTONE" -> "milestones_streaks"
        else -> "direct_messages"
    }

    private fun resolveNotificationPriority(type: String): Int = when (type) {
        "NEW_MESSAGE", "NEW_FRIEND_REQUEST", "FRIEND_REQUEST_ACCEPTED" ->
            NotificationCompat.PRIORITY_HIGH
        else -> NotificationCompat.PRIORITY_DEFAULT
    }
}
