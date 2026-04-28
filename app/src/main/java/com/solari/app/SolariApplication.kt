package com.solari.app

import android.app.Application
import com.solari.app.data.di.AppContainer

class SolariApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(applicationContext)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(android.app.NotificationManager::class.java) ?: return

        val directMessagesChannel = android.app.NotificationChannel(
            "direct_messages",
            "Direct Messages",
            android.app.NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new direct messages and replies"
        }

        val reactionsChannel = android.app.NotificationChannel(
            "reactions",
            "Reactions",
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for reactions to your messages or posts"
        }

        val friendActivitiesChannel = android.app.NotificationChannel(
            "friend_activities",
            "Friend Activity",
            android.app.NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for friend requests and acceptances"
        }

        val milestonesStreaksChannel = android.app.NotificationChannel(
            "milestones_streaks",
            "Milestones & Streaks",
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for streak milestones"
        }

        notificationManager.createNotificationChannels(
            listOf(
                directMessagesChannel,
                reactionsChannel,
                friendActivitiesChannel,
                milestonesStreaksChannel
            )
        )
    }
}
