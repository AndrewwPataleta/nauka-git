package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateFolderState
import uddug.com.naukoteka.mvvm.chat.ChatCreateFolderViewModel
import uddug.com.naukoteka.mvvm.chat.ChatFolderSelectionItem
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import uddug.com.naukoteka.ui.theme.NauTheme

@Composable
fun ChatCreateFolderScreen(
    viewModel: ChatCreateFolderViewModel,
    onBackPressed: () -> Unit,
    onAddChatsClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    // Пустую папку бэк не принимает (HTTP 400) — требуем хотя бы один чат,
    // блокируя кнопку "Создать", пока чаты не выбраны.
    val isActionEnabled =
        state.folderName.isNotBlank() && state.selectedChats.isNotEmpty() && !state.isSaving

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chat_create_folder_title),
                        fontSize = 20.sp,
                        color = MaterialTheme.colors.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_back),
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onCreateFolderClick() },
                        enabled = isActionEnabled
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_create_apply),
                            contentDescription = null,
                            tint = if (isActionEnabled) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.3f)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_create_folder_description),
                color = NauTheme.extendedColors.inactive,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.chat_create_folder_name_label),
                color = MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.folderName,
                onValueChange = viewModel::onFolderNameChanged,
                placeholder = {
                    Text(text = stringResource(R.string.chat_create_folder_name_placeholder))
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = NauTheme.extendedColors.inputBackground,
                    focusedBorderColor = MaterialTheme.colors.primary,
                    unfocusedBorderColor = NauTheme.extendedColors.inputStroke,
                    cursorColor = MaterialTheme.colors.primary,
                    textColor = MaterialTheme.colors.onBackground,
                    placeholderColor = NauTheme.extendedColors.inactive
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
                    color = MaterialTheme.colors.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.chat_create_folder_add),
                    color = MaterialTheme.colors.primary,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clickable{ onAddChatsClick() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (state.selectedChats.isEmpty()) {
                Text(
                    text = stringResource(R.string.chat_create_folder_empty_placeholder),
                    color = NauTheme.extendedColors.inactive,
                    fontSize = 14.sp
                )
            } else {
                LazyColumn {
                    items(state.selectedChats, key = { it.dialogId }) { chat ->
                        SelectedChatRow(
                            item = chat,
                            onRemove = { viewModel.onChatRemoved(chat.dialogId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedChatRow(
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
                color = MaterialTheme.colors.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.subtitle?.takeIf { it.isNotBlank() }?.let { subtitle ->
                Text(
                    text = subtitle,
                    color = NauTheme.extendedColors.inactive,
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
                tint = NauTheme.extendedColors.inactive
            )
        }
    }
}
