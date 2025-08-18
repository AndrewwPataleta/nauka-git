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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.ui.chat.compose.components.ChatFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatTabBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarComponent
import uddug.com.naukoteka.ui.chat.compose.components.SearchField

@Composable
fun ChatListComponent(
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
    onCreateChatClick: () -> Unit,
    onShowAttachments: (Long) -> Unit,
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
        SearchField(
            title = stringResource(R.string.find_chat_message),
            query = "",
            onSearchChanged = {

            }
        )
        ChatTabBar(
            viewModel = viewModel,
            onChatLongClick = { id -> selectedDialogId = id },
            isSelectionMode = isSelectionMode,
            selectedChats = selectedChats,
            onChatSelect = { viewModel.toggleChatSelection(it) }
        )
    }

    selectedDialogId?.let { id ->
        val isBlocked = (uiState as? ChatListUiState.Success)?.chats?.firstOrNull { it.dialogId == id }?.isBlocked ?: false
        ChatFunctionsBottomSheetDialog(
            dialogId = id,
            isBlocked = isBlocked,
            onDismissRequest = { selectedDialogId = null },
            onShowAttachments = onShowAttachments,
            onSelectMessages = {
                viewModel.startSelection(id)
                selectedDialogId = null
            }
        )
    }
}
