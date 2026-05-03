package com.solari.app.notifications

import android.content.ComponentName
import android.content.Intent
import android.appwidget.AppWidgetManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.solari.app.R

class SolariFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val appContainer = (application as? com.solari.app.SolariApplication)
            ?.appContainer
            ?: return

        CoroutineScope(Dispatchers.IO).launch {
            appContainer.pushNotificationCoordinator.onNewToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("SolariWidgetUpdate", "FCM onMessageReceived called. Data: ${message.data}")
        val type = message.data["type"]
        
        // Trigger widget update only upon receiving a new post push notification
        if (type == "NEW_POST_PUBLISHED") {
            val ids = AppWidgetManager.getInstance(applicationContext)
                .getAppWidgetIds(ComponentName(applicationContext, com.solari.app.widget.SolariWidgetProvider::class.java))
            
            Log.d("SolariWidgetUpdate", "Triggering widget update for NEW_POST_PUBLISHED. Active widget ids: ${ids.contentToString()}")
            
            val widgetIntent = Intent(this, com.solari.app.widget.SolariWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            sendBroadcast(widgetIntent)
        } else {
            Log.d("SolariWidgetUpdate", "Ignored widget update for FCM type: $type")
        }
    }
}
