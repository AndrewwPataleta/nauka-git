package uddug.com.naukoteka.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.activities.main.AuthActivity
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.call.SingleCallFragment

class NaukotekaPushService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val notificationTitle = remoteMessage.notification?.title
            ?: data[KEY_NOTIFICATION_TITLE]
            ?: if (isIncomingCallPush(data)) CALL_NOTIFICATION_TITLE else NOTIFICATION_TITLE
        val notificationBody = remoteMessage.notification?.body
            ?: data[KEY_NOTIFICATION_BODY]
            ?: if (isIncomingCallPush(data)) CALL_NOTIFICATION_BODY else ""

        sendNotification(
            title = notificationTitle,
            messageBody = notificationBody,
            remoteMessageData = data,
        )
    }

    override fun onNewToken(token: String) {}

    private fun sendNotification(
        title: String,
        messageBody: String,
        remoteMessageData: Map<String, String>,
    ) {
        val requestCode = 0
        val intent = createNotificationIntent(remoteMessageData)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val isIncomingCallPush = isIncomingCallPush(remoteMessageData)
        val channelId = if (isIncomingCallPush) {
            CALL_NOTIFICATION_CHANNEL_ID
        } else {
            DEFAULT_NOTIFICATION_CHANNEL_ID
        }
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification).setContentTitle(title)
            .setContentText(messageBody).setAutoCancel(true).setSound(defaultSoundUri)
            .setPriority(
                if (isIncomingCallPush) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT,
            )
            .setContentIntent(pendingIntent)
        if (isIncomingCallPush) {
            notificationBuilder
                .setFullScreenIntent(pendingIntent, true)
                .setCategory(NotificationCompat.CATEGORY_CALL)
        }

        if (!canPostNotifications()) return

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun canPostNotifications(): Boolean {
        return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createNotificationIntent(remoteMessageData: Map<String, String>): Intent {
        if (!isIncomingCallPush(remoteMessageData)) {
            return Intent(this, AuthActivity::class.java)
        }

        val dialogId = remoteMessageData[KEY_DIALOG_ID]?.toLongOrNull()
            ?: remoteMessageData[KEY_DIALOG]?.toLongOrNull()

        return Intent(this, ContainerActivity::class.java).apply {
            putExtra(EXTRA_OPEN_INCOMING_CALL, true)
            putExtra(SingleCallFragment.ARG_IS_INCOMING_CALL, true)
            putExtra(SingleCallFragment.ARG_DIALOG_ID, dialogId ?: -1L)
            putExtra(
                SingleCallFragment.ARG_CONTACT_NAME,
                remoteMessageData[KEY_CONTACT_NAME],
            )
            putExtra(
                SingleCallFragment.ARG_AVATAR_URL,
                remoteMessageData[KEY_AVATAR_URL],
            )
            putExtra(
                SingleCallFragment.ARG_CALL_TITLE,
                remoteMessageData[KEY_CALL_TITLE] ?: remoteMessageData[KEY_NOTIFICATION_TITLE],
            )
        }
    }

    private fun isIncomingCallPush(data: Map<String, String>): Boolean {
        val cType = data[KEY_C_TYPE]?.toIntOrNull()
        val eventType = data[KEY_EVENT_TYPE]
        return cType in setOf(2, 3) ||
            eventType.equals(INCOMING_CALL_EVENT_TYPE, ignoreCase = true) ||
            data.containsKey(KEY_DIALOG_ID)
    }

    companion object {
        private const val DEFAULT_NOTIFICATION_CHANNEL_ID = "default_notification_channel_id"
        private const val CALL_NOTIFICATION_CHANNEL_ID = "incoming_call_notification_channel_id"
        private const val NOTIFICATION_TITLE = "Уведомление"
        private const val CALL_NOTIFICATION_TITLE = "Вам звонят"
        private const val CALL_NOTIFICATION_BODY = "Нажмите, чтобы открыть звонок"
        private const val INCOMING_CALL_EVENT_TYPE = "incoming_call"

        const val EXTRA_OPEN_INCOMING_CALL = "extra_open_incoming_call"

        private const val KEY_C_TYPE = "cType"
        private const val KEY_DIALOG = "dialog"
        private const val KEY_DIALOG_ID = "dialogId"
        private const val KEY_EVENT_TYPE = "type"
        private const val KEY_NOTIFICATION_TITLE = "title"
        private const val KEY_NOTIFICATION_BODY = "body"
        private const val KEY_CONTACT_NAME = "contactName"
        private const val KEY_AVATAR_URL = "avatarUrl"
        private const val KEY_CALL_TITLE = "callTitle"
    }
}
