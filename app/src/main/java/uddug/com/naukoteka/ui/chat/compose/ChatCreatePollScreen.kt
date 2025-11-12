package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreatePollUiState
import uddug.com.naukoteka.mvvm.chat.ChatCreatePollViewModel
import uddug.com.naukoteka.mvvm.chat.PollOptionUi

@Composable
fun ChatCreatePollScreen(
    viewModel: ChatCreatePollViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    val hasQuestion = state.question.isNotBlank()
    val filledOptions = state.options.count { it.text.isNotBlank() }
    val hasCorrectAnswer =
        !state.isQuizMode || state.options.any { it.isCorrect && it.text.isNotBlank() }
    val canCreate = hasQuestion && filledOptions >= 2 && hasCorrectAnswer

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chat_create_poll_title),
                        color = Color(0xFF1F1F1F),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            tint = Color(0xFF1F1F1F)
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        },
        backgroundColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.chat_create_poll_description),
                    color = Color(0xFF6F6F7B),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                QuestionSection(
                    state = state,
                    onQuestionChange = viewModel::onQuestionChange
                )
                SettingsSection(
                    state = state,
                    onAnonymousChange = viewModel::onAnonymousVotingChange,
                    onMultipleChange = viewModel::onMultipleAnswersChange,
                    onQuizModeChange = viewModel::onQuizModeChange
                )
                OptionsSection(
                    isQuizMode = state.isQuizMode,
                    options = state.options,
                    onOptionChange = viewModel::onOptionChange,
                    onCorrectAnswerToggle = viewModel::onCorrectAnswerToggle
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = viewModel::onCreatePoll,
                enabled = canCreate && !state.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF2E83D9),
                    contentColor = Color.White,
                    disabledBackgroundColor = Color(0x4D2E83D9),
                    disabledContentColor = Color.White
                )
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.chat_create_poll_create_button),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionSection(
    state: ChatCreatePollUiState,
    onQuestionChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.chat_create_poll_question_section),
            color = Color(0xFF1F1F1F),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = state.question,
            onValueChange = onQuestionChange,
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(R.string.chat_create_poll_question_placeholder),
                    color = Color(0xFFB0B2C3)
                )
            },
            singleLine = false,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color(0xFFF5F5F9),
                focusedBorderColor = Color(0xFF2E83D9),
                unfocusedBorderColor = Color(0xFFE0E0E8),
                cursorColor = Color(0xFF2E83D9),
                textColor = Color(0xFF1F1F1F),
                placeholderColor = Color(0xFFB0B2C3)
            )
        )
    }
}

@Composable
private fun SettingsSection(
    state: ChatCreatePollUiState,
    onAnonymousChange: (Boolean) -> Unit,
    onMultipleChange: (Boolean) -> Unit,
    onQuizModeChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.chat_create_poll_settings_section),
            color = Color(0xFF1F1F1F),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        PollSettingItem(
            title = stringResource(R.string.chat_create_poll_setting_anonymous),
            description = stringResource(R.string.chat_create_poll_setting_anonymous_description),
            checked = state.isAnonymous,
            onCheckedChange = onAnonymousChange
        )
        PollSettingItem(
            title = stringResource(R.string.chat_create_poll_setting_multiple),
            description = stringResource(R.string.chat_create_poll_setting_multiple_description),
            checked = state.allowMultipleAnswers,
            onCheckedChange = onMultipleChange
        )
        PollSettingItem(
            title = stringResource(R.string.chat_create_poll_setting_quiz),
            description = stringResource(R.string.chat_create_poll_setting_quiz_description),
            checked = state.isQuizMode,
            onCheckedChange = onQuizModeChange
        )
    }
}

@Composable
private fun PollSettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
//            .background(Color(0xFFF5F5F9), RoundedCornerShape(16.dp))
//            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFF6F6F7B),
                fontSize = 18.sp,
                lineHeight = 18.sp
            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = description,
//                color = Color(0xFF6F6F7B),
//                fontSize = 13.sp,
//                lineHeight = 18.sp
//            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2E83D9),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE0E0E8)
            )
        )
    }
}

@Composable
private fun OptionsSection(
    isQuizMode: Boolean,
    options: List<PollOptionUi>,
    onOptionChange: (Long, String) -> Unit,
    onCorrectAnswerToggle: (Long, Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.chat_create_poll_options_section),
            color = Color(0xFF1F1F1F),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        options.forEach { option ->
            key(option.id) {
                PollOptionField(
                    isQuizMode = isQuizMode,
                    value = option.text,
                    isCorrect = option.isCorrect,
                    onValueChange = { onOptionChange(option.id, it) },
                    onCorrectToggle = { checked -> onCorrectAnswerToggle(option.id, checked) }
                )
            }
        }
    }
}

@Composable
private fun PollOptionField(
    isQuizMode: Boolean,
    value: String,
    isCorrect: Boolean,
    onValueChange: (String) -> Unit,
    onCorrectToggle: (Boolean) -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(R.string.chat_create_poll_option_placeholder),
                color = Color(0xFF8F8FA0)
            )
        },
        leadingIcon = {
            if (isQuizMode) {
                Checkbox(
                    checked = isCorrect,
                    onCheckedChange = onCorrectToggle,
                    enabled = value.isNotBlank(),
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF2E83D9),
                        uncheckedColor = Color(0xFFB0B2C3),
                        checkmarkColor = Color.White,
                        disabledColor = Color(0xFFE0E0E8)
                    )
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color(0xFFF5F5F9),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFF2E83D9),
            textColor = Color(0xFF1F1F1F),
            placeholderColor = Color(0xFF8F8FA0),
            leadingIconColor = if (isQuizMode) Color.Unspecified else Color(0xFFB0B2C3),
            disabledLeadingIconColor = if (isQuizMode) Color.Unspecified else Color(0xFFB0B2C3)
        )
    )
}
