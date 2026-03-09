package uddug.com.naukoteka.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.app.ActivityManager
import android.media.RingtoneManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.call.SingleCallFragment
import uddug.com.naukoteka.ui.chat.di.SocketService
import javax.inject.Inject

@AndroidEntryPoint
class IncomingCallSocketService : Service() {

    @Inject
    lateinit var socketService: SocketService

    @Inject
    lateinit var chatInteractor: ChatInteractor

    @Inject
    lateinit var userRepository: UserProfileRepository

    private val gson = Gson()
    private var currentUserId: String? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        userRepository.getProfileInfo()
            .subscribeOn(Schedulers.io())
            .subscribe({ user ->
                currentUserId = user.id
            }, { error ->
                Log.e(TAG, "Failed to load user profile", error)
            })

        runCatching { socketService.connect() }
            .onFailure { error -> Log.e(TAG, "Failed to connect socket", error) }

        socketService.setOnEvent("message") { message ->
            serviceScope.launch {
                handleIncomingMessage(message)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun handleIncomingMessage(message: String) {
        try {
            val jsonObject = JSONObject(message)
            if (jsonObject.has("action")) return

            val socketMessage = gson.fromJson(message, ChatSocketMessage::class.java)
            val dialogId = socketMessage.dialog ?: return
            if (socketMessage.owner == currentUserId) return
            if (socketMessage.cType !in listOf(CALL_AUDIO_TYPE, CALL_VIDEO_TYPE)) return

            val dialogInfo = runCatching { chatInteractor.getDialogInfo(dialogId) }.getOrNull()
            val contactName = dialogInfo?.interlocutor?.fullName ?: dialogInfo?.name
            val avatarUrl = dialogInfo?.dialogImage?.path ?: dialogInfo?.interlocutor?.image
            val callTitle = dialogInfo?.name ?: contactName

            if (!isAppInForeground()) {
                sendIncomingCallNotification(
                dialogId = dialogId,
                contactName = contactName,
                avatarUrl = avatarUrl,
                callTitle = callTitle,
            )
            }
        } catch (error: Exception) {
            Log.e(TAG, "Error processing incoming call", error)
        }
    }

    private fun sendIncomingCallNotification(
        dialogId: Long,
        contactName: String?,
        avatarUrl: String?,
        callTitle: String?,
    ) {
        val intent = Intent(this, ContainerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(NaukotekaPushService.EXTRA_OPEN_INCOMING_CALL, true)
            putExtra(SingleCallFragment.ARG_IS_INCOMING_CALL, true)
            putExtra(SingleCallFragment.ARG_DIALOG_ID, dialogId)
            putExtra(SingleCallFragment.ARG_CONTACT_NAME, contactName)
            putExtra(SingleCallFragment.ARG_AVATAR_URL, avatarUrl)
            putExtra(SingleCallFragment.ARG_CALL_TITLE, callTitle)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            dialogId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CALL_NOTIFICATION_CHANNEL_ID,
            CALL_NOTIFICATION_TITLE,
            NotificationManager.IMPORTANCE_HIGH,
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, CALL_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.logo_farmix)
            .setContentTitle(CALL_NOTIFICATION_TITLE)
            .setContentText(CALL_NOTIFICATION_BODY)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(dialogId.toInt(), notification)
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return false
        val runningProcesses = activityManager.runningAppProcesses ?: return false
        return runningProcesses.any {
            it.processName == packageName &&
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }

    companion object {
        private const val TAG = "IncomingCallSocketService"
        private const val CALL_AUDIO_TYPE = 2
        private const val CALL_VIDEO_TYPE = 3
        private const val CALL_NOTIFICATION_CHANNEL_ID = "incoming_call_notification_channel_id"
        private const val CALL_NOTIFICATION_TITLE = "Вам звонят"
        private const val CALL_NOTIFICATION_BODY = "Нажмите, чтобы открыть звонок"
    }
}
