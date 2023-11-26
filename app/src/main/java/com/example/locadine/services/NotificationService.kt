package com.example.locadine.services

import android.content.Context
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.locadine.R

object NotificationService {
    private val CHANNEL_ID = "localdine"

    fun displayNotification(context: Context, title: String, body: String) {
        try {
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1, notificationBuilder.build())
        } catch (e: SecurityException) {
            Toast.makeText(context, "Attempted to display a notification but permission has not been granted", Toast.LENGTH_SHORT).show()
        }
    }
}