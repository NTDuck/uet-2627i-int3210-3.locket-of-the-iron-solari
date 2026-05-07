package com.solari.app.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WidgetPinReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("WidgetPinReceiver", "Widget pinning confirmed, exiting app")

        val exitIntent = Intent(context, com.solari.app.MainActivity::class.java).apply {
            action = "com.solari.app.ACTION_EXIT"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(exitIntent)
    }
}
