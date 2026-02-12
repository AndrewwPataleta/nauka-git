package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val users = withContext(Dispatchers.IO) {
                    chatInteractor.getDialogs()
                        .filter { it.dialogType == 1 }
                        .map { chat ->
                            UserProfileFullInfo(
                                id = chat.interlocutor.userId,
                                fullName = chat.interlocutor.fullName,
                                image = Image(path = chat.interlocutor.image)
                            )
                        }
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
                        val result = withContext(Dispatchers.IO) {
                            chatInteractor.searchUsers(query)
                        }
                        val success = _uiState.value as? ChatCreateSingleUiState.Success
                        if (success != null) {
                            _uiState.value = success.copy(searchResults = result)
                        }
                    } catch (e: Exception) {
                        _uiState.value = ChatCreateSingleUiState.Error(e.message ?: "Unknown error")
                    }
                } else {
                    val success = _uiState.value as? ChatCreateSingleUiState.Success
                    if (success != null) {
                        _uiState.value = success.copy(searchResults = emptyList())
                    }
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
                _events.emit(ChatCreateSingleEvent.OpenDialogDetail(userId))
            } catch (e: Exception) {
                _uiState.value = ChatCreateSingleUiState.Error(
                    e.message ?: "Unknown error"
                )
            }
        }
    }
}






sealed class ChatCreateSingleEvent {
    data class OpenDialogDetail(val interlocutorId: String) : ChatCreateSingleEvent()
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
