package uddug.com.naukoteka.mvvm.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uddug.com.domain.entities.chat.Poll
import uddug.com.domain.interactors.chat.ChatInteractor
import uddug.com.naukoteka.ui.chat.ChatPollResultsFragment
import kotlin.math.roundToInt

@HiltViewModel
class ChatPollResultsViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val pollId: String = savedStateHandle.get<String>(ChatPollResultsFragment.ARG_POLL_ID).orEmpty()

    private val initialPoll: Poll? = savedStateHandle.get<String>(ChatPollResultsFragment.ARG_POLL_JSON)?.let { json ->
        try {
            com.google.gson.Gson().fromJson(json, Poll::class.java)
        } catch (_: Exception) {
            null
        }
    }

    private val _uiState = MutableStateFlow(ChatPollResultsUiState(isLoading = true))
    val uiState: StateFlow<ChatPollResultsUiState> = _uiState.asStateFlow()

    init {
        if (initialPoll != null) {
            _uiState.update { it.copy(isLoading = false, poll = initialPoll.toUi()) }
        }
        loadPoll()
    }

    fun loadPoll() {
        if (pollId.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = null, poll = null) }
            return
        }
        if (_uiState.value.poll == null) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        }
        viewModelScope.launch {
            try {
                val poll = withContext(Dispatchers.IO) { chatInteractor.getPoll(pollId) }
                val uiModel = poll.toUi()
                val hasData = uiModel.totalVotes > 0 || uiModel.options.any { it.percent > 0 }
                if (hasData) {
                    _uiState.update { state ->
                        state.copy(isLoading = false, errorMessage = null, poll = uiModel)
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(isLoading = false, errorMessage = null, poll = state.poll ?: uiModel)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = if (state.poll == null) e.message else null,
                    )
                }
            }
        }
    }

    private fun Poll.toUi(): PollResultsUiModel {
        val totalVotes = options.sumOf { it.voteCount }
        val safeTotal = if (totalVotes <= 0) 0 else totalVotes
        val optionUi = options.map { option ->
            val percent = option.percent
                ?: if (safeTotal > 0) {
                    ((option.voteCount.toDouble() / safeTotal) * 100).roundToInt()
                } else {
                    0
                }
            PollResultOptionUi(
                id = option.id,
                text = option.value,
                percent = percent.coerceIn(0, 100),
                isSelected = option.isVoted,
                isRightAnswer = option.isRightAnswer == true,
                description = option.description
            )
        }
        return PollResultsUiModel(
            id = id,
            question = subject,
            isAnonymous = isAnonymous,
            allowsMultipleAnswers = multipleAnswers,
            isQuiz = isQuiz,
            isStopped = isStopped,
            totalVotes = safeTotal,
            options = optionUi
        )
    }
}

data class ChatPollResultsUiState(
    val isLoading: Boolean = false,
    val poll: PollResultsUiModel? = null,
    val errorMessage: String? = null,
)

data class PollResultsUiModel(
    val id: String,
    val question: String,
    val isAnonymous: Boolean,
    val allowsMultipleAnswers: Boolean,
    val isQuiz: Boolean,
    val isStopped: Boolean,
    val totalVotes: Int,
    val options: List<PollResultOptionUi>,
)

data class PollResultOptionUi(
    val id: String,
    val text: String,
    val percent: Int,
    val isSelected: Boolean,
    val isRightAnswer: Boolean,
    val description: String?,
)
