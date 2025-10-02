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
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.User
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.mvvm.chat.ChatStatusFormatter
import uddug.com.naukoteka.mvvm.chat.ChatStatusTextMode.GENERIC
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

    private val _events = MutableSharedFlow<ChatGroupDetailEvent>()
    val events: SharedFlow<ChatGroupDetailEvent> = _events.asSharedFlow()

    private var dialogId: Long? = null
    private var currentUserId: String? = null

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun loadDialogInfo(dialogId: Long) {
        this.dialogId = dialogId
        viewModelScope.launch {
            try {
                _searchQuery.value = ""
                val info = chatInteractor.getDialogInfo(dialogId)
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
                dialogId = dialogInfo.id
                val currentUser = withContext(Dispatchers.IO) {
                    userProfileRepository.getProfileInfo().await()
                }
                currentUserId = currentUser.id
                val media = chatInteractor.getDialogMedia(
                    dialogInfo.id,
                    category = 1,
                    limit = 50,
                    page = 1,
                    query = null,
                    sd = null,
                    ed = null,
                )
                val files = chatInteractor.getDialogMedia(
                    dialogInfo.id,
                    category = 3,
                    limit = 50,
                    page = 1,
                    query = null,
                    sd = null,
                    ed = null,
                )
                val users = dialogInfo.users.orEmpty()
                val userIds = users.mapNotNull { it.userId }
                val statuses = if (userIds.isNotEmpty()) chatInteractor.getUsersStatus(userIds) else emptyList()
                val statusMap = statuses.associateBy { it.userId }
                val notificationsDisabled = runCatching {
                    withContext(Dispatchers.IO) {
                        chatInteractor.getDialogs()
                    }.firstOrNull { it.dialogId == dialogInfo.id }?.notificationsDisable ?: false
                }.getOrDefault(false)
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
                    participants = participants,
                    media = media,
                    files = files,
                    dialogId = dialogInfo.id,
                    isCurrentUserAdmin = isCurrentUserAdmin,
                    notificationsDisabled = notificationsDisabled,
                )
            } catch (e: Exception) {
                _uiState.value = ChatGroupDetailUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }

    fun refreshDialogInfo() {
        dialogId?.let { loadDialogInfo(it) }
    }

    fun toggleNotifications() {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        viewModelScope.launch {
            try {
                if (currentState.notificationsDisabled) {
                    chatInteractor.enableNotifications(currentState.dialogId)
                    val latestState = _uiState.value as? ChatGroupDetailUiState.Success ?: return@launch
                    _uiState.value = latestState.copy(notificationsDisabled = false)
                    _events.emit(ChatGroupDetailEvent.NotificationsUpdated(false))
                } else {
                    chatInteractor.disableNotifications(currentState.dialogId)
                    val latestState = _uiState.value as? ChatGroupDetailUiState.Success ?: return@launch
                    _uiState.value = latestState.copy(notificationsDisabled = true)
                    _events.emit(ChatGroupDetailEvent.NotificationsUpdated(true))
                }
            } catch (e: Exception) {
                _events.emit(ChatGroupDetailEvent.ShowError(e.message ?: "Error"))
            }
        }
    }

    fun deleteGroup() {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin) return
        viewModelScope.launch {
            try {
                chatInteractor.deleteGroupDialog(currentState.dialogId)
                _events.emit(ChatGroupDetailEvent.GroupDeleted)
            } catch (e: Exception) {
                _events.emit(ChatGroupDetailEvent.ShowError(e.message ?: "Error"))
            }
        }
    }

    fun grantAdmin(userId: String) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin) return
        viewModelScope.launch {
            try {
                chatInteractor.makeDialogAdmin(currentState.dialogId, userId)
                val latestState = _uiState.value as? ChatGroupDetailUiState.Success ?: return@launch
                val updatedParticipants = latestState.participants.map { participant ->
                    if (participant.user.userId == userId && !participant.isOwner) {
                        val updatedUser = participant.user.copy(isAdmin = true)
                        participant.copy(user = updatedUser, isAdmin = true)
                    } else {
                        participant
                    }
                }
                _uiState.value = latestState.copy(
                    participants = updatedParticipants,
                    isCurrentUserAdmin = isCurrentUserAdmin(updatedParticipants)
                )
            } catch (e: Exception) {
                _events.emit(ChatGroupDetailEvent.ShowError(e.message ?: "Error"))
            }
        }
    }

    fun revokeAdmin(userId: String) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin) return
        viewModelScope.launch {
            try {
                chatInteractor.removeDialogAdmin(currentState.dialogId, userId)
                val latestState = _uiState.value as? ChatGroupDetailUiState.Success ?: return@launch
                val updatedParticipants = latestState.participants.map { participant ->
                    if (participant.user.userId == userId && participant.isAdmin && !participant.isOwner) {
                        val updatedUser = participant.user.copy(isAdmin = false)
                        participant.copy(user = updatedUser, isAdmin = false)
                    } else {
                        participant
                    }
                }
                _uiState.value = latestState.copy(
                    participants = updatedParticipants,
                    isCurrentUserAdmin = isCurrentUserAdmin(updatedParticipants)
                )
            } catch (e: Exception) {
                _events.emit(ChatGroupDetailEvent.ShowError(e.message ?: "Error"))
            }
        }
    }

    fun removeParticipant(userId: String) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin) return
        val updatedParticipants = currentState.participants.filterNot { participant ->
            participant.user.userId == userId && !participant.isOwner
        }
        if (updatedParticipants.size == currentState.participants.size) return
        viewModelScope.launch {
            try {
                val users = updatedParticipants
                    .filterNot { it.isCurrentUser }
                    .mapNotNull { it.user.userId }
                chatInteractor.updateDialog(currentState.dialogId, users = users)
                _uiState.value = currentState.copy(
                    participants = updatedParticipants,
                    isCurrentUserAdmin = isCurrentUserAdmin(updatedParticipants)
                )
            } catch (e: Exception) {
                _events.emit(ChatGroupDetailEvent.ShowError(e.message ?: "Error"))
            }
        }
    }

    fun addParticipants(members: List<UserProfileFullInfo>) {
        val currentState = _uiState.value as? ChatGroupDetailUiState.Success ?: return
        if (!currentState.isCurrentUserAdmin) return
        val existingIds = currentState.participants
            .mapNotNull { it.user.userId }
            .toMutableSet()
        val selfId = currentUserId
        val newIds = members.mapNotNull { user ->
            val userId = user.id
            if (userId == null || userId == selfId || existingIds.contains(userId)) {
                null
            } else {
                existingIds.add(userId)
                userId
            }
        }
        if (newIds.isEmpty()) return
        viewModelScope.launch {
            try {
                val users = currentState.participants
                    .filterNot { it.isCurrentUser }
                    .mapNotNull { it.user.userId }
                    .toMutableSet()
                users.addAll(newIds)
                val updatedDialog = chatInteractor.updateDialog(
                    dialogId = currentState.dialogId,
                    users = users.toList()
                )
                setDialogInfo(updatedDialog)
            } catch (e: Exception) {
                _events.emit(ChatGroupDetailEvent.ShowError(e.message ?: "Error"))
            }
        }
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
        val participants: List<Participant>,
        val media: List<MediaMessage>,
        val files: List<MediaMessage>,
        val dialogId: Long,
        val isCurrentUserAdmin: Boolean,
        val notificationsDisabled: Boolean,
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

sealed class ChatGroupDetailEvent {
    data class ShowError(val message: String) : ChatGroupDetailEvent()
    data class NotificationsUpdated(val disabled: Boolean) : ChatGroupDetailEvent()
    object GroupDeleted : ChatGroupDetailEvent()
}
