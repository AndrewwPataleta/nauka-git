package uddug.com.naukoteka.mvvm.chat

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uddug.com.domain.entities.chat.Poll
import uddug.com.domain.entities.chat.PollOptionInput
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.naukoteka.R

@HiltViewModel
class ChatCreatePollViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
) : ViewModel() {

    private var nextOptionId = 0L

    private fun createOption(text: String = "", isCorrect: Boolean = false) =
        PollOptionUi(id = nextOptionId++, text = text, isCorrect = isCorrect)

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
        _uiState.update { state ->
            val options = if (enabled) {
                state.options
            } else {
                state.options.map { it.copy(isCorrect = false) }
            }
            state.copy(
                isQuizMode = enabled,
                options = normalizeOptions(options)
            )
        }
    }

    fun onOptionChange(id: Long, text: String) {
        _uiState.update { state ->
            val updated = state.options.map { option ->
                if (option.id == id) {
                    val sanitized = option.copy(text = text)
                    if (text.isBlank() && option.isCorrect) {
                        sanitized.copy(isCorrect = false)
                    } else {
                        sanitized
                    }
                } else {
                    option
                }
            }
            state.copy(options = normalizeOptions(updated))
        }
    }

    fun onCorrectAnswerToggle(id: Long, checked: Boolean) {
        _uiState.update { state ->
            if (!state.isQuizMode) return@update state
            val target = state.options.firstOrNull { it.id == id } ?: return@update state
            if (target.text.isBlank()) return@update state
            val updated = state.options.map { option ->
                when (option.id) {
                    id -> option.copy(isCorrect = checked)
                    else -> if (checked) option.copy(isCorrect = false) else option
                }
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
        if (state.isQuizMode && options.none { it.isCorrect }) {
            viewModelScope.launch {
                _events.emit(
                    ChatCreatePollEvent.ValidationError(
                        messageResId = R.string.chat_create_poll_quiz_validation_error
                    )
                )
            }
            return
        }
        val trimmedOptions = options.map { it.copy(text = it.text.trim()) }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                val poll = withContext(Dispatchers.IO) {
                    chatInteractor.createPoll(
                        subject = state.question.trim(),
                        isAnonymous = state.isAnonymous,
                        multipleAnswers = state.allowMultipleAnswers,
                        isQuiz = state.isQuizMode,
                        options = trimmedOptions.map { option ->
                            PollOptionInput(
                                value = option.text,
                                isRightAnswer = if (state.isQuizMode) {
                                    if (option.isCorrect) true else false
                                } else {
                                    null
                                }
                            )
                        }
                    )
                }
                _events.emit(ChatCreatePollEvent.PollCreated(poll))
            } catch (e: Exception) {
                _events.emit(ChatCreatePollEvent.ValidationError(message = e.message))
            } finally {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun normalizeOptions(options: List<PollOptionUi>): List<PollOptionUi> {
        val sanitized = options.map { option ->
            if (option.text.isBlank() && option.isCorrect) {
                option.copy(isCorrect = false)
            } else {
                option
            }
        }
        val filled = sanitized.filter { it.text.isNotBlank() }
        return if (filled.isEmpty()) {
            val blanks = sanitized.filter { it.text.isBlank() }.map { it.copy(isCorrect = false) }
            val preserved = blanks.take(2)
            val missing = 2 - preserved.size
            if (missing > 0) {
                preserved + List(missing) { createOption() }
            } else {
                preserved
            }
        } else {
            val blanks = sanitized.filter { it.text.isBlank() }.map { it.copy(isCorrect = false) }
            val blankOption = blanks.lastOrNull() ?: createOption()
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
    val isQuizMode: Boolean = false,
    val isSubmitting: Boolean = false,
)

data class PollOptionUi(
    val id: Long,
    val text: String = "",
    val isCorrect: Boolean = false,
)

sealed class ChatCreatePollEvent {
    class ValidationError(
        val message: String? = null,
        @StringRes val messageResId: Int? = null,
    ) : ChatCreatePollEvent()

    data class PollCreated(val poll: Poll) : ChatCreatePollEvent()
}
