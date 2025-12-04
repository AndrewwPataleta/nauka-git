package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatPollResultsUiState
import uddug.com.naukoteka.mvvm.chat.ChatPollResultsViewModel
import uddug.com.naukoteka.mvvm.chat.PollResultOptionUi
import uddug.com.naukoteka.mvvm.chat.PollResultsUiModel
import uddug.com.naukoteka.ui.theme.NauTheme

@Composable
fun ChatPollResultsScreen(
    viewModel: ChatPollResultsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    ChatPollResultsScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::loadPoll
    )
}

@Composable
fun ChatPollResultsScreen(
    state: ChatPollResultsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.chat_poll_results_title),
                        color = primaryTextColor(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = primaryTextColor()
                        )
                    }
                },
                backgroundColor = surfaceColor(),
                elevation = 0.dp
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentColor()
                    )
                }
                state.poll != null -> {
                    PollResultsContent(model = state.poll)
                }
                else -> {
                    PollResultsError(
                        modifier = Modifier.align(Alignment.Center),
                        message = state.errorMessage,
                        onRetry = onRetry
                    )
                }
            }
        }
    }
}

@Composable
private fun PollResultsContent(model: PollResultsUiModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(surfaceColor())
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(id = R.string.chat_poll_results_question_label),
                    color = secondaryTextColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 16.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = questionBackgroundColor(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = questionBorderColor(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = model.question,
                        color = primaryTextColor(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            val pollTypeText = if (model.isAnonymous) {
                stringResource(id = R.string.chat_poll_results_anonymous)
            } else {
                stringResource(id = R.string.chat_poll_results_public)
            }
            val votesText = pluralStringResource(
                id = R.plurals.chat_poll_results_votes,
                count = model.totalVotes,
                model.totalVotes
            )
            Text(
                text = stringResource(
                    id = R.string.chat_poll_results_meta,
                    pollTypeText,
                    votesText
                ),
                color = secondaryTextColor(),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            if (model.allowsMultipleAnswers) {
                Text(
                    text = stringResource(id = R.string.chat_poll_results_multiple_allowed),
                    color = secondaryTextColor(),
                    fontSize = 12.sp
                )
            }
            if (model.isQuiz) {
                Text(
                    text = stringResource(id = R.string.chat_poll_results_quiz_mode),
                    color = secondaryTextColor(),
                    fontSize = 12.sp
                )
            }
            if (model.isStopped) {
                Text(
                    text = stringResource(id = R.string.chat_poll_results_stopped),
                    color = secondaryTextColor(),
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = stringResource(id = R.string.chat_poll_results_options_section),
            color = primaryTextColor(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            model.options.forEach { option ->
                PollResultOptionItem(option)
            }
        }
    }
}

@Composable
private fun PollResultOptionItem(option: PollResultOptionUi) {
    val background = when {
        option.isRightAnswer -> accentColor().copy(alpha = 0.08f)
        option.isSelected -> accentColor().copy(alpha = 0.05f)
        else -> surfaceColor()
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${option.percent}%",
                color = primaryTextColor(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = option.text,
                    color = primaryTextColor(),
                    fontSize = 15.sp,
                    fontWeight = if (option.isSelected || option.isRightAnswer) FontWeight.SemiBold else FontWeight.Normal,
                    lineHeight = 20.sp
                )
                option.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        color = secondaryTextColor(),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                if (option.isRightAnswer) {
                    Text(
                        text = stringResource(id = R.string.chat_poll_results_correct_answer),
                        color = accentColor(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (option.isSelected) {
                    Text(
                        text = stringResource(id = R.string.chat_poll_results_your_choice),
                        color = accentColor(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = option.percent.coerceIn(0, 100) / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(3.dp)),
            backgroundColor = questionBorderColor(),
            color = accentColor()
        )
    }
}

@Composable
private fun PollResultsError(
    modifier: Modifier = Modifier,
    message: String?,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.chat_poll_results_error),
            color = secondaryTextColor(),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        message?.takeIf { it.isNotBlank() }?.let { details ->
            Text(
                text = details,
                color = secondaryTextColor(),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        TextButton(
            onClick = onRetry,
            colors = ButtonDefaults.textButtonColors(contentColor = accentColor())
        ) {
            Text(
                text = stringResource(id = R.string.chat_poll_results_retry),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun primaryTextColor() = MaterialTheme.colors.onSurface

@Composable
private fun secondaryTextColor() = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)

@Composable
private fun accentColor() = MaterialTheme.colors.primary

@Composable
private fun surfaceColor() = MaterialTheme.colors.surface

@Composable
private fun questionBackgroundColor() = NauTheme.extendedColors.backgroundMoreInfo

@Composable
private fun questionBorderColor() = NauTheme.extendedColors.inputStroke
