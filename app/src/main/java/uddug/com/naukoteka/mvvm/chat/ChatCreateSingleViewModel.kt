package uddug.com.naukoteka.mvvm.chat

import android.annotation.SuppressLint
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
import uddug.com.domain.entities.profile.UserProfileFullInfo
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
        _uiState.value = ChatCreateSingleUiState.Success(query = "")
    }

    fun onGroupCreateClick() {

    }

    fun onCurrentSearchChange(query: String) {
        _uiState.value = when (val currentState = _uiState.value) {
            is ChatCreateSingleUiState.Success -> currentState.copy(query = query)
            else -> ChatCreateSingleUiState.Success(query = query)
        }
    }
}

sealed class ChatCreateSingleEvent {
    data class OpenDialogDetail(val dialogId: Long) : ChatCreateSingleEvent()
}

sealed class ChatCreateSingleUiState {
    object Loading : ChatCreateSingleUiState()
    data class Success(
        val query: String,
    ) : ChatCreateSingleUiState()

    data class Error(val message: String) : ChatCreateSingleUiState()
}