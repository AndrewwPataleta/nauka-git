package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.User
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.naukoteka.mvvm.chat.ChatStatusFormatter
import uddug.com.naukoteka.mvvm.chat.ChatStatusTextMode.GENERIC
import javax.inject.Inject

@HiltViewModel
class ChatGroupDetailViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val chatStatusFormatter: ChatStatusFormatter,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatGroupDetailUiState>(ChatGroupDetailUiState.Loading)
    val uiState: StateFlow<ChatGroupDetailUiState> = _uiState

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun loadDialogInfo(dialogId: Long) {
        viewModelScope.launch {
            try {
                val info = chatInteractor.getDialogInfo(dialogId)
                setDialogInfo(info)
            } catch (e: Exception) {
                _uiState.value = ChatGroupDetailUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun setDialogInfo(dialogInfo: DialogInfo) {
        viewModelScope.launch {
            try {
                val media = chatInteractor.getDialogMedia(
                    dialogInfo.id,
                    category = 1,
                    limit = 50,
                    page = 1,
                    query = null,
                    sd = null,
                    ed = null,
                )
                val files = chatInteractor.getDialogMedia(
                    dialogInfo.id,
                    category = 3,
                    limit = 50,
                    page = 1,
                    query = null,
                    sd = null,
                    ed = null,
                )
                val users = dialogInfo.users.orEmpty()
                val userIds = users.mapNotNull { it.userId }
                val statuses = if (userIds.isNotEmpty()) chatInteractor.getUsersStatus(userIds) else emptyList()
                val statusMap = statuses.associateBy { it.userId }
                val participants = users.map { user ->
                    val status = statusMap[user.userId]
                    val statusText = status?.let {
                        if (it.isOnline) {
                            chatStatusFormatter.online()
                        } else {
                            it.lastSeen?.let { ls ->
                                runCatching { java.time.Instant.parse(ls) }
                                    .map { instant -> chatStatusFormatter.formatLastSeen(instant, GENERIC) }
                                    .getOrDefault("")
                            }
                        }
                    }?.takeIf { it?.isNotEmpty() == true }
                    Participant(user, statusText)
                }
                _uiState.value = ChatGroupDetailUiState.Success(
                    name = dialogInfo.name.orEmpty(),
                    image = dialogInfo.dialogImage?.path,
                    participants = participants,
                    media = media,
                    files = files,
                    dialogId = dialogInfo.id,
                )
            } catch (e: Exception) {
                _uiState.value = ChatGroupDetailUiState.Error(e.message ?: "Error")
            }
        }
    }
}

sealed class ChatGroupDetailUiState {
    object Loading : ChatGroupDetailUiState()
    data class Success(
        val name: String,
        val image: String?,
        val participants: List<Participant>,
        val media: List<MediaMessage>,
        val files: List<MediaMessage>,
        val dialogId: Long,
    ) : ChatGroupDetailUiState()

    data class Error(val message: String) : ChatGroupDetailUiState()
}

data class Participant(
    val user: User,
    val status: String?,
)
