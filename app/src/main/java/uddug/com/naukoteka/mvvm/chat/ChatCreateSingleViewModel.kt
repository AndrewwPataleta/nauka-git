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
import uddug.com.data.repositories.chat.ChatRepository
import uddug.com.domain.entities.chat.User
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import javax.inject.Inject

@HiltViewModel
class ChatCreateSingleViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ChatCreateSingleUiState>(ChatCreateSingleUiState.Loading)
    val uiState: StateFlow<ChatCreateSingleUiState> = _uiState


    private val _events = MutableSharedFlow<ChatCreateSingleEvent>()
    val events: SharedFlow<ChatCreateSingleEvent> = _events.asSharedFlow()

    init {
        _uiState.value = ChatCreateSingleUiState.Success(query = "", users = emptyList())
    }

    fun onGroupCreateClick() {

    }

    fun onCurrentSearchChange(query: String) {
        viewModelScope.launch {
            _uiState.value = when (val currentState = _uiState.value) {
                is ChatCreateSingleUiState.Success -> currentState.copy(query = query)
                else -> ChatCreateSingleUiState.Success(query = query, users = emptyList())
            }
            if (query.isNotBlank()) {
                val users = chatRepository.searchUsers(query)
                _uiState.value = ChatCreateSingleUiState.Success(query = query, users = users)
            } else {
                _uiState.value = ChatCreateSingleUiState.Success(query = query, users = emptyList())
            }
        }
    }

    fun onUserClick(user: User) {
        viewModelScope.launch {
            user.userId?.toLongOrNull()?.let { id ->
                chatRepository.createDialog(id)
                _events.emit(ChatCreateSingleEvent.CloseAndRefresh)
            }
        }
    }
}

sealed class ChatCreateSingleEvent {
    data class OpenDialogDetail(val dialogId: Long) : ChatCreateSingleEvent()
    data object CloseAndRefresh : ChatCreateSingleEvent()
}

sealed class ChatCreateSingleUiState {
    object Loading : ChatCreateSingleUiState()
    data class Success(
        val query: String,
        val users: List<User>,
    ) : ChatCreateSingleUiState()

    data class Error(val message: String) : ChatCreateSingleUiState()
}