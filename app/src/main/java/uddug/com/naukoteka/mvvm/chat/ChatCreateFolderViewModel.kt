package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.domain.interactors.chat.ChatInteractor
import javax.inject.Inject

@HiltViewModel
class ChatCreateFolderViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatCreateFolderState())
    val uiState: StateFlow<ChatCreateFolderState> = _uiState

    private val _events = MutableSharedFlow<ChatCreateFolderEvent>()
    val events: SharedFlow<ChatCreateFolderEvent> = _events.asSharedFlow()

    fun onFolderNameChanged(name: String) {
        _uiState.update { it.copy(folderName = name) }
    }

    fun onChatsSelected(chats: List<ChatFolderSelectionItem>) {
        _uiState.update { state ->
            val uniqueChats = chats.distinctBy { it.dialogId }
            state.copy(selectedChats = uniqueChats)
        }
    }

    fun onChatRemoved(dialogId: Long) {
        _uiState.update { state ->
            state.copy(selectedChats = state.selectedChats.filterNot { it.dialogId == dialogId })
        }
    }

    fun onCreateFolderClick() {
        val current = _uiState.value
        val name = current.folderName.trim()
        if (name.isEmpty()) {
            viewModelScope.launch { _events.emit(ChatCreateFolderEvent.ShowNameRequired) }
            return
        }
        if (current.isSaving) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val folder = chatInteractor.createFolder(
                    name = name,
                    dialogIds = current.selectedChats.map { it.dialogId }
                )
                _events.emit(ChatCreateFolderEvent.FolderCreated(folder.id))
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(ChatCreateFolderEvent.ShowError(e.message))
            }
        }
    }
}

data class ChatCreateFolderState(
    val folderName: String = "",
    val selectedChats: List<ChatFolderSelectionItem> = emptyList(),
    val isSaving: Boolean = false,
)

sealed class ChatCreateFolderEvent {
    data class FolderCreated(val folderId: Long) : ChatCreateFolderEvent()
    object ShowNameRequired : ChatCreateFolderEvent()
    data class ShowError(val message: String?) : ChatCreateFolderEvent()
}
