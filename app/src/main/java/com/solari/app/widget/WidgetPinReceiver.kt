package com.solari.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WidgetPinReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetPinReceiver", "Widget pinning confirmed, exiting app")
        
        // This broadcast is received when the user clicks "Add" in the pinning dialog.
        // We want to exit the app. Since this is a receiver, we can't call finish() on an activity.
        // But we can send another broadcast that MainActivity listens to, or use a simpler approach.
        
        // Let's use an intent to the MainActivity with a flag to finish.
        val exitIntent = Intent(context, com.solari.app.MainActivity::class.java).apply {
            action = "com.solari.app.ACTION_EXIT"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(exitIntent)
    }
}
