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
import kotlinx.coroutines.rx2.await
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.profile.Image
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.domain.repositories.user_profile.UserProfileRepository
import uddug.com.naukoteka.mvvm.chat.ChatStatusTextMode.GENERIC
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

    private val dialogId: Long = savedStateHandle.get<Long>(DIALOG_ID_KEY) ?: 0L

    private val _uiState = MutableStateFlow<ChatEditGroupUiState>(ChatEditGroupUiState.Loading)
    val uiState: StateFlow<ChatEditGroupUiState> = _uiState

    private val _events = MutableSharedFlow<ChatEditGroupEvent>()
    val events: SharedFlow<ChatEditGroupEvent> = _events.asSharedFlow()

    private var currentUser: UserProfileFullInfo? = null
    private var initialName: String = ""
    private var initialAvatarId: String? = null
    private var initialMembers: Set<String> = emptySet()

    init {
        if (dialogId == 0L) {
            _uiState.value = ChatEditGroupUiState.Error("Dialog not found")
        } else {
            loadDialogInfo()
        }
    }

    private fun loadDialogInfo() {
        viewModelScope.launch {
            try {
                val profile = withContext(Dispatchers.IO) {
                    userProfileRepository.getProfileInfo().await()
                }
                currentUser = profile
                val dialogInfo = chatInteractor.getDialogInfo(dialogId)
                applyDialogInfo(dialogInfo)
            } catch (e: Exception) {
                _uiState.value = ChatEditGroupUiState.Error(e.message ?: "Error")
            }
        }
    }

    private suspend fun applyDialogInfo(dialogInfo: DialogInfo) {
        val currentUserId = currentUser?.id
        val members = dialogInfo.users.orEmpty().map { user ->
            val userProfile = UserProfileFullInfo(
                id = user.userId,
                fullName = user.fullName,
                nickname = user.nickname,
                image = Image(path = user.image)
            )
            val isCreator = user.userId != null && user.userId == currentUserId
            val isAdmin = user.isAdmin || isAdminRole(user.role) || isOwnerRole(user.role)
            GroupMember(
                user = userProfile,
                isCreator = isCreator,
                isAdmin = isAdmin,
            )
        }
        val membersWithStatus = enrichWithStatuses(members)
        initialName = dialogInfo.name.orEmpty()
        initialAvatarId = dialogInfo.dialogImage?.id
        initialMembers = membersWithStatus
            .filterNot { it.isCreator }
            .mapNotNull { it.user.id }
            .toSet()
        _uiState.value = ChatEditGroupUiState.Success(
            groupName = dialogInfo.name.orEmpty(),
            members = membersWithStatus,
            avatarPath = dialogInfo.dialogImage?.path,
            avatarId = dialogInfo.dialogImage?.id,
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
        _uiState.value = current.copy(isAvatarUploading = true, isAvatarRemoved = false)
        viewModelScope.launch {
            try {
                val uploaded = withContext(Dispatchers.IO) {
                    chatInteractor.uploadFiles(listOf(file), raw = false)
                }
                val uploadedFile = uploaded.firstOrNull()
                val latestState = _uiState.value as? ChatEditGroupUiState.Success
                if (uploadedFile != null && latestState != null) {
                    _uiState.value = latestState.copy(
                        avatarPath = uploadedFile.path,
                        avatarId = uploadedFile.id,
                        isAvatarUploading = false,
                        isAvatarRemoved = false,
                    )
                } else {
                    resetAvatarUploading()
                    _events.emit(ChatEditGroupEvent.ShowImageUploadError)
                }
            } catch (e: Exception) {
                resetAvatarUploading()
                _events.emit(ChatEditGroupEvent.ShowError(e.message ?: "Error"))
            }
        }
    }

    private suspend fun resetAvatarUploading() {
        val latestState = _uiState.value as? ChatEditGroupUiState.Success ?: return
        _uiState.value = latestState.copy(isAvatarUploading = false)
    }

    fun onRemoveAvatar() {
        val current = _uiState.value as? ChatEditGroupUiState.Success ?: return
        if (current.avatarPath == null && current.avatarId == null) return
        _uiState.value = current.copy(
            avatarPath = null,
            avatarId = null,
            isAvatarRemoved = true,
        )
    }

    fun onRemoveMember(memberId: String) {
        updateMembers { members ->
            members.filterNot { member ->
                member.user.id == memberId && !member.isCreator
            }
        }
    }

    fun onMembersSelected(users: List<UserProfileFullInfo>) {
        val current = _uiState.value as? ChatEditGroupUiState.Success ?: return
        val existingIds = current.members.mapNotNull { it.user.id }.toSet()
        val filtered = users.filter { user ->
            val id = user.id
            id != null && id != currentUser?.id && !existingIds.contains(id)
        }
        if (filtered.isEmpty()) return
        viewModelScope.launch {
            val currentState = _uiState.value as? ChatEditGroupUiState.Success ?: return@launch
            val newMembers = filtered.map { user ->
                GroupMember(
                    user = user,
                    isCreator = user.id == currentUser?.id,
                    isAdmin = false,
                )
            }
            val enriched = enrichWithStatuses(currentState.members + newMembers)
            _uiState.value = currentState.copy(members = enriched)
        }
    }

    fun onSaveChanges() {
        val current = _uiState.value as? ChatEditGroupUiState.Success ?: return
        if (current.isSaving || current.isAvatarUploading) return
        val participants = current.members.filterNot { it.isCreator }
        if (participants.size < MIN_MEMBERS) {
            viewModelScope.launch { _events.emit(ChatEditGroupEvent.ShowMissingParticipantsError) }
            return
        }

        val trimmedName = current.groupName.trim()
        val nameToSend = trimmedName.takeIf { it.isNotBlank() && it != initialName }
        val participantsIds = participants.mapNotNull { it.user.id }
        val usersToSend = if (participantsIds.toSet() != initialMembers) participantsIds else null
        val clearImage = current.isAvatarRemoved && !initialAvatarId.isNullOrEmpty()
        val imageIdToSend = current.avatarId?.takeIf { !current.isAvatarRemoved && it != initialAvatarId }

        if (nameToSend == null && usersToSend == null && !clearImage && imageIdToSend == null) {
            viewModelScope.launch {
                val info = runCatching { chatInteractor.getDialogInfo(dialogId) }.getOrNull()
                if (info != null) {
                    applyDialogInfo(info)
                    _events.emit(ChatEditGroupEvent.GroupUpdated(info))
                } else {
                    _events.emit(ChatEditGroupEvent.GroupUpdated(null))
                }
            }
            return
        }

        _uiState.value = current.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val updatedInfo = chatInteractor.updateDialog(
                    dialogId = dialogId,
                    name = nameToSend,
                    imageId = imageIdToSend,
                    clearImage = clearImage,
                    users = usersToSend,
                )
                applyDialogInfo(updatedInfo)
                _events.emit(ChatEditGroupEvent.GroupUpdated(updatedInfo))
            } catch (e: Exception) {
                val latestState = _uiState.value as? ChatEditGroupUiState.Success
                if (latestState != null) {
                    _uiState.value = latestState.copy(isSaving = false)
                }
                _events.emit(ChatEditGroupEvent.ShowError(e.message ?: "Error"))
            }
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
            withContext(Dispatchers.IO) { chatInteractor.getUsersStatus(userIds) }
                .associateBy { it.userId }
        }.getOrElse { emptyMap() }
        if (statusMap.isEmpty()) return members
        return members.map { member ->
            val status = member.user.id?.let { id -> statusMap[id] }
            val statusText = status?.let {
                if (it.isOnline) {
                    chatStatusFormatter.online()
                } else {
                    it.lastSeen?.let { lastSeen ->
                        runCatching { Instant.parse(lastSeen) }
                            .map { instant -> chatStatusFormatter.formatLastSeen(instant, GENERIC) }
                            .getOrNull()
                    }
                }
            }
            member.copy(status = statusText)
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
        const val DIALOG_ID_KEY = "dialog_id"
        private const val MAX_GROUP_NAME_LENGTH = 100
        private const val MIN_MEMBERS = 2
        private const val ROLE_OWNER = "37:201"
        private const val ROLE_ADMIN = "37:202"
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
    data class GroupUpdated(val dialogInfo: DialogInfo?) : ChatEditGroupEvent()
    data class ShowError(val message: String) : ChatEditGroupEvent()
    object ShowImageUploadError : ChatEditGroupEvent()
    object ShowMissingParticipantsError : ChatEditGroupEvent()
}
