package uddug.com.naukoteka.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
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
import uddug.com.naukoteka.mvvm.call.IncomingCallEvent
import uddug.com.naukoteka.mvvm.call.IncomingCallStore
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

    @Inject
    lateinit var incomingCallStore: IncomingCallStore

    private val gson = Gson()
    private var currentUserId: String? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val disposables = CompositeDisposable()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        ensureForeground()

        disposables.add(
            userRepository.getProfileInfo()
                .subscribeOn(Schedulers.io())
                .subscribe({ user ->
                    currentUserId = user.id
                }, { error ->
                    Log.e(TAG, "Failed to load user profile", error)
                })
        )

        runCatching { socketService.connect() }
            .onFailure { error -> Log.e(TAG, "Failed to connect socket", error) }

        socketService.setOnEvent("message", LISTENER_TAG) { message ->
            serviceScope.launch {
                handleIncomingMessage(message)
            }
        }
    }

    override fun onDestroy() {
        disposables.clear()
        socketService.removeEvent("message", LISTENER_TAG)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Каждый старт через startForegroundService() ОБЯЗАН вызвать
        // startForeground() в течение ~5с, иначе система убивает сервис с
        // RemoteServiceException. onCreate вызывается не при каждом старте
        // (START_STICKY / повторный старт живого сервиса), поэтому дублируем тут.
        ensureForeground()
        return START_STICKY
    }

    private fun ensureForeground() {
        runCatching {
            createNotificationChannels()
            startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())
        }.onFailure { Log.e(TAG, "startForeground failed", it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isAppInForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState
            .isAtLeast(Lifecycle.State.STARTED)
    }

    private fun createNotificationChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            "Фоновой сервис",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Поддержание связи для получения сообщений и звонков"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(serviceChannel)

        val callChannel = NotificationChannel(
            CALL_NOTIFICATION_CHANNEL_ID,
            "Входящие звонки",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Уведомления о входящих звонках"
        }
        notificationManager.createNotificationChannel(callChannel)

        val messageChannel = NotificationChannel(
            MESSAGE_NOTIFICATION_CHANNEL_ID,
            "Сообщения",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Уведомления о новых сообщениях"
        }
        notificationManager.createNotificationChannel(messageChannel)
    }

    private fun buildForegroundNotification() =
        NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Наукотека")
            .setContentText("Подключено к чату")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()

    private suspend fun handleIncomingMessage(message: String) {
        try {
            val jsonObject = JSONObject(message)
            if (jsonObject.has("action")) return

            val socketMessage = gson.fromJson(message, ChatSocketMessage::class.java)
            val dialogId = socketMessage.dialog ?: return
            if (socketMessage.owner == currentUserId) return

            if (socketMessage.cType == CALL_END_TYPE) {
                cancelCallNotification(dialogId)
                incomingCallStore.clear()
                return
            }

            val isIncomingCall = socketMessage.cType in listOf(CALL_AUDIO_TYPE, CALL_VIDEO_TYPE) &&
                (socketMessage.text?.contains("звонок", ignoreCase = true) == true ||
                    socketMessage.text.equals(CALL_STARTED_TEXT, ignoreCase = true))

            if (isIncomingCall) {
                handleIncomingCallMessage(socketMessage, dialogId)
            } else if (!isAppInForeground()) {
                handleChatMessage(socketMessage, dialogId)
            }
        } catch (error: Exception) {
            Log.e(TAG, "Error processing incoming message", error)
        }
    }

    private suspend fun handleIncomingCallMessage(socketMessage: ChatSocketMessage, dialogId: Long) {
        val dialogInfo = runCatching { chatInteractor.getDialogInfo(dialogId) }.getOrNull()
        val contactName = dialogInfo?.interlocutor?.fullName ?: dialogInfo?.name
        val avatarUrl = dialogInfo?.dialogImage?.path ?: dialogInfo?.interlocutor?.image
        val callTitle = dialogInfo?.name ?: contactName

        val incomingCallEvent = IncomingCallEvent(
            dialogId = dialogId,
            contactName = contactName,
            avatarUrl = avatarUrl,
            callTitle = callTitle,
            // DialogInfo.type == 1 is a 1-to-1 dialog; anything else is a group.
            isGroupCall = (dialogInfo?.type ?: 1) != 1,
            // cType 3 — видеозвонок, 2 — аудио.
            isVideoCall = socketMessage.cType == 3,
        )
        incomingCallStore.save(incomingCallEvent)
        // When the app is in the foreground the in-app IncomingCallViewModel
        // already shows the call screen. Posting a heads-up on top of it makes
        // the call ring twice and stacks a second call UI.
        if (!isAppInForeground()) {
            sendIncomingCallNotification(incomingCallEvent)
        }
    }

    private suspend fun handleChatMessage(socketMessage: ChatSocketMessage, dialogId: Long) {
        val dialogInfo = runCatching { chatInteractor.getDialogInfo(dialogId) }.getOrNull()
        val senderName = socketMessage.ownerName
            ?: dialogInfo?.interlocutor?.fullName
            ?: dialogInfo?.name
            ?: "Новое сообщение"

        val messageText = when {
            socketMessage.cType == FILE_TYPE && !socketMessage.files.isNullOrEmpty() -> {
                val file = socketMessage.files!!.first()
                when (file.fileType) {
                    FILE_TYPE_IMAGE -> "\uD83D\uDCF7 Фото"
                    FILE_TYPE_VIDEO -> "\uD83C\uDFA5 Видео"
                    FILE_TYPE_AUDIO -> "\uD83C\uDFA7 Аудио"
                    FILE_TYPE_DOCUMENT -> "\uD83D\uDCC4 ${file.fileName ?: "Документ"}"
                    else -> "\uD83D\uDCCE Вложение"
                }
            }
            !socketMessage.text.isNullOrBlank() -> socketMessage.text
            socketMessage.poll != null -> "\uD83D\uDCCA Опрос"
            socketMessage.forwarded != null || !socketMessage.forwardedn.isNullOrEmpty() -> "Пересланное сообщение"
            else -> "Новое сообщение"
        }

        sendMessageNotification(
            dialogId = dialogId,
            senderName = senderName,
            messageText = messageText,
        )
    }

    private fun cancelCallNotification(dialogId: Long) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(dialogId.toInt())
    }

    private fun sendIncomingCallNotification(event: IncomingCallEvent) {
        val intent = Intent(this, ContainerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(NaukotekaPushService.EXTRA_OPEN_INCOMING_CALL, true)
            putExtra(SingleCallFragment.ARG_IS_INCOMING_CALL, true)
            putExtra(SingleCallFragment.ARG_DIALOG_ID, event.dialogId)
            putExtra(SingleCallFragment.ARG_CONTACT_NAME, event.contactName)
            putExtra(SingleCallFragment.ARG_AVATAR_URL, event.avatarUrl)
            putExtra(SingleCallFragment.ARG_CALL_TITLE, event.callTitle)
            putExtra(SingleCallFragment.ARG_IS_GROUP_CALL, event.isGroupCall)
            putExtra(SingleCallFragment.ARG_IS_VIDEO_CALL, event.isVideoCall)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            event.dialogId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CALL_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(CALL_NOTIFICATION_TITLE)
            .setContentText(CALL_NOTIFICATION_BODY)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .build()

        notificationManager.notify(event.dialogId.toInt(), notification)
    }

    private fun sendMessageNotification(
        dialogId: Long,
        senderName: String,
        messageText: String?,
    ) {
        val intent = Intent(this, ContainerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(EXTRA_OPEN_CHAT_DIALOG, true)
            putExtra(EXTRA_CHAT_DIALOG_ID, dialogId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            CHAT_NOTIFICATION_BASE_ID + dialogId.toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, MESSAGE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(
            CHAT_NOTIFICATION_BASE_ID + dialogId.toInt(),
            notification,
        )
    }

    companion object {
        private const val TAG = "IncomingCallSocketService"
        private const val FOREGROUND_NOTIFICATION_ID = 10001
        const val CHAT_NOTIFICATION_BASE_ID = 20000

        private const val CALL_AUDIO_TYPE = 2
        private const val CALL_VIDEO_TYPE = 3
        private const val CALL_END_TYPE = 6
        private const val FILE_TYPE = 4
        private const val FILE_TYPE_IMAGE = 1
        private const val FILE_TYPE_VIDEO = 2
        private const val FILE_TYPE_AUDIO = 3
        private const val FILE_TYPE_DOCUMENT = 4

        private const val SERVICE_CHANNEL_ID = "socket_service_channel"
        private const val CALL_NOTIFICATION_CHANNEL_ID = "incoming_call_notification_channel_id"
        private const val MESSAGE_NOTIFICATION_CHANNEL_ID = "message_notification_channel_id"

        private const val CALL_NOTIFICATION_TITLE = "Вам звонят"
        private const val CALL_NOTIFICATION_BODY = "Нажмите, чтобы открыть звонок"
        private const val CALL_STARTED_TEXT = "Звонок начался"
        private const val LISTENER_TAG = "IncomingCallSocketService"

        const val EXTRA_OPEN_CHAT_DIALOG = "extra_open_chat_dialog"
        const val EXTRA_CHAT_DIALOG_ID = "extra_chat_dialog_id"
    }
}
