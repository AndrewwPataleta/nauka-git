package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.LinkedHashMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.Chat
import uddug.com.domain.entities.chat.ChatFolderDetails
import uddug.com.domain.entities.chat.ChatFolderDialogSummary
import uddug.com.domain.interactors.chat.ChatInteractor
import javax.inject.Inject

@HiltViewModel
class ChatEditFolderViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val folderId: Long = requireNotNull(savedStateHandle.get<Long>(FOLDER_ID_ARG)) {
        "Folder id is required"
    }

    private val _uiState = MutableStateFlow(ChatEditFolderState())
    val uiState: StateFlow<ChatEditFolderState> = _uiState

    private val _events = MutableSharedFlow<ChatEditFolderEvent>()
    val events: SharedFlow<ChatEditFolderEvent> = _events.asSharedFlow()

    init {
        loadFolder()
    }

    fun onFolderNameChanged(name: String) {
        _uiState.update { it.copy(folderName = name) }
    }

    fun onChatsSelected(chats: List<ChatFolderSelectionItem>) {
        val unique = LinkedHashMap<Long, ChatFolderSelectionItem>()
        chats.forEach { item -> unique[item.dialogId] = item }
        _uiState.update { state ->
            state.copy(selectedChats = unique.values.toList())
        }
    }

    fun onChatRemoved(dialogId: Long) {
        _uiState.update { state ->
            state.copy(selectedChats = state.selectedChats.filterNot { it.dialogId == dialogId })
        }
    }

    fun onSaveClick() {
        val current = _uiState.value
        if (current.isSaving || current.isLoading) return
        val name = current.folderName.trim()
        if (name.isEmpty()) {
            viewModelScope.launch { _events.emit(ChatEditFolderEvent.ShowNameRequired) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                chatInteractor.updateFolder(
                    folderId = folderId,
                    name = name,
                    dialogIds = current.selectedChats.map { it.dialogId },
                )
                _events.emit(ChatEditFolderEvent.FolderUpdated)
            } catch (e: Exception) {
                _events.emit(ChatEditFolderEvent.ShowError(e.message))
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun retryLoad() {
        if (_uiState.value.isLoading) return
        loadFolder()
    }

    private fun loadFolder() {
        viewModelScope.launch {
            _uiState.value = ChatEditFolderState(isLoading = true)
            try {
                val details = chatInteractor.getFolder(folderId)
                val chats = runCatching { chatInteractor.getDialogs(folderId) }.getOrDefault(emptyList())
                val selectedChats = combineFolderData(details, chats)
                _uiState.value = ChatEditFolderState(
                    isLoading = false,
                    folderName = details.folder.name,
                    selectedChats = selectedChats,
                )
            } catch (e: Exception) {
                _uiState.value = ChatEditFolderState(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    private fun combineFolderData(
        details: ChatFolderDetails,
        chats: List<Chat>,
    ): List<ChatFolderSelectionItem> {
        val itemsById = LinkedHashMap<Long, ChatFolderSelectionItem>()
        chats.forEach { chat ->
            itemsById[chat.dialogId] = mapChatToSelection(chat)
        }
        details.dialogs.forEach { dialog ->
            itemsById.putIfAbsent(dialog.dialogId, mapDialogSummaryToSelection(dialog))
        }
        return details.dialogIds.mapNotNull { id -> itemsById[id] }
    }

    private fun mapChatToSelection(chat: Chat): ChatFolderSelectionItem {
        val isGroup = chat.dialogType != 1
        val title = if (isGroup) {
            chat.dialogName
        } else {
            chat.interlocutor.fullName ?: chat.interlocutor.nickname ?: chat.dialogName
        }
        val avatarUrl = if (isGroup) chat.dialogImage?.path else chat.interlocutor.image
        val subtitle = when {
            isGroup -> chat.lastMessage.text
            else -> null
        }
        return ChatFolderSelectionItem(
            dialogId = chat.dialogId,
            title = title.orEmpty(),
            subtitle = subtitle,
            avatarUrl = avatarUrl,
            initials = title,
            isGroup = isGroup,
        )
    }

    private fun mapDialogSummaryToSelection(dialog: ChatFolderDialogSummary): ChatFolderSelectionItem {
        val isGroup = dialog.dialogType != 1
        val title = when {
            !dialog.fullName.isNullOrBlank() -> dialog.fullName
            !dialog.nickname.isNullOrBlank() -> dialog.nickname
            !dialog.name.isNullOrBlank() -> dialog.name
            else -> ""
        }?.ifBlank { dialog.name.orEmpty() }?.ifBlank { dialog.dialogId.toString() }
        val subtitle = when {
            isGroup -> dialog.name?.takeIf { it.isNotBlank() && it != title }
            else -> null
        }
        return ChatFolderSelectionItem(
            dialogId = dialog.dialogId,
            title = title.orEmpty(),
            subtitle = subtitle,
            avatarUrl = dialog.imagePath,
            initials = title,
            isGroup = isGroup,
        )
    }

    companion object {
        const val FOLDER_ID_ARG = "folder_id"
    }
}

data class ChatEditFolderState(
    val isLoading: Boolean = true,
    val folderName: String = "",
    val selectedChats: List<ChatFolderSelectionItem> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

sealed class ChatEditFolderEvent {
    object FolderUpdated : ChatEditFolderEvent()
    object ShowNameRequired : ChatEditFolderEvent()
    data class ShowError(val message: String?) : ChatEditFolderEvent()
}
