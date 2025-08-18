package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import uddug.com.domain.entities.profile.Image
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import javax.inject.Inject

@HiltViewModel
class ChatCreateSingleViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ChatCreateSingleUiState>(ChatCreateSingleUiState.Loading)
    val uiState: StateFlow<ChatCreateSingleUiState> = _uiState


    private val _events = MutableSharedFlow<ChatCreateSingleEvent>()
    val events: SharedFlow<ChatCreateSingleEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                val users = chatInteractor.getDialogs().map { chat ->
                    UserProfileFullInfo(
                        id = chat.interlocutor.userId,
                        fullName = chat.interlocutor.fullName,
                        image = Image(path = chat.interlocutor.image)
                    )
                }
                _uiState.value = ChatCreateSingleUiState.Success(
                    query = "",
                    users = users,
                    searchResults = emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = ChatCreateSingleUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onGroupCreateClick() {
        viewModelScope.launch {
            _events.emit(ChatCreateSingleEvent.OpenMultiCreate)
        }
    }

    fun onCurrentSearchChange(query: String) {
        val currentState = _uiState.value
        if (currentState is ChatCreateSingleUiState.Success) {
            _uiState.value = currentState.copy(query = query)
            viewModelScope.launch {
                if (query.isNotBlank()) {
                    try {
                        val result = chatInteractor.searchUsers(query)
                        _uiState.value = (_uiState.value as ChatCreateSingleUiState.Success).copy(
                            searchResults = result,
                        )
                    } catch (e: Exception) {
                        _uiState.value = ChatCreateSingleUiState.Error(e.message ?: "Unknown error")
                    }
                } else {
                    _uiState.value = (_uiState.value as ChatCreateSingleUiState.Success).copy(
                        searchResults = emptyList(),
                    )
                }
            }
        } else {
            _uiState.value = ChatCreateSingleUiState.Success(
                query = query,
                users = emptyList(),
                searchResults = emptyList()
            )
        }
    }

    fun onUserClick(userId: String) {
        viewModelScope.launch {
            try {
                val id = userId.toLongOrNull() ?: throw IllegalArgumentException("Invalid user id")
                val dialogId = chatInteractor.createDialog(id)
                _events.emit(ChatCreateSingleEvent.OpenDialogDetail(dialogId))
            } catch (e: Exception) {
                _uiState.value = ChatCreateSingleUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class ChatCreateSingleEvent {
    data class OpenDialogDetail(val dialogId: Long) : ChatCreateSingleEvent()
    object OpenMultiCreate : ChatCreateSingleEvent()
}

sealed class ChatCreateSingleUiState {
    object Loading : ChatCreateSingleUiState()
    data class Success(
        val query: String,
        val users: List<UserProfileFullInfo>,
        val searchResults: List<UserProfileFullInfo>,
    ) : ChatCreateSingleUiState()

    data class Error(val message: String) : ChatCreateSingleUiState()
}