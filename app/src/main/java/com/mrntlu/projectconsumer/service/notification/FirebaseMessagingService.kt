package com.mrntlu.projectconsumer.service.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService: FirebaseMessagingService() {
    companion object {
        const val CHANNEL_NAME = "Main Notification"
        const val GROUP_NAME = "Group Notification"
        const val GROUP_ID = "watchnlist.notification"
    }

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        handleNewToken(token)
    }

    // After you've obtained the token, you can send it to your app server and store it using your preferred method.
    private fun handleNewToken(token: String) {
        //TODO Save new token
    }

    /**
     * Called when message is received.
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //TODO Implement later
    }
}