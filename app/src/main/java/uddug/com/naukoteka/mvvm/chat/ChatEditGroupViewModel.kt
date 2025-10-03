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
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.User
import uddug.com.domain.entities.profile.Image
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import java.io.File
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ChatEditGroupViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    private val userProfileRepository: UserProfileRepository,
    private val chatStatusFormatter: ChatStatusFormatter,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val ARG_DIALOG_ID = "dialog_id"
        private const val MAX_GROUP_NAME_LENGTH = 100
        private const val ADMIN_ROLE = "37:202"
    }

    private val dialogId: Long = savedStateHandle.get<Long>(ARG_DIALOG_ID) ?: 0L

    private val _uiState = MutableStateFlow<ChatEditGroupUiState>(ChatEditGroupUiState.Loading)
    val uiState: StateFlow<ChatEditGroupUiState> = _uiState

    private val _events = MutableSharedFlow<ChatEditGroupEvent>()
    val events: SharedFlow<ChatEditGroupEvent> = _events.asSharedFlow()

    private var currentUserId: String? = null
    private var originalName: String? = null
    private var originalAvatarId: String? = null
    private var originalMembers: Set<String> = emptySet()
    private var originalAdminIds: Set<String> = emptySet()
    private var removeAvatar: Boolean = false

    init {
        if (dialogId != 0L) {
            loadDialogInfo()
        } else {
            _uiState.value = ChatEditGroupUiState.Error("Invalid dialog")
        }
    }

    private fun loadDialogInfo() {
        viewModelScope.launch {
            try {
                val info = chatInteractor.getDialogInfo(dialogId)
                val currentUser = withContext(Dispatchers.IO) {
                    userProfileRepository.getProfileInfo().await()
                }
                currentUserId = currentUser.id
                applyDialogInfo(info, currentUser.id)
            } catch (e: Exception) {
                _uiState.value = ChatEditGroupUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun applyDialogInfo(info: DialogInfo, currentUserId: String?) {
        val members = buildMembers(info.users.orEmpty(), currentUserId)
        val membersWithStatuses = enrichWithStatuses(members)
        originalName = info.name
        originalAvatarId = info.dialogImage?.id
        originalMembers = membersWithStatuses
            .mapNotNull { it.user.id }
            .filter { it != currentUserId }
            .toSet()
        originalAdminIds = membersWithStatuses
            .filter { it.isAdmin && !it.isCreator }
            .mapNotNull { it.user.id }
            .toSet()
        removeAvatar = false
        _uiState.value = ChatEditGroupUiState.Success(
            groupName = info.name.orEmpty(),
            members = membersWithStatuses,
            avatarPath = info.dialogImage?.path,
            avatarId = info.dialogImage?.id,
            isSaving = false,
            isAvatarUploading = false,
            isAvatarRemoved = false,
        )
    }

    fun onGroupNameChanged(name: String) {
        val current = _uiState.value
        if (current is ChatEditGroupUiState.Success) {
            val trimmed = name.take(MAX_GROUP_NAME_LENGTH)
            _uiState.value = current.copy(groupName = trimmed)
        }
    }

    fun onAvatarSelected(file: File) {
        val current = _uiState.value as? ChatEditGroupUiState.Success ?: return
        if (current.isAvatarUploading) return
        _uiState.value = current.copy(isAvatarUploading = true)
        viewModelScope.launch {
            try {
                val uploaded = withContext(Dispatchers.IO) {
                    chatInteractor.uploadFiles(listOf(file), raw = false)
                }.firstOrNull()
                if (uploaded != null) {
                    val latest = _uiState.value as? ChatEditGroupUiState.Success
                    if (latest != null) {
                        _uiState.value = latest.copy(
                            avatarPath = uploaded.path,
                            avatarId = uploaded.id,
                            isAvatarUploading = false,
                            isAvatarRemoved = false,
                        )
                        removeAvatar = false
                    }
                } else {
                    val latest = _uiState.value as? ChatEditGroupUiState.Success
                    _uiState.value = latest?.copy(isAvatarUploading = false) ?: current.copy(isAvatarUploading = false)
                    _events.emit(ChatEditGroupEvent.ShowImageUploadError)
                }
            } catch (e: Exception) {
                val latest = _uiState.value as? ChatEditGroupUiState.Success
                _uiState.value = latest?.copy(isAvatarUploading = false) ?: current.copy(isAvatarUploading = false)
                _events.emit(ChatEditGroupEvent.ShowError(e.message))
            }
        }
    }

    fun onAvatarRemoved() {
        val current = _uiState.value as? ChatEditGroupUiState.Success ?: return
        if (current.avatarPath.isNullOrBlank() && current.avatarId.isNullOrBlank()) return
        _uiState.value = current.copy(avatarPath = null, avatarId = null, isAvatarRemoved = true)
        removeAvatar = true
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

    fun onParticipantsAdded(users: List<UserProfileFullInfo>) {
        if (users.isEmpty()) return
        updateMembers { members ->
            val existingIds = members.mapNotNull { it.user.id }.toSet()
            val newMembers = users
                .filter { it.id != null && !existingIds.contains(it.id) }
                .map {
                    GroupMember(
                        user = it,
                        isCreator = false,
                        isAdmin = false
                    )
                }
            (members + newMembers)
        }
        viewModelScope.launch {
            val current = _uiState.value as? ChatEditGroupUiState.Success ?: return@launch
            val enriched = enrichWithStatuses(current.members)
            _uiState.value = current.copy(members = enriched)
        }
    }

    fun onSaveChanges() {
        val current = _uiState.value as? ChatEditGroupUiState.Success ?: return
        if (current.isSaving || current.isAvatarUploading) return
        val participantsCount = current.members.count { !it.isCreator }
        if (participantsCount < 2) {
            viewModelScope.launch { _events.emit(ChatEditGroupEvent.ShowMissingParticipantsError) }
            return
        }
        val name = current.groupName.take(MAX_GROUP_NAME_LENGTH)
        val nameChanged = name != originalName && name.isNotBlank()
        val imageId = current.avatarId
        val imageChanged = !imageId.isNullOrBlank() && imageId != originalAvatarId
        val users = current.members.mapNotNull { it.user.id }
        val currentUserId = currentUserId
        if (currentUserId.isNullOrEmpty()) {
            viewModelScope.launch { _events.emit(ChatEditGroupEvent.ShowError("User not found")) }
            return
        }
        val usersForRequest = users.filter { it != currentUserId }
        val usersChanged = usersForRequest.toSet() != originalMembers
        val currentAdmins = current.members
            .filter { it.isAdmin && !it.isCreator }
            .mapNotNull { it.user.id }
            .toSet()
        val adminsAdded = currentAdmins.minus(originalAdminIds)
        val adminsRemoved = originalAdminIds.minus(currentAdmins)
        _uiState.value = current.copy(isSaving = true)
        viewModelScope.launch {
            try {
                val updatedInfo = withContext(Dispatchers.IO) {
                    chatInteractor.updateGroupDialog(
                        dialogId = dialogId,
                        dialogName = if (nameChanged) name else null,
                        imageId = if (imageChanged) imageId else null,
                        removeImage = removeAvatar,
                        users = if (usersChanged) usersForRequest else null,
                    )
                }
                adminsAdded.forEach { userId ->
                    runCatching { chatInteractor.makeDialogAdmin(dialogId, userId) }
                }
                adminsRemoved.forEach { userId ->
                    runCatching { chatInteractor.removeDialogAdmin(dialogId, userId) }
                }
                applyDialogInfo(updatedInfo, currentUserId)
                _events.emit(ChatEditGroupEvent.GroupUpdated(dialogId))
            } catch (e: Exception) {
                val latest = _uiState.value as? ChatEditGroupUiState.Success
                _uiState.value = latest?.copy(isSaving = false) ?: current.copy(isSaving = false)
                _events.emit(ChatEditGroupEvent.ShowError(e.message))
            } finally {
                removeAvatar = false
            }
        }
    }

    private fun buildMembers(users: List<User>, currentUserId: String?): List<GroupMember> {
        return users.map { user ->
            val userInfo = UserProfileFullInfo(
                id = user.userId,
                fullName = user.fullName,
                nickname = user.nickname,
                image = Image(path = user.image)
            )
            val isOwner = isOwnerRole(user.role)
            val isAdmin = user.isAdmin || isAdminRole(user.role) || isOwner
            val isCreator = isOwner || user.userId == currentUserId
            GroupMember(
                user = userInfo,
                isCreator = isCreator,
                isAdmin = isAdmin
            )
        }
    }

    private fun updateMembers(transform: (List<GroupMember>) -> List<GroupMember>) {
        val current = _uiState.value as? ChatEditGroupUiState.Success ?: return
        val updated = transform(current.members)
        _uiState.value = current.copy(members = updated)
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
                    if (status.isOnline) {
                        chatStatusFormatter.online()
                    } else {
                        status.lastSeen?.let { lastSeen ->
                            runCatching { Instant.parse(lastSeen) }
                                .map { instant -> chatStatusFormatter.formatLastSeen(instant) }
                                .getOrNull()
                        }
                    }
                }
            }
            member.copy(status = statusText)
        }
    }

    private fun isOwnerRole(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        val normalized = role.lowercase()
        return normalized == "37:201" || normalized.contains("owner") || normalized.contains("влад")
    }

    private fun isAdminRole(role: String?): Boolean {
        if (role.isNullOrBlank()) return false
        val normalized = role.lowercase()
        return normalized == ADMIN_ROLE || normalized.contains("admin") || normalized.contains("админ")
    }
}

sealed class ChatEditGroupUiState {
    object Loading : ChatEditGroupUiState()
    data class Success(
        val groupName: String,
        val members: List<GroupMember>,
        val avatarPath: String?,
        val avatarId: String?,
        val isSaving: Boolean,
        val isAvatarUploading: Boolean,
        val isAvatarRemoved: Boolean,
    ) : ChatEditGroupUiState()

    data class Error(val message: String) : ChatEditGroupUiState()
}

sealed class ChatEditGroupEvent {
    data class GroupUpdated(val dialogId: Long) : ChatEditGroupEvent()
    data class ShowError(val message: String?) : ChatEditGroupEvent()
    object ShowImageUploadError : ChatEditGroupEvent()
    object ShowMissingParticipantsError : ChatEditGroupEvent()
}
