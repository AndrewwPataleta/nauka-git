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
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import javax.inject.Inject

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
    }

    fun setDialogInfo(dialogInfo: DialogInfo) {
        currentDialogInfo = dialogInfo

        viewModelScope.launch {
            userRepository.getProfileInfo().subscribeOn(Schedulers.io())
                .subscribe({
                    currentUser = it
                    viewModelScope.launch {
                        val currentMedia = chatRepository.getDialogMedia(dialogId = dialogInfo.id)
                        _uiState.value = ChatDetailUiState.Success(
                            profile = User(
                                image = dialogInfo.interlocutor?.image.orEmpty(),
                                fullName = dialogInfo.interlocutor?.fullName.orEmpty(),
                                nickname = dialogInfo.interlocutor?.nickname.orEmpty()
                            ),
                            currentMedia = currentMedia
                        )
                    }

                }, {})

        }
    }

}

sealed class ChatDetailUiState {
    object Loading : ChatDetailUiState()
    data class Success(
        val profile: User,
        val currentMedia: List<MediaMessage>,
    ) : ChatDetailUiState()

    data class Error(val message: String) : ChatDetailUiState()
}
