package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import uddug.com.naukoteka.mvvm.chat.ChatFolderAddChatsUiState
import uddug.com.naukoteka.mvvm.chat.ChatFolderAddChatsViewModel
import uddug.com.naukoteka.mvvm.chat.ChatFolderSelectionItem
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import uddug.com.naukoteka.ui.chat.compose.components.SearchField
import uddug.com.naukoteka.ui.theme.NauTheme

@Composable
fun ChatFolderAddChatsScreen(
    viewModel: ChatFolderAddChatsViewModel,
    onBackPressed: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val isConfirmEnabled = (state as? ChatFolderAddChatsUiState.Success)?.selected?.isNotEmpty() == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chat_select_chats_title),
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
                        onClick = { viewModel.onConfirmSelection() },
                        enabled = isConfirmEnabled
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_create_apply),
                            contentDescription = null,
                            tint = if (isConfirmEnabled) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.3f)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp
            )
        }
    ) { padding ->
        when (val current = state) {
            ChatFolderAddChatsUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colors.primary)
                }
            }

            is ChatFolderAddChatsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .padding(padding),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = current.message, color = MaterialTheme.colors.error)
                }
            }

            is ChatFolderAddChatsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .padding(padding)
                ) {
                    SearchField(
                        title = stringResource(R.string.chat_select_chats_search_placeholder),
                        query = current.query,
                        onSearchChanged = viewModel::onQueryChanged,
                        showClearIcon = current.query.isNotEmpty(),
                        onClearClick = { viewModel.onQueryChanged("") }
                    )
                    SelectedChatsRow(current.selectedItems, viewModel::onChatClick)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        val items = if (current.query.isNotBlank()) current.searchResults else current.chats
                        if (items.isNotEmpty()) {
                            if (current.query.isBlank()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.chat_select_chats_section_all),
                                        color = MaterialTheme.colors.onBackground,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                            items(items, key = { it.dialogId }) { item ->
                                SelectableChatRow(
                                    item = item,
                                    isSelected = current.selected.contains(item.dialogId),
                                    onClick = { viewModel.onChatClick(item.dialogId) }
                                )
                            }
                        } else {
                            item {
                                Text(
                                    text = stringResource(R.string.chat_select_chats_empty),
                                    color = NauTheme.extendedColors.inactive,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedChatsRow(
    selected: List<ChatFolderSelectionItem>,
    onChipClick: (Long) -> Unit,
) {
    if (selected.isEmpty()) return
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(selected, key = { it.dialogId }) { item ->
            Surface(
                color = NauTheme.extendedColors.backgroundMoreInfo,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { onChipClick(item.dialogId) }
                ) {
                    Text(
                        text = item.title,
                        color = MaterialTheme.colors.onBackground,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = NauTheme.extendedColors.inactive,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectableChatRow(
    item: ChatFolderSelectionItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
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
        androidx.compose.material3.Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.primary)
        )
    }
}
