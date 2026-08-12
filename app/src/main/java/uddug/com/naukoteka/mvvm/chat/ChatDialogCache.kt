package uddug.com.naukoteka.mvvm.chat

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import uddug.com.data.cache.persistent.PersistentJsonCache
import uddug.com.domain.entities.chat.MessageChat
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory кэш последнего успешно загруженного состояния диалога, чтобы при
 * повторном открытии чата (например, возврат из звонка, когда ViewModel
 * пересоздаётся) сразу показать прошлые сообщения, а свежие подтянуть в фоне —
 * без полноэкранного спиннера. Живёт в рамках процесса (Singleton).
 *
 * Дополнительно write-through в persistent-кэш: список сообщений каждого диалога
 * пишется в Room под ключом "messages:<dialogId>", чтобы после холодного старта
 * (смерть процесса) первый экран диалога тоже показывал сообщения мгновенно.
 */
@Singleton
class ChatDialogCache @Inject constructor(
    private val persistentCache: PersistentJsonCache,
) {

    private val byDialogId = ConcurrentHashMap<Long, ChatDialogUiState.Success>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun get(dialogId: Long): ChatDialogUiState.Success? = byDialogId[dialogId]

    fun put(dialogId: Long, state: ChatDialogUiState.Success) {
        byDialogId[dialogId] = state
        // Write-through: сохраняем только список сообщений (остальные поля Success —
        // производные/эфемерные и восстанавливаются дефолтами в getPersisted).
        val messages = state.chats
        scope.launch {
            persistentCache.putList(messagesKey(dialogId), messages)
        }
    }

    /**
     * Возвращает in-memory Success, если он есть; иначе собирает Success из
     * persistent-кэша сообщений (chats = сохранённый список, все прочие поля —
     * безопасные дефолты). null, если ничего не закэшировано.
     */
    suspend fun getPersisted(dialogId: Long): ChatDialogUiState.Success? {
        byDialogId[dialogId]?.let { return it }
        val messages = persistentCache.getList<MessageChat>(
            messagesKey(dialogId),
            object : TypeToken<List<MessageChat>>() {}.type,
        )
        if (messages.isNullOrEmpty()) return null
        return ChatDialogUiState.Success(
            chats = messages,
            chatName = "",
            chatImage = "",
            isGroup = false,
        )
    }

    fun clear(dialogId: Long) {
        byDialogId.remove(dialogId)
    }

    private fun messagesKey(dialogId: Long): String = "messages:$dialogId"
}
