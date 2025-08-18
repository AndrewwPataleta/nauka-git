package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uddug.com.data.repositories.chat.ChatRepository
import uddug.com.domain.entities.chat.ChatSocketMessage
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.FileDescriptor
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.User
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import javax.inject.Inject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@HiltViewModel
class ChatDialogDetailViewModel @Inject constructor(
    private val userRepository: UserProfileRepository,
    private val chatRepository: ChatRepository,
    private val socketService: SocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatDetailUiState>(ChatDetailUiState.Loading)
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    private var currentDialogInfo: DialogInfo? = null
    private var attachedFiles: MutableList<FileDescriptor> = mutableListOf()

    private var currentUser: UserProfileFullInfo? = null

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        currentDialogInfo?.let { dialogInfo ->
            viewModelScope.launch {
                val media = chatRepository.getDialogMedia(
                    dialogId = dialogInfo.id,
                    category = index + 1,
                )
                val state = _uiState.value
                if (state is ChatDetailUiState.Success) {
                    _uiState.value = state.copy(currentMedia = media)
                }
            }
        }
    }

    fun setDialogInfo(dialogInfo: DialogInfo) {
        currentDialogInfo = dialogInfo

        viewModelScope.launch {
            userRepository.getProfileInfo().subscribeOn(Schedulers.io())
                .subscribe({
                    currentUser = it
                    viewModelScope.launch {
                        val currentMedia = chatRepository.getDialogMedia(
                            dialogId = dialogInfo.id,
                            category = _selectedTabIndex.value + 1,
                        )
                        val status = dialogInfo.interlocutor?.userId?.let { userId ->
                            chatRepository.getUserStatuses(listOf(userId)).firstOrNull()
                        }
                        val statusText = formatStatus(status)
                        _uiState.value = ChatDetailUiState.Success(
                            profile = User(
                                image = dialogInfo.interlocutor?.image.orEmpty(),
                                fullName = dialogInfo.interlocutor?.fullName.orEmpty(),
                                nickname = dialogInfo.interlocutor?.nickname.orEmpty()
                            ),
                            currentMedia = currentMedia,
                            status = statusText
                        )
                    }

                }, {})

        }
    }

    private fun formatStatus(status: UserStatus?): String {
        return if (status == null) {
            ""
        } else if (status.isOnline) {
            "Онлайн"
        } else {
            status.lastSeen?.let { "Был в сети ${formatLastSeen(it)}" } ?: ""
        }
    }

    private fun formatLastSeen(lastSeen: String): String {
        return try {
            val instant = Instant.parse(lastSeen)
            val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            localDateTime.format(formatter)
        } catch (e: Exception) {
            ""
        }
    }

}

sealed class ChatDetailUiState {
    object Loading : ChatDetailUiState()
    data class Success(
        val profile: User,
        val currentMedia: List<MediaMessage>,
        val status: String,
    ) : ChatDetailUiState()

    data class Error(val message: String) : ChatDetailUiState()
}
