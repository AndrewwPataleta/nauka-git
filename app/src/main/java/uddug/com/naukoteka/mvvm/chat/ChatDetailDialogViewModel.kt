package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.User
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import uddug.com.domain.interactors.chat.ChatInteractor
import javax.inject.Inject

@HiltViewModel
class ChatDialogDetailViewModel @Inject constructor(
    private val userRepository: UserProfileRepository,
    private val chatInteractor: ChatInteractor,
    private val socketService: SocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatDetailUiState>(ChatDetailUiState.Loading)
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    private var currentDialogInfo: DialogInfo? = null
    private var currentUser: UserProfileFullInfo? = null

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun loadDialogInfo(dialogId: Long) {
        viewModelScope.launch {
            try {
                val info = chatInteractor.getDialogInfo(dialogId)
                setDialogInfo(info)
            } catch (e: Exception) {
                _uiState.value = ChatDetailUiState.Error(e.message ?: "Error")
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
                        val voices = chatInteractor.getDialogMedia(
                            dialogInfo.id,
                            category = 4,
                            limit = 50,
                            page = 1,
                            query = null,
                            sd = null,
                            ed = null,
                        )
                        val notes = chatInteractor.getDialogMedia(
                            dialogInfo.id,
                            category = 2,
                            limit = 50,
                            page = 1,
                            query = null,
                            sd = null,
                            ed = null,
                        )
                        _uiState.value = ChatDetailUiState.Success(
                            profile = User(
                                image = dialogInfo.interlocutor?.image.orEmpty(),
                                fullName = dialogInfo.interlocutor?.fullName.orEmpty(),
                                nickname = dialogInfo.interlocutor?.nickname.orEmpty()
                            ),
                            media = media,
                            files = files,
                            voices = voices,
                            notes = notes,
                            dialogId = dialogInfo.id
                        )
                    }

                }, {})

        }
    }

    fun getCurrentUser(): UserProfileFullInfo? = currentUser
}

sealed class ChatDetailUiState {
    object Loading : ChatDetailUiState()
    data class Success(
        val profile: User,
        val media: List<MediaMessage>,
        val files: List<MediaMessage>,
        val voices: List<MediaMessage>,
        val notes: List<MediaMessage>,
        val dialogId: Long,
    ) : ChatDetailUiState()

    data class Error(val message: String) : ChatDetailUiState()
}
