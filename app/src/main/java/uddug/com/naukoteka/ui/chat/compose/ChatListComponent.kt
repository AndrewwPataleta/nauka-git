package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.SearchResult
import uddug.com.naukoteka.ui.chat.compose.components.ChatFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatListShimmer
import uddug.com.naukoteka.ui.chat.compose.components.ChatTabBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarComponent
import uddug.com.naukoteka.ui.chat.compose.components.SearchField
import uddug.com.naukoteka.ui.chat.compose.components.SearchResultItem

@Composable
fun ChatListComponent(
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
    onCreateChatClick: () -> Unit,
    onShowAttachments: (Long) -> Unit,
    onFolderSettings: () -> Unit,
    onChangeFolderOrder: () -> Unit,
) {
    var selectedDialogId by remember { mutableStateOf<Long?>(null) }
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedChats by viewModel.selectedChats.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(color = Color.White)
    ) {
        ChatToolbarComponent(
            viewModel = viewModel,
            onCreateChatClick = { onCreateChatClick() },
            onBackPressed = { onBackPressed() },
            isSelectionMode = isSelectionMode,
            selectedCount = selectedChats.size,
            onCloseSelection = { viewModel.clearSelection() },
            onDeleteSelected = { viewModel.deleteSelectedChats() },
            onMoreClick = { }
        )
        var query by remember { mutableStateOf("") }
        val searchResults by viewModel.searchResults.collectAsState()
        val isSearchLoading by viewModel.isSearchLoading.collectAsState()

        SearchField(
            title = stringResource(R.string.find_chat_message),
            query = query,
            onSearchChanged = {
                query = it
                viewModel.search(it)
            }
        )
        if (query.length < 4) {
            ChatTabBar(
                viewModel = viewModel,
                onChatLongClick = { id -> selectedDialogId = id },
                isSelectionMode = isSelectionMode,
                selectedChats = selectedChats,
                onChatSelect = { viewModel.toggleChatSelection(it) },
                onOpenFolderSettings = onFolderSettings,
                onChangeFolderOrder = onChangeFolderOrder
            )
        } else {
            when {
                isSearchLoading -> ChatListShimmer()
                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(searchResults) { result ->
                        SearchResultItem(result = result) {
                            viewModel.onChatClick(it)
                        }
                    }
                }
            }
        }
    }

    selectedDialogId?.let { id ->
        val chat = (uiState as? ChatListUiState.Success)?.chats?.firstOrNull { it.dialogId == id }
        val isBlocked = chat?.isBlocked ?: false
        val isPinned = chat?.isPinned ?: false
        val notificationsDisabled = chat?.notificationsDisable ?: false
        ChatFunctionsBottomSheetDialog(
            dialogId = id,
            isBlocked = isBlocked,
            isPinned = isPinned,
            notificationsDisabled = notificationsDisabled,
            onDismissRequest = { selectedDialogId = null },
            onShowAttachments = onShowAttachments,
            onSelectMessages = {
                viewModel.startSelection(id)
                selectedDialogId = null
            },
            onPinChange = { dialogId, pinned ->
                viewModel.updateDialogPin(dialogId, pinned)
            },
            onNotificationsChange = { dialogId, disabled ->
                viewModel.updateDialogNotifications(dialogId, disabled)
            }
        )
    }
}
