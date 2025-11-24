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
import uddug.com.domain.entities.chat.ChatMediaCategory
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.User
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.mvvm.chat.ChatStatusFormatter
import uddug.com.naukoteka.mvvm.chat.ChatStatusTextMode.GENERIC
import java.io.File
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatGroupDetailViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val chatStatusFormatter: ChatStatusFormatter,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatGroupDetailUiState>(ChatGroupDetailUiState.Loading)
    val uiState: StateFlow<ChatGroupDetailUiState> = _uiState

    private val _selectedTabIndex = MutableStateFlow(0)
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var currentUserId: String? = null
    private var currentDialogInfo: DialogInfo? = null
    private var isMediaLoading = false
    private var isFilesLoading = false

    private val _avatarEvents = MutableSharedFlow<AvatarUpdateEvent>()
    val avatarEvents: SharedFlow<AvatarUpdateEvent> = _avatarEvents.asSharedFlow()

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        when (index) {
            1 -> if (!currentState.isMediaLoaded) loadMedia(currentState.dialogId)
            2 -> if (!currentState.isFilesLoaded) loadFiles(currentState.dialogId)
        }
    }

    fun loadDialogInfo(dialogId: Long) {
        viewModelScope.launch {
            try {
                _searchQuery.value = ""
                _selectedTabIndex.value = 0
                val info = chatInteractor.getDialogInfo(dialogId)
                currentDialogInfo = info
                setDialogInfo(info)
            } catch (e: Exception) {
                _uiState.value = ChatGroupDetailUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun setDialogInfo(dialogInfo: DialogInfo) {
        viewModelScope.launch {
            try {
                _searchQuery.value = ""
                currentDialogInfo = dialogInfo
                val currentUser = withContext(Dispatchers.IO) {
                    userProfileRepository.getProfileInfo().await()
                }
                currentUserId = currentUser.id
                val users = dialogInfo.users.orEmpty()
                val userIds = users.mapNotNull { it.userId }
                val statuses = if (userIds.isNotEmpty()) chatInteractor.getUsersStatus(userIds) else emptyList()
                val statusMap = statuses.associateBy { it.userId }
                val participants = users.map { user ->
                    val status = statusMap[user.userId]
                    val statusText = status?.let {
                        if (it.isOnline) {
                            chatStatusFormatter.online()
                        } else {
                            it.lastSeen?.let { ls ->
                                runCatching { Instant.parse(ls) }
                                    .map { instant -> chatStatusFormatter.formatLastSeen(instant, GENERIC) }
                                    .getOrDefault("")
                            }
                        }
                    }?.takeIf { it?.isNotEmpty() == true }
                    createParticipant(user, statusText)
                }
                val isCurrentUserAdmin = isCurrentUserAdmin(participants)
                _uiState.value = ChatGroupDetailUiState.Success(
                    name = dialogInfo.name.orEmpty(),
                    image = dialogInfo.dialogImage?.path,
                    imageId = dialogInfo.dialogImage?.id,
                    participants = participants,
                    media = emptyList(),
                    files = emptyList(),
                    dialogId = dialogInfo.id,
                    isCurrentUserAdmin = isCurrentUserAdmin,
                    isMediaLoaded = false,
                    isFilesLoaded = false,
                    isAvatarUpdating = false,
                )
                _selectedTabIndex.value = 0
                loadMedia(dialogInfo.id)
                loadFiles(dialogInfo.id)
            } catch (e: Exception) {
                _uiState.value = ChatGroupDetailUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun onAvatarSelected(file: File) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin || currentState.isAvatarUpdating) return

        updateSuccessState { it.copy(isAvatarUpdating = true) }

        viewModelScope.launch {
            try {
                val uploaded = withContext(Dispatchers.IO) {
                    chatInteractor.uploadFiles(listOf(file), raw = false)
                }.firstOrNull()

                if (uploaded != null) {
                    val updatedInfo = chatInteractor.updateDialogInfo(
                        dialogId = currentState.dialogId,
                        imageId = uploaded.id,
                        removeImage = false,
                    )
                    currentDialogInfo = updatedInfo
                    updateSuccessState {
                        it.copy(
                            image = updatedInfo.dialogImage?.path,
                            imageId = updatedInfo.dialogImage?.id,
                            isAvatarUpdating = false,
                        )
                    }
                    _avatarEvents.emit(AvatarUpdateEvent.Success)
                } else {
                    updateSuccessState { it.copy(isAvatarUpdating = false) }
                    _avatarEvents.emit(AvatarUpdateEvent.Error(null))
                }
            } catch (e: Exception) {
                updateSuccessState { it.copy(isAvatarUpdating = false) }
                _avatarEvents.emit(AvatarUpdateEvent.Error(e.message))
            }
        }
    }

    fun onAvatarDeleted() {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin || currentState.isAvatarUpdating) return

        updateSuccessState { it.copy(isAvatarUpdating = true) }

        viewModelScope.launch {
            try {
                val updatedInfo = chatInteractor.updateDialogInfo(
                    dialogId = currentState.dialogId,
                    removeImage = true,
                )
                currentDialogInfo = updatedInfo
                updateSuccessState {
                    it.copy(
                        image = updatedInfo.dialogImage?.path,
                        imageId = updatedInfo.dialogImage?.id,
                        isAvatarUpdating = false,
                    )
                }
                _avatarEvents.emit(AvatarUpdateEvent.Success)
            } catch (e: Exception) {
                updateSuccessState { it.copy(isAvatarUpdating = false) }
                _avatarEvents.emit(AvatarUpdateEvent.Error(e.message))
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun grantAdmin(userId: String) {
        updateParticipants {
            it.map { participant ->
                if (participant.user.userId == userId && !participant.isOwner) {
                    val updatedUser = participant.user.copy(isAdmin = true)
                    participant.copy(user = updatedUser, isAdmin = true)
                } else {
                    participant
                }
            }
        }
    }

    fun revokeAdmin(userId: String) {
        updateParticipants {
            it.map { participant ->
                if (participant.user.userId == userId && participant.isAdmin && !participant.isOwner) {
                    val updatedUser = participant.user.copy(isAdmin = false)
                    participant.copy(user = updatedUser, isAdmin = false)
                } else {
                    participant
                }
            }
        }
    }

    fun removeParticipant(userId: String) {
        updateParticipants { participants ->
            participants.filterNot { participant ->
                participant.user.userId == userId && !participant.isOwner
            }
        }
    }

    private fun updateParticipants(transform: (List<Participant>) -> List<Participant>) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin) return
        val updatedParticipants = transform(currentState.participants)
        _uiState.value = currentState.copy(
            participants = updatedParticipants,
            isCurrentUserAdmin = isCurrentUserAdmin(updatedParticipants)
        )
    }

    private fun loadMedia(dialogId: Long) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (isMediaLoading || currentState.isMediaLoaded) return
        isMediaLoading = true
        viewModelScope.launch {
            try {
                val media = chatInteractor.getDialogMedia(
                    dialogId,
                    category = ChatMediaCategory.MEDIA,
                    limit = 50,
                    page = 1,
                    query = null,
                    sd = null,
                    ed = null,
                )
                updateSuccessState { it.copy(media = media, isMediaLoaded = true) }
            } catch (e: Exception) {
                // Ignore and allow retry on next tab selection
            } finally {
                isMediaLoading = false
            }
        }
    }

    private fun loadFiles(dialogId: Long) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (isFilesLoading || currentState.isFilesLoaded) return
        isFilesLoading = true
        viewModelScope.launch {
            try {
                val files = chatInteractor.getDialogMedia(
                    dialogId,
                    category = ChatMediaCategory.FILES,
                    limit = 50,
                    page = 1,
                    query = null,
                    sd = null,
                    ed = null,
                )
                updateSuccessState { it.copy(files = files, isFilesLoaded = true) }
            } catch (e: Exception) {
                // Ignore and allow retry on next tab selection
            } finally {
                isFilesLoading = false
            }
        }
    }

    private fun updateSuccessState(transform: (ChatGroupDetailUiState.Success) -> ChatGroupDetailUiState.Success) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        _uiState.value = transform(currentState)
    }

    private fun createParticipant(user: User, statusText: String?): Participant {
        val isOwner = isOwnerRole(user.role)
        val isAdmin = user.isAdmin || isAdminRole(user.role) || isOwner
        val updatedUser = user.copy(isAdmin = isAdmin)
        val isCurrent = user.userId != null && user.userId == currentUserId
        return Participant(
            user = updatedUser,
            status = statusText,
            isAdmin = isAdmin,
            isOwner = isOwner,
            isCurrentUser = isCurrent,
        )
    }

    private fun isCurrentUserAdmin(participants: List<Participant>): Boolean {
        val userId = currentUserId ?: return false
        return participants.any { participant ->
            participant.user.userId == userId && (participant.isAdmin || participant.isOwner)
        }
    }

    private fun isOwnerRole(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        val normalized = role.lowercase()
        return normalized == ROLE_OWNER || normalized.contains("owner") || normalized.contains("влад")
    }

    private fun isAdminRole(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        val normalized = role.lowercase()
        return normalized == ROLE_ADMIN || normalized.contains("admin") || normalized.contains("админ")
    }

    companion object {
        private const val ROLE_OWNER = "37:201"
        private const val ROLE_ADMIN = "37:202"
    }
}

sealed class ChatGroupDetailUiState {
    object Loading : ChatGroupDetailUiState()
    data class Success(
        val name: String,
        val image: String?,
        val imageId: String?,
        val participants: List<Participant>,
        val media: List<MediaMessage>,
        val files: List<MediaMessage>,
        val dialogId: Long,
        val isCurrentUserAdmin: Boolean,
        val isMediaLoaded: Boolean = false,
        val isFilesLoaded: Boolean = false,
        val isAvatarUpdating: Boolean = false,
    ) : ChatGroupDetailUiState()

    data class Error(val message: String) : ChatGroupDetailUiState()
}

data class Participant(
    val user: User,
    val status: String?,
    val isAdmin: Boolean,
    val isOwner: Boolean,
    val isCurrentUser: Boolean,
)
