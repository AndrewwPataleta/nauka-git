package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ChatCreatePollViewModel @Inject constructor() : ViewModel() {

    private var nextOptionId = 0L

    private fun createOption(text: String = "") = PollOptionUi(id = nextOptionId++, text = text)

    private val _uiState = MutableStateFlow(
        ChatCreatePollUiState(
            options = listOf(createOption(), createOption())
        )
    )
    val uiState: StateFlow<ChatCreatePollUiState> = _uiState

    private val _events = MutableSharedFlow<ChatCreatePollEvent>()
    val events: SharedFlow<ChatCreatePollEvent> = _events.asSharedFlow()

    fun onQuestionChange(question: String) {
        _uiState.update { it.copy(question = question) }
    }

    fun onAnonymousVotingChange(enabled: Boolean) {
        _uiState.update { it.copy(isAnonymous = enabled) }
    }

    fun onMultipleAnswersChange(enabled: Boolean) {
        _uiState.update { it.copy(allowMultipleAnswers = enabled) }
    }

    fun onQuizModeChange(enabled: Boolean) {
        _uiState.update { it.copy(isQuizMode = enabled) }
    }

    fun onOptionChange(id: Long, text: String) {
        _uiState.update { state ->
            val updated = state.options.map { option ->
                if (option.id == id) option.copy(text = text) else option
            }
            state.copy(options = normalizeOptions(updated))
        }
    }

    fun onCreatePoll() {
        val state = _uiState.value
        val options = state.options.filter { it.text.isNotBlank() }
        if (state.question.isBlank() || options.size < 2) {
            viewModelScope.launch {
                _events.emit(ChatCreatePollEvent.ValidationError())
            }
            return
        }
        val poll = ChatCreatePollData(
            question = state.question,
            options = options.map { it.text.trim() },
            isAnonymous = state.isAnonymous,
            allowMultipleAnswers = state.allowMultipleAnswers,
            isQuizMode = state.isQuizMode
        )
        viewModelScope.launch {
            _events.emit(ChatCreatePollEvent.PollCreated(poll))
        }
    }

    private fun normalizeOptions(options: List<PollOptionUi>): List<PollOptionUi> {
        val filled = options.filter { it.text.isNotBlank() }
        return if (filled.isEmpty()) {
            val blanks = options.filter { it.text.isBlank() }
            val preserved = blanks.take(2)
            val missing = 2 - preserved.size
            if (missing > 0) {
                preserved + List(missing) { createOption() }
            } else {
                preserved
            }
        } else {
            val blankOption = options.filter { it.text.isBlank() }.lastOrNull() ?: createOption()
            var result = filled + blankOption
            if (result.last().text.isNotBlank()) {
                result = result + createOption()
            }
            result
        }
    }
}

data class ChatCreatePollUiState(
    val question: String = "",
    val options: List<PollOptionUi> = emptyList(),
    val isAnonymous: Boolean = true,
    val allowMultipleAnswers: Boolean = false,
    val isQuizMode: Boolean = false
)

data class PollOptionUi(
    val id: Long,
    val text: String = ""
)

data class ChatCreatePollData(
    val question: String,
    val options: List<String>,
    val isAnonymous: Boolean,
    val allowMultipleAnswers: Boolean,
    val isQuizMode: Boolean
)

sealed class ChatCreatePollEvent {
    class ValidationError(val message: String? = null) : ChatCreatePollEvent()
    data class PollCreated(val poll: ChatCreatePollData) : ChatCreatePollEvent()
}
