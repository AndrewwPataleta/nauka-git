package uddug.com.naukoteka.mvvm.chat

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory кэш последнего успешно загруженного состояния диалога, чтобы при
 * повторном открытии чата (например, возврат из звонка, когда ViewModel
 * пересоздаётся) сразу показать прошлые сообщения, а свежие подтянуть в фоне —
 * без полноэкранного спиннера. Живёт в рамках процесса (Singleton); при смерти
 * процесса очищается, что допустимо для UX-кэша.
 */
@Singleton
class ChatDialogCache @Inject constructor() {

    private val byDialogId = ConcurrentHashMap<Long, ChatDialogUiState.Success>()

    fun get(dialogId: Long): ChatDialogUiState.Success? = byDialogId[dialogId]

    fun put(dialogId: Long, state: ChatDialogUiState.Success) {
        byDialogId[dialogId] = state
    }

    fun clear(dialogId: Long) {
        byDialogId.remove(dialogId)
    }
}
