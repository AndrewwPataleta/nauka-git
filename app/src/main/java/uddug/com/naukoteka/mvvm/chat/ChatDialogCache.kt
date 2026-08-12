package uddug.com.naukoteka.mvvm.chat

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import uddug.com.data.cache.persistent.PersistentJsonCache
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.PinnedMessagePreview
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory кэш последнего успешно загруженного состояния диалога, чтобы при
 * повторном открытии чата (например, возврат из звонка, когда ViewModel
 * пересоздаётся) сразу показать прошлые сообщения, а свежие подтянуть в фоне —
 * без полноэкранного спиннера. Живёт в рамках процесса (Singleton).
 *
 * Дополнительно write-through в persistent-кэш (Room): под ключом "dialog:<id>"
 * сохраняется снапшот (ШАПКА чата — имя/аватар/группа/пины + сообщения), чтобы
 * после холодного старта первый экран диалога сразу показывал и сообщения, И
 * название/аватар группы, не дожидаясь фоновой загрузки getDialogInfo.
 */
@Singleton
class ChatDialogCache @Inject constructor(
    private val persistentCache: PersistentJsonCache,
) {

    private val byDialogId = ConcurrentHashMap<Long, ChatDialogUiState.Success>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Persistable снапшот диалога: шапка + сообщения (эфемерные поля Success не пишем). */
    private data class Snapshot(
        val chatName: String,
        val chatImage: String,
        val isGroup: Boolean,
        val firstParticipantName: String,
        val chats: List<MessageChat>,
        val pinnedMessages: List<PinnedMessagePreview>,
    )

    fun get(dialogId: Long): ChatDialogUiState.Success? = byDialogId[dialogId]

    fun put(dialogId: Long, state: ChatDialogUiState.Success) {
        byDialogId[dialogId] = state
        // Write-through в Room: шапку и сообщения. Название/аватар кэшируем, только
        // когда они реально есть (не пустые) — иначе не затираем валидный снапшот
        // промежуточным пустым состоянием.
        if (state.chatName.isBlank() && state.chats.isEmpty()) return
        val snapshot = Snapshot(
            chatName = state.chatName,
            chatImage = state.chatImage,
            isGroup = state.isGroup,
            firstParticipantName = state.firstParticipantName,
            chats = state.chats,
            pinnedMessages = state.pinnedMessages,
        )
        scope.launch {
            persistentCache.putObject(dialogKey(dialogId), snapshot)
        }
    }

    /**
     * Возвращает in-memory Success, если он есть; иначе собирает Success из
     * persistent-снапшота (шапка чата + сообщения). Свежие данные подтянет фон.
     * null, если ничего не закэшировано.
     */
    suspend fun getPersisted(dialogId: Long): ChatDialogUiState.Success? {
        byDialogId[dialogId]?.let { return it }
        val snapshot = persistentCache.getObject<Snapshot>(
            dialogKey(dialogId),
            object : TypeToken<Snapshot>() {}.type,
        )
        if (snapshot == null || (snapshot.chats.isEmpty() && snapshot.chatName.isBlank())) {
            return null
        }
        return ChatDialogUiState.Success(
            chats = snapshot.chats,
            chatName = snapshot.chatName,
            chatImage = snapshot.chatImage,
            isGroup = snapshot.isGroup,
            firstParticipantName = snapshot.firstParticipantName,
            pinnedMessages = snapshot.pinnedMessages,
        )
    }

    fun clear(dialogId: Long) {
        byDialogId.remove(dialogId)
    }

    private fun dialogKey(dialogId: Long): String = "dialog:$dialogId"
}
