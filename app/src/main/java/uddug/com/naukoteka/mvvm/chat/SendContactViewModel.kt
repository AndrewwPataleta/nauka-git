package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.chat.ChatInteractor
import javax.inject.Inject

data class SendContactUiState(
    val query: String = "",
    val contacts: List<UserProfileFullInfo> = emptyList()
)

@HiltViewModel
class SendContactViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendContactUiState())
    val uiState: StateFlow<SendContactUiState> = _uiState

    init {
        loadContacts("")
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        loadContacts(query)
    }

    private fun loadContacts(query: String) {
        viewModelScope.launch {
            val users = try {
                chatInteractor.searchUsers(query)
            } catch (e: Exception) {
                emptyList()
            }
            _uiState.update { it.copy(contacts = users) }
        }
    }
}

