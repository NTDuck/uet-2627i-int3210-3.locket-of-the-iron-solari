package com.solari.app.notifications

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        if (type == "NEW_POST_PUBLISHED") {
            val ids = AppWidgetManager.getInstance(applicationContext)
                .getAppWidgetIds(
                    ComponentName(
                        applicationContext,
                        com.solari.app.widget.SolariWidgetProvider::class.java
                    )
                )

            Log.d(
                "SolariWidgetUpdate",
                "Triggering widget update for NEW_POST_PUBLISHED. Active widget ids: ${ids.contentToString()}"
            )

            val widgetIntent =
                Intent(this, com.solari.app.widget.SolariWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
            sendBroadcast(widgetIntent)
        } else {
            Log.d("SolariWidgetUpdate", "Ignored widget update for FCM type: $type")
        }
    }
}
