package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import uddug.com.domain.entities.chat.DialogInfo
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.domain.entities.chat.User
import uddug.com.domain.interactors.chat.ChatInteractor
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

    private var currentUserId: String? = null

    fun selectTab(index: Int) {
        _selectedTabIndex.value = index
    }

    fun loadDialogInfo(dialogId: Long) {
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
