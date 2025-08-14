package uddug.com.naukoteka.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.activities.main.AuthActivity

class NaukotekaPushService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        if (remoteMessage.data.isNotEmpty()) {
        }

        remoteMessage.notification?.let {
            it.body?.let { body -> sendNotification(body) }
        }
    }

    override fun onNewToken(token: String) {}

    private fun sendNotification(messageBody: String) {
        val requestCode = 0
        val intent = Intent(this, AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = DEFAULT_NOTIFICATION_CHANNEL_ID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.logo_farmix).setContentTitle(NOTIFICATION_TITLE)
            .setContentText(messageBody).setAutoCancel(true).setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        val channel = NotificationChannel(
            channelId,
            NOTIFICATION_TITLE,
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)

        val notificationId = 0
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FirebaseMessagingService"
        private const val DEFAULT_NOTIFICATION_CHANNEL_ID = "default_notification_channel_id"
        private const val NOTIFICATION_TITLE = "notification_title"
    }
}
