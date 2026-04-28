package com.solari.app.notifications

import com.google.firebase.messaging.FirebaseMessagingService
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
}
