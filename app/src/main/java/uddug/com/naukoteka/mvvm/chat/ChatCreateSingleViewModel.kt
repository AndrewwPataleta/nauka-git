package uddug.com.naukoteka.mvvm.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.await
import javax.inject.Inject

@HiltViewModel
class ChatCreateSingleViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val userProfileRepository: UserProfileRepository,
    @ApplicationContext private val context: Context,
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
            val currentState = _uiState.value
            val selectedUser =
                if (currentState is ChatCreateSingleUiState.Success) {
                    (currentState.users + currentState.searchResults).find { it.id == userId }
                } else null


            val me = withContext(Dispatchers.IO) {
                userProfileRepository.getProfileInfo().await()
            }

            val hasPermit = selectedUser?.permits?.contains("82:200") ?: true
            if (hasPermit) {
                _events.emit(ChatCreateSingleEvent.OpenDialogDetail(userId))
            } else {
                _uiState.value = ChatCreateSingleUiState.Error(
                    context.getString(R.string.chat_private_dialog_permission_error)
                )
            }
















        }
    }
}





private suspend fun <T> Single<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        val d = this.subscribe(
            { value -> if (cont.isActive) cont.resume(value) },
            { error -> if (cont.isActive) cont.resumeWithException(error) }
        )
        cont.invokeOnCancellation { d.dispose() }
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
