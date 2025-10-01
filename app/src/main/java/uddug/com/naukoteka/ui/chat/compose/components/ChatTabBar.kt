package uddug.com.naukoteka.ui.chat.compose.components

import ChatCard
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MarkChatRead
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uddug.com.domain.entities.chat.ChatFolder
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatTabBar(
    viewModel: ChatListViewModel,
    onChatLongClick: (Long) -> Unit,
    isSelectionMode: Boolean,
    selectedChats: Set<Long>,
    onChatSelect: (Long) -> Unit,
    onOpenFolderSettings: () -> Unit,
    onChangeFolderOrder: () -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val folders by viewModel.folders.collectAsState()
    val currentFolderId by viewModel.currentFolderId.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    val mainFolderId = folders.firstOrNull()?.id
    var bottomSheetState by remember { mutableStateOf<FolderBottomSheetState?>(null) }
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var folderToRename by remember { mutableStateOf<ChatFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<ChatFolder?>(null) }

    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState != null) {
            modalBottomSheetState.show()
        }
    }

    LaunchedEffect(folders, currentFolderId) {
        if (folders.isEmpty()) {
            selectedTabIndex = 0
        } else {
            val newIndex = folders.indexOfFirst { it.id == currentFolderId }
            val resolvedIndex = if (newIndex >= 0) newIndex else 0
            if (selectedTabIndex != resolvedIndex) {
                selectedTabIndex = resolvedIndex
            }
        }
    }

    Box {
        bottomSheetState?.let { sheetState ->
            ModalBottomSheet(
                sheetState = modalBottomSheetState,
                onDismissRequest = {
                    coroutineScope.launch {
                        modalBottomSheetState.hide()
                        bottomSheetState = null
                    }
                }
            ) {
                when (sheetState) {
                    FolderBottomSheetState.Main -> {
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        modalBottomSheetState.hide()
                                        bottomSheetState = null
                                        onOpenFolderSettings()
                                    }
                                },
                            leadingContent = {
                                Icon(Icons.Outlined.Settings, contentDescription = null)
                            },
                            headlineContent = {
                                Text(text = stringResource(R.string.chat_folder_menu_configure))
                            }
                        )
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        modalBottomSheetState.hide()
                                        bottomSheetState = null
                                        onChangeFolderOrder()
                                    }
                                },
                            leadingContent = {
                                Icon(Icons.Outlined.Sort, contentDescription = null)
                            },
                            headlineContent = {
                                Text(text = stringResource(R.string.chat_folder_menu_change_order))
                            }
                        )
                    }

                    is FolderBottomSheetState.Folder -> {
                        val folder = sheetState.folder
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        modalBottomSheetState.hide()
                                        bottomSheetState = null
                                        folderToRename = folder
                                    }
                                },
                            leadingContent = {
                                Icon(Icons.Outlined.Edit, contentDescription = null)
                            },
                            headlineContent = {
                                Text(text = stringResource(R.string.chat_folder_action_rename))
                            }
                        )
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        modalBottomSheetState.hide()
                                        bottomSheetState = null
                                        viewModel.markFolderAsRead(folder.id)
                                    }
                                },
                            leadingContent = {
                                Icon(Icons.Outlined.MarkChatRead, contentDescription = null)
                            },
                            headlineContent = {
                                Text(text = stringResource(R.string.chat_folder_action_mark_read))
                            }
                        )
                        ListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        modalBottomSheetState.hide()
                                        bottomSheetState = null
                                        folderToDelete = folder
                                    }
                                },
                            leadingContent = {
                                Icon(Icons.Outlined.Delete, contentDescription = null)
                            },
                            headlineContent = {
                                Text(text = stringResource(R.string.chat_folder_action_delete))
                            }
                        )
                    }
                }
            }
        }

        folderToRename?.let { folder ->
            var name by remember(folder) { mutableStateOf(folder.name) }
            AlertDialog(
                onDismissRequest = { folderToRename = null },
                title = { Text(text = stringResource(R.string.chat_folder_action_rename_title)) },
                text = {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.chat_folder_action_rename_placeholder)) }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.renameFolder(folder.id, name.trim())
                            folderToRename = null
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text(text = stringResource(R.string.chat_folder_action_rename_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { folderToRename = null }) {
                        Text(text = stringResource(R.string.chat_folder_action_delete_confirm_negative))
                    }
                }
            )
        }

        folderToDelete?.let { folder ->
            AlertDialog(
                onDismissRequest = { folderToDelete = null },
                title = { Text(text = stringResource(R.string.chat_folder_action_delete_confirm_title)) },
                text = {
                    Text(
                        text = stringResource(
                            R.string.chat_folder_action_delete_confirm_message,
                            folder.name
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteFolder(folder.id)
                            folderToDelete = null
                        }
                    ) {
                        Text(text = stringResource(R.string.chat_folder_action_delete_confirm_positive))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { folderToDelete = null }) {
                        Text(text = stringResource(R.string.chat_folder_action_delete_confirm_negative))
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (folders.isNotEmpty()) {
                TabRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    selectedTabIndex = selectedTabIndex,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF2E83D9)
                        )
                    },
                    divider = {},
                ) {
                    folders.forEachIndexed { index, folder ->
                        Box(
                            modifier = Modifier
                                .background(Color.White)
                                .combinedClickable(
                                    onClick = {
                                        selectedTabIndex = index
                                        viewModel.onFolderSelected(folder.id)
                                    },
                                    onLongClick = {
                                        bottomSheetState = if (mainFolderId != null && folder.id == mainFolderId) {
                                            FolderBottomSheetState.Main
                                        } else {
                                            FolderBottomSheetState.Folder(folder)
                                        }
                                    }
                                )
                        ) {
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = {
                                    selectedTabIndex = index
                                    viewModel.onFolderSelected(folder.id)
                                },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = folder.name,
                                            maxLines = 1,
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedTabIndex == index) Color.Black else Color(
                                                    0xFF8083A0
                                                )
                                            )
                                        )
                                        if (folder.unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Badge(
                                                containerColor = Color(0xFF2E83D9),
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    text = folder.unreadCount.toString(),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            when (val state = uiState) {
                is ChatListUiState.Error -> {

                }

                ChatListUiState.Loading -> {
                    ChatListShimmer()
                }

                is ChatListUiState.Success -> {
                    val chats = state.chats
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(chats) { chat ->
                            val displayName = when {
                                chat.dialogType != 1 && chat.dialogName.isNotBlank() -> chat.dialogName
                                !chat.interlocutor.fullName.isNullOrBlank() -> chat.interlocutor.fullName!!
                                !chat.interlocutor.nickname.isNullOrBlank() -> chat.interlocutor.nickname!!
                                chat.dialogName.isNotBlank() -> chat.dialogName
                                else -> stringResource(R.string.group_chat)
                            }
                            val avatarUrl = chat.dialogImage?.path?.takeIf { it.isNotBlank() }
                                ?: chat.interlocutor.image
                            ChatCard(
                                dialogId = chat.dialogId,
                                avatarUrl = avatarUrl,
                                name = displayName,
                                message = chat.lastMessage.text ?: stringResource(R.string.chat_no_messages),
                                time = chat.lastMessage.createdAt ?: stringResource(R.string.chat_unknown_time),
                                newMessagesCount = chat.unreadMessages,
                                attachment = chat.lastMessage.files?.firstOrNull()?.path,
                                isRepost = false,
                               //     chat.lastMessage.type == 5,
                                isMedia = chat.lastMessage.files?.isNotEmpty() == true,
                                isFromMe = chat.lastMessage.ownerId == "",
                                notificationsDisable = chat.notificationsDisable,
                                isPinned = chat.isPinned,
                                isMuted = chat.notificationsDisable,
                                selectionMode = isSelectionMode,
                                isSelected = selectedChats.contains(chat.dialogId),
                                onSelectChange = { onChatSelect(chat.dialogId) },
                                onChatClick = {
                                    viewModel.onChatClick(it)
                                },
                                onChatLongClick = {
                                    if (!isSelectionMode) onChatLongClick(it) else onChatSelect(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private sealed class FolderBottomSheetState {
    object Main : FolderBottomSheetState()
    data class Folder(val folder: ChatFolder) : FolderBottomSheetState()
}
