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
class ChatCreateMultiViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ChatCreateMultiUiState>(ChatCreateMultiUiState.Loading)
    val uiState: StateFlow<ChatCreateMultiUiState> = _uiState

    private val _events = MutableSharedFlow<ChatCreateMultiEvent>()
    val events: SharedFlow<ChatCreateMultiEvent> = _events.asSharedFlow()

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
                _uiState.value = ChatCreateMultiUiState.Success(
                    query = "",
                    users = users,
                    searchResults = emptyList(),
                    selected = emptySet<String>()
                )
            } catch (e: Exception) {
                _uiState.value = ChatCreateMultiUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onCurrentSearchChange(query: String) {
        val currentState = _uiState.value
        if (currentState is ChatCreateMultiUiState.Success) {
            _uiState.value = currentState.copy(query = query)
            viewModelScope.launch {
                try {
                    if (query.isNotBlank()) {
                        val result = chatInteractor.searchUsers(query)
                        _uiState.value =
                            (_uiState.value as ChatCreateMultiUiState.Success).copy(searchResults = result)
                    } else {
                        _uiState.value =
                            (_uiState.value as ChatCreateMultiUiState.Success).copy(searchResults = emptyList())
                    }
                } catch (e: Exception) {
                    _uiState.value = ChatCreateMultiUiState.Error(e.message ?: "Unknown error")
                }
            }
        } else {
            _uiState.value = ChatCreateMultiUiState.Success(
                query = query,
                users = emptyList(),
                searchResults = emptyList(),
                selected = emptySet<String>()
            )
        }
    }

    fun onUserClick(userId: String) {
        val current = _uiState.value
        if (current is ChatCreateMultiUiState.Success) {
            val newSelected = current.selected.toMutableSet()
            if (!newSelected.add(userId)) newSelected.remove(userId)
            _uiState.value = current.copy(selected = newSelected)
        }
    }

    fun onCreateGroupClick() {
        val current = _uiState.value
        if (current is ChatCreateMultiUiState.Success) {
            viewModelScope.launch {
                try {
                    val allUsers = current.users + current.searchResults
                    val userRoles = allUsers
                        .mapNotNull { user ->
                            user.id?.let { id -> id to if (current.selected.contains(id)) "37:202" else null }
                        }
                        .toMap()
                    val dialogId = chatInteractor.createGroupDialog(userRoles)
                    _events.emit(ChatCreateMultiEvent.GroupCreated(dialogId))
                } catch (e: Exception) {
                    _uiState.value = ChatCreateMultiUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
}

sealed class ChatCreateMultiEvent {
    data class GroupCreated(val dialogId: Long) : ChatCreateMultiEvent()
}

sealed class ChatCreateMultiUiState {
    object Loading : ChatCreateMultiUiState()
    data class Success(
        val query: String,
        val users: List<UserProfileFullInfo>,
        val searchResults: List<UserProfileFullInfo>,
        val selected: Set<String>,
    ) : ChatCreateMultiUiState()

    data class Error(val message: String) : ChatCreateMultiUiState()
}

