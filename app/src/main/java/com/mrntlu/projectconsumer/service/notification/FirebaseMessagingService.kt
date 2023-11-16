package com.mrntlu.projectconsumer.service.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mrntlu.projectconsumer.MainActivity
import com.mrntlu.projectconsumer.R
import com.mrntlu.projectconsumer.models.auth.retrofit.UpdateFCMTokenBody
import com.mrntlu.projectconsumer.repository.UserRepository
import com.mrntlu.projectconsumer.utils.setGroupNotification
import com.mrntlu.projectconsumer.utils.setNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService: FirebaseMessagingService() {
    companion object {
        const val CHANNEL_NAME = "Main Notification"
        const val GROUP_NAME = "Group Notification"
        const val GROUP_ID = "watchlistfy.notification"

        const val PATH_EXTRA = "path"
        const val DATA_EXTRA = "data"
        const val DEEPLINK_EXTRA = "deeplink"
    }

    @Inject
    lateinit var userRepository: UserRepository

    private val job = SupervisorJob()

    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        CoroutineScope(job).launch {
            userRepository.updateFCMToken(UpdateFCMTokenBody(
                token
            )).collect()
        }
    }

    /**
     * Called when message is received.
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body, remoteMessage.data)
        }
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    private fun sendNotification(
        title: String?,
        messageBody: String?,
        data: Map<String, String>
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        for (i in 0 until data.size) {
            val key = data.keys.toList()[i]
            val value = data.values.toList()[i]
            intent.putExtra(key, value)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val channelId = getString(R.string.notification_channel_id)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = setNotification(
            channelId,
            title,
            messageBody,
            defaultSoundUri,
            GROUP_ID,
            pendingIntent
        )

        val groupNotification = setGroupNotification(
            channelId,
            GROUP_ID,
            true,
            "$title $messageBody",
            "New Notifications",
            "Notifications Grouped"
        )

        //ID of notification
        notificationManager.notify(System.currentTimeMillis().toInt(), notification.build())
        notificationManager.notify(0, groupNotification)
    }
}