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
import javax.inject.Inject

@HiltViewModel
class ChatCreateMultiViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ChatCreateMultiUiState>(ChatCreateMultiUiState.Loading)
    val uiState: StateFlow<ChatCreateMultiUiState> = _uiState

    private val _events = MutableSharedFlow<ChatCreateMultiEvent>()
    val events: SharedFlow<ChatCreateMultiEvent> = _events.asSharedFlow()

    init {
        _uiState.value = ChatCreateMultiUiState.Success(query = "", users = emptyList(), selectedIds = emptySet())
    }

    fun onCurrentSearchChange(query: String) {
        viewModelScope.launch {
            val selected = (uiState.value as? ChatCreateMultiUiState.Success)?.selectedIds ?: emptySet()
            _uiState.value = when (val currentState = _uiState.value) {
                is ChatCreateMultiUiState.Success -> currentState.copy(query = query)
                else -> ChatCreateMultiUiState.Success(query = query, users = emptyList(), selectedIds = selected)
            }
            if (query.isNotBlank()) {
                val users = chatRepository.searchUsers(query)
                _uiState.value = ChatCreateMultiUiState.Success(query = query, users = users, selectedIds = selected)
            } else {
                _uiState.value = ChatCreateMultiUiState.Success(query = query, users = emptyList(), selectedIds = selected)
            }
        }
    }

    fun onUserChecked(user: User, checked: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ChatCreateMultiUiState.Success) {
                val newSelected = currentState.selectedIds.toMutableSet()
                user.userId?.toLongOrNull()?.let { id ->
                    if (checked) newSelected.add(id) else newSelected.remove(id)
                }
                _uiState.value = currentState.copy(selectedIds = newSelected)
            }
        }
    }

    fun onCreateGroupClick() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is ChatCreateMultiUiState.Success) {
                chatRepository.createGroupDialog(current.selectedIds.toList())
                _events.emit(ChatCreateMultiEvent.CloseAndRefresh)
            }
        }
    }
}

sealed class ChatCreateMultiEvent {
    data object CloseAndRefresh : ChatCreateMultiEvent()
}

sealed class ChatCreateMultiUiState {
    object Loading : ChatCreateMultiUiState()
    data class Success(
        val query: String,
        val users: List<User>,
        val selectedIds: Set<Long>,
    ) : ChatCreateMultiUiState()

    data class Error(val message: String) : ChatCreateMultiUiState()
}
