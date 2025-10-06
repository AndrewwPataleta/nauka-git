package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.SavedStateHandle
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
import uddug.com.domain.entities.chat.UserStatus
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import java.io.File
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatCreateGroupViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val userProfileRepository: UserProfileRepository,
    private val chatStatusFormatter: ChatStatusFormatter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val SELECTED_USERS_KEY = "selected_users"
        private const val MAX_GROUP_NAME_LENGTH = 100
        private const val ADMIN_ROLE = "37:202"
    }

    private val initialParticipants: List<UserProfileFullInfo> =
        savedStateHandle.get<ArrayList<UserProfileFullInfo>>(SELECTED_USERS_KEY)?.toList().orEmpty()

    private val _uiState = MutableStateFlow<ChatCreateGroupUiState>(ChatCreateGroupUiState.Loading)
    val uiState: StateFlow<ChatCreateGroupUiState> = _uiState

    private val _events = MutableSharedFlow<ChatCreateGroupEvent>()
    val events: SharedFlow<ChatCreateGroupEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                val currentUser = withContext(Dispatchers.IO) {
                    userProfileRepository.getProfileInfo().await()
                }
                val distinctParticipants = initialParticipants
                    .filter { participant -> participant.id != currentUser.id }
                    .distinctBy { it.id }
                val members = buildList {
                    add(GroupMember(user = currentUser, isCreator = true, isAdmin = true))
                    distinctParticipants.forEach { participant ->
                        add(GroupMember(user = participant, isCreator = false, isAdmin = false))
                    }
                }
                val membersWithStatus = enrichWithStatuses(members)
                val defaultName = distinctParticipants
                    .mapNotNull { it.fullName }
                    .joinToString(separator = ", ")
                    .take(MAX_GROUP_NAME_LENGTH)
                _uiState.value = ChatCreateGroupUiState.Success(
                    groupName = defaultName,
                    members = membersWithStatus,
                    avatarPath = null,
                    avatarId = null,
                    isSaving = false,
                    isAvatarUploading = false
                )
            } catch (e: Exception) {
                _uiState.value = ChatCreateGroupUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onGroupNameChanged(name: String) {
        val current = _uiState.value
        if (current is ChatCreateGroupUiState.Success) {
            val trimmed = name.take(MAX_GROUP_NAME_LENGTH)
            _uiState.value = current.copy(groupName = trimmed)
        }
    }

    fun onAvatarSelected(file: File) {
        val current = _uiState.value
        if (current is ChatCreateGroupUiState.Success && !current.isAvatarUploading) {
            _uiState.value = current.copy(isAvatarUploading = true)
            viewModelScope.launch {
                try {
                    val uploaded = withContext(Dispatchers.IO) {
                        chatInteractor.uploadFiles(listOf(file), raw = false)
                    }
                    val uploadedFile = uploaded.firstOrNull()
                    if (uploadedFile != null) {
                        val latestState = _uiState.value as? ChatCreateGroupUiState.Success
                        if (latestState != null) {
                            _uiState.value = latestState.copy(
                                avatarPath = uploadedFile.path,
                                avatarId = uploadedFile.id,
                                isAvatarUploading = false
                            )
                        } else {
                            _uiState.value = current.copy(
                                avatarPath = uploadedFile.path,
                                avatarId = uploadedFile.id,
                                isAvatarUploading = false
                            )
                        }
                    } else {
                        val latestState = _uiState.value as? ChatCreateGroupUiState.Success
                        _uiState.value = latestState?.copy(isAvatarUploading = false)
                            ?: current.copy(isAvatarUploading = false)
                        _events.emit(ChatCreateGroupEvent.ShowImageUploadError)
                    }
                } catch (e: Exception) {
                    val latestState = _uiState.value as? ChatCreateGroupUiState.Success
                    _uiState.value = latestState?.copy(isAvatarUploading = false)
                        ?: current.copy(isAvatarUploading = false)
                    _events.emit(ChatCreateGroupEvent.ShowError(e.message))
                }
            }
        }
    }

    fun onAvatarRemoved() {
        val current = _uiState.value
        if (current is ChatCreateGroupUiState.Success && !current.isAvatarUploading) {
            _uiState.value = current.copy(avatarPath = null, avatarId = null)
        }
    }

    fun onCreateGroupClick() {
        val current = _uiState.value
        if (current is ChatCreateGroupUiState.Success) {
            val participants = current.members.filterNot { it.isCreator }
            if (participants.isEmpty()) {
                viewModelScope.launch {
                    _events.emit(ChatCreateGroupEvent.ShowMissingParticipantsError)
                }
                return
            }

            if (current.isSaving || current.isAvatarUploading) return

            val groupName = current.groupName.takeIf { it.isNotBlank() }
                ?: participants.mapNotNull { it.user.fullName }
                    .joinToString(separator = ", ")
                    .take(MAX_GROUP_NAME_LENGTH)

            val userRoles = current.members
                .filterNot { it.isCreator }
                .mapNotNull { member ->
                    member.user.id?.let { id ->
                        val role = if (member.isAdmin) ADMIN_ROLE else "37:203"
                        id to role
                    }
                }
                .toMap()

            if (userRoles.isEmpty()) {
                _uiState.value = current.copy(isSaving = false)
                viewModelScope.launch {
                    _events.emit(ChatCreateGroupEvent.ShowError(null))
                }
                return
            }

            _uiState.value = current.copy(isSaving = true)

            viewModelScope.launch {
                try {
                    val dialogId = withContext(Dispatchers.IO) {
                        chatInteractor.createGroupDialog(groupName, userRoles, current.avatarId)
                    }
                    _events.emit(ChatCreateGroupEvent.GroupCreated(dialogId))
                } catch (e: Exception) {
                    _uiState.value = current.copy(isSaving = false)
                    _events.emit(ChatCreateGroupEvent.ShowError(e.message))
                }
            }
        }
    }

    fun onGrantAdminRights(memberId: String) {
        updateMembers { members ->
            members.map { member ->
                if (member.user.id == memberId && !member.isCreator) {
                    member.copy(isAdmin = true)
                } else {
                    member
                }
            }
        }
    }

    fun onRevokeAdminRights(memberId: String) {
        updateMembers { members ->
            members.map { member ->
                if (member.user.id == memberId && !member.isCreator) {
                    member.copy(isAdmin = false)
                } else {
                    member
                }
            }
        }
    }

    fun onRemoveMember(memberId: String) {
        updateMembers { members ->
            members.filterNot { member ->
                member.user.id == memberId && !member.isCreator
            }
        }
    }

    private fun updateMembers(transform: (List<GroupMember>) -> List<GroupMember>) {
        val current = _uiState.value
        if (current is ChatCreateGroupUiState.Success) {
            val updatedMembers = transform(current.members)
            _uiState.value = current.copy(members = updatedMembers)
        }
    }
    private suspend fun enrichWithStatuses(members: List<GroupMember>): List<GroupMember> {
        val userIds = members.mapNotNull { it.user.id }
        if (userIds.isEmpty()) return members

        val statusMap = runCatching {
            withContext(Dispatchers.IO) {
                chatInteractor.getUsersStatus(userIds)
            }.associateBy { it.userId }
        }.getOrElse { emptyMap() }

        if (statusMap.isEmpty()) return members

        return members.map { member ->
            val statusText = member.user.id?.let { id ->
                statusMap[id]?.let { status ->
                    formatStatus(status)
                }
            }
            member.copy(status = statusText)
        }
    }

    private fun formatStatus(status: UserStatus): String? {
        return if (status.isOnline) {
            chatStatusFormatter.online()
        } else {
            status.lastSeen?.let { lastSeen ->
                runCatching { Instant.parse(lastSeen) }
                    .map { instant -> chatStatusFormatter.formatLastSeen(instant) }
                    .getOrNull()
                    ?.takeIf { it.isNotBlank() }
            }
        }
    }
}

data class GroupMember(
    val user: UserProfileFullInfo,
    val isCreator: Boolean,
    val isAdmin: Boolean,
    val status: String? = null,
)

sealed class ChatCreateGroupUiState {
    object Loading : ChatCreateGroupUiState()
    data class Success(
        val groupName: String,
        val members: List<GroupMember>,
        val avatarPath: String?,
        val avatarId: String?,
        val isSaving: Boolean,
        val isAvatarUploading: Boolean,
    ) : ChatCreateGroupUiState()

    data class Error(val message: String) : ChatCreateGroupUiState()
}

sealed class ChatCreateGroupEvent {
    data class GroupCreated(val dialogId: Long) : ChatCreateGroupEvent()
    data class ShowError(val message: String?) : ChatCreateGroupEvent()
    object ShowImageUploadError : ChatCreateGroupEvent()
    object ShowMissingParticipantsError : ChatCreateGroupEvent()
}
