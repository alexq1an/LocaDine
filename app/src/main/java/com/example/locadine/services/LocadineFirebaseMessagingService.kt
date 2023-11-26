package com.example.locadine.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class LocadineFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.notification != null) {
            val title = message.notification!!.title!!
            val body = message.notification!!.body!!

            NotificationService.displayNotification(applicationContext, title, body)
        }
    }
}