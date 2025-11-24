package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatEditFolderState
import uddug.com.naukoteka.mvvm.chat.ChatEditFolderViewModel
import uddug.com.naukoteka.mvvm.chat.ChatFolderSelectionItem
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@Composable
fun ChatEditFolderScreen(
    viewModel: ChatEditFolderViewModel,
    onBackPressed: () -> Unit,
    onAddChatsClick: () -> Unit,
    onRetryClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val isActionEnabled = state.folderName.isNotBlank() && !state.isSaving && state.errorMessage == null && !state.isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chat_edit_folder_title),
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_back),
                            contentDescription = null,
                            tint = Color(0xFF2E83D9)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onSaveClick() },
                        enabled = isActionEnabled
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_create_apply),
                            contentDescription = null,
                            tint = if (isActionEnabled) Color(0xFF2E83D9) else Color(0x4D2E83D9)
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2E83D9))
                }
            }

            state.errorMessage != null -> {
                ChatEditFolderError(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding),
                    message = state.errorMessage,
                    onRetryClick = onRetryClick
                )
            }

            else -> {
                ChatEditFolderContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    state = state,
                    onNameChanged = viewModel::onFolderNameChanged,
                    onAddChatsClick = onAddChatsClick,
                    onChatRemoved = viewModel::onChatRemoved
                )
            }
        }
    }
}

@Composable
private fun ChatEditFolderError(
    modifier: Modifier,
    message: String?,
    onRetryClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message ?: stringResource(id = R.string.chat_edit_folder_generic_error),
            color = Color(0xFF8083A0),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.chat_edit_folder_retry),
            color = Color(0xFF2E83D9),
            modifier = Modifier.clickable(onClick = onRetryClick)
        )
    }
}

@Composable
private fun ChatEditFolderContent(
    modifier: Modifier,
    state: ChatEditFolderState,
    onNameChanged: (String) -> Unit,
    onAddChatsClick: () -> Unit,
    onChatRemoved: (Long) -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.chat_edit_folder_description),
            color = Color(0xFF8083A0),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.chat_create_folder_name_label),
            color = Color.Black,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.folderName,
            onValueChange = onNameChanged,
            placeholder = {
                Text(text = stringResource(R.string.chat_create_folder_name_placeholder))
            },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color(0xFFF5F5F9),
                focusedBorderColor = Color(0xFF2E83D9),
                unfocusedBorderColor = Color(0xFFE0E0E8),
                cursorColor = Color(0xFF2E83D9),
                textColor = Color(0xFF1F1F1F),
                placeholderColor = Color(0xFFB0B2C3)
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.chat_create_folder_selected_title),
                color = Color.Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = stringResource(R.string.chat_create_folder_add),
                color = Color(0xFF2E83D9),
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .clickable { onAddChatsClick() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (state.selectedChats.isEmpty()) {
            Text(
                text = stringResource(R.string.chat_create_folder_empty_placeholder),
                color = Color(0xFF8083A0),
                fontSize = 14.sp
            )
        } else {
            LazyColumn {
                items(state.selectedChats, key = { it.dialogId }) { chat ->
                    ChatEditFolderRow(
                        item = chat,
                        onRemove = { onChatRemoved(chat.dialogId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatEditFolderRow(
    item: ChatFolderSelectionItem,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(url = item.avatarUrl, name = item.initials, size = 48.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = item.title,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    color = Color(0xFF8083A0),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color(0xFFBFC4D5)
            )
        }
    }
}
