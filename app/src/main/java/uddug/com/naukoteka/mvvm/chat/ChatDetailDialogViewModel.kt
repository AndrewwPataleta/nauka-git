package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.User
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.ui.chat.di.SocketService
import uddug.com.domain.interactors.chat.ChatInteractor
import android.net.Uri
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
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

    private val _searchMessages = MutableStateFlow<List<MessageChat>>(emptyList())
    val searchMessages: StateFlow<List<MessageChat>> = _searchMessages

    private val _searchMedia = MutableStateFlow<List<MediaMessage>>(emptyList())
    val searchMedia: StateFlow<List<MediaMessage>> = _searchMedia

    private val _searchFiles = MutableStateFlow<List<MediaMessage>>(emptyList())
    val searchFiles: StateFlow<List<MediaMessage>> = _searchFiles

    private val _searchNotes = MutableStateFlow<List<MediaMessage>>(emptyList())
    val searchNotes: StateFlow<List<MediaMessage>> = _searchNotes

    private val _events = MutableSharedFlow<ChatDialogDetailEvent>()
    val events: SharedFlow<ChatDialogDetailEvent> = _events.asSharedFlow()

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

    fun search(dialogId: Long, query: String) {
        if (query.isBlank()) {
            _searchMessages.value = emptyList()
            _searchMedia.value = emptyList()
            _searchFiles.value = emptyList()
            _searchNotes.value = emptyList()
            return
        }

        val userId = currentUser?.id ?: return
        viewModelScope.launch {
            try {
                _searchMessages.value = chatInteractor.searchMessages(
                    userId,
                    dialogId,
                    query,
                )
                _searchMedia.value = chatInteractor.getDialogMedia(
                    dialogId,
                    category = 1,
                    limit = 50,
                    page = 1,
                    query = query,
                    sd = null,
                    ed = null,
                )
                _searchFiles.value = chatInteractor.getDialogMedia(
                    dialogId,
                    category = 3,
                    limit = 50,
                    page = 1,
                    query = query,
                    sd = null,
                    ed = null,
                )
                _searchNotes.value = chatInteractor.getDialogMedia(
                    dialogId,
                    category = 2,
                    limit = 50,
                    page = 1,
                    query = query,
                    sd = null,
                    ed = null,
                )
            } catch (e: Exception) {
                _searchMessages.value = emptyList()
                _searchMedia.value = emptyList()
                _searchFiles.value = emptyList()
                _searchNotes.value = emptyList()
            }
        }
    }

    fun shareDialog() {
        val dialogId = currentDialogInfo?.id ?: return
        Firebase.dynamicLinks.shortLinkAsync {
            link = Uri.parse("https://stage.naukotheka.ru/chat/$dialogId")
            domainUriPrefix = "https://naukoteka.page.link"
            androidParameters {}
        }.addOnSuccessListener { shortLink ->
            viewModelScope.launch {
                _events.emit(ChatDialogDetailEvent.Share(shortLink.shortLink.toString()))
            }
        }
    }
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

sealed class ChatDialogDetailEvent {
    data class Share(val link: String) : ChatDialogDetailEvent()
}
