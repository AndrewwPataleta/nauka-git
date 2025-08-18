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
                    selected = emptySet()
                )
            } catch (e: Exception) {
                _uiState.value = ChatCreateMultiUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onCurrentSearchChange(query: String) {
        _uiState.value = when (val current = _uiState.value) {
            is ChatCreateMultiUiState.Success -> current.copy(query = query)
            else -> ChatCreateMultiUiState.Success(query = query, users = emptyList(), selected = emptySet())
        }
    }

    fun onUserClick(userId: Long) {
        val current = _uiState.value
        if (current is ChatCreateMultiUiState.Success) {
            val newSelected = current.selected.toMutableSet()
            if (!newSelected.add(userId)) newSelected.remove(userId)
            _uiState.value = current.copy(selected = newSelected)
        }
    }

    fun onCreateGroupClick() {
        viewModelScope.launch {
            _events.emit(ChatCreateMultiEvent.GroupCreated)
        }
    }
}

sealed class ChatCreateMultiEvent {
    object GroupCreated : ChatCreateMultiEvent()
}

sealed class ChatCreateMultiUiState {
    object Loading : ChatCreateMultiUiState()
    data class Success(
        val query: String,
        val users: List<UserProfileFullInfo>,
        val selected: Set<Long>,
    ) : ChatCreateMultiUiState()

    data class Error(val message: String) : ChatCreateMultiUiState()
}

