package uddug.com.naukoteka.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import uddug.com.naukoteka.R

/**
 * Foreground-сервис для демонстрации экрана.
 *
 * На Android 10+ (Q) [android.media.projection.MediaProjectionManager.getMediaProjection]
 * можно вызывать ТОЛЬКО когда уже работает foreground-сервис с типом
 * `mediaProjection`. Поэтому фрагмент сначала стартует этот сервис, а получив
 * подтверждение ([ScreenShareBridge.onForegrounded]) — строит MediaProjection и
 * запускает захват. Кнопка «Остановить» в шторке шлёт [ACTION_STOP] →
 * [ScreenShareBridge.onStopRequested].
 */
class ScreenShareService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            ScreenShareBridge.onStopRequested?.invoke()
            stopSelf()
            return START_NOT_STICKY
        }
        // Сервис поднят и переведён в foreground → безопасно строить проекцию.
        // Колбэк одноразовый: снимаем его сразу, чтобы возможный перезапуск сервиса
        // не дёрнул устаревшую ссылку на уничтоженный фрагмент.
        val callback = ScreenShareBridge.onForegrounded
        ScreenShareBridge.onForegrounded = null
        callback?.invoke()
        return START_NOT_STICKY
    }

    private fun startAsForeground() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Демонстрация экрана",
                NotificationManager.IMPORTANCE_LOW,
            )
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Наукотека")
            .setContentText("Идёт демонстрация экрана")
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    companion object {
        const val ACTION_STOP = "uddug.com.naukoteka.screenshare.STOP"
        private const val CHANNEL_ID = "screen_share"
        private const val NOTIFICATION_ID = 0x5C7EE // произвольный стабильный id

        fun start(context: Context) {
            val intent = Intent(context, ScreenShareService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, ScreenShareService::class.java))
        }
    }
}

/**
 * Мост «сервис → фрагмент» в пределах одного процесса. Проще и надёжнее, чем
 * (deprecated) LocalBroadcastManager: колбэки вызываются на главном потоке из
 * [ScreenShareService.onStartCommand].
 */
object ScreenShareBridge {
    @Volatile
    var onForegrounded: (() -> Unit)? = null

    @Volatile
    var onStopRequested: (() -> Unit)? = null

    fun clear() {
        onForegrounded = null
        onStopRequested = null
    }
}
