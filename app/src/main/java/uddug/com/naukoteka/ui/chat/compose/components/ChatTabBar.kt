package uddug.com.naukoteka.ui.chat.compose.components

import ChatAttachmentPreview
import ChatCard
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit

import androidx.compose.material.icons.outlined.Settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var folderActionsTarget by remember { mutableStateOf<FolderActionsTarget?>(null) }
    var folderToRename by remember { mutableStateOf<ChatFolder?>(null) }
    var folderToDelete by remember { mutableStateOf<ChatFolder?>(null) }

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
        folderActionsTarget?.let { target ->
            val folder = target.folder
            AlertDialog(
                onDismissRequest = { folderActionsTarget = null },
                title = { Text(text = folder.name) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FolderActionItem(
                            icon = Icons.Outlined.Settings,
                            text = stringResource(R.string.chat_folder_menu_configure)
                        ) {
                            folderActionsTarget = null
                            onOpenFolderSettings()
                        }
                        if (!target.isMainFolder) {
                            FolderActionItem(
                                icon = Icons.Outlined.Edit,
                                text = stringResource(R.string.chat_folder_action_rename)
                            ) {
                                folderActionsTarget = null
                                folderToRename = folder
                            }
                        }
                        FolderActionItem(
                            icon = Icons.Outlined.Done,
                            text = stringResource(R.string.chat_folder_action_mark_read)
                        ) {
                            folderActionsTarget = null
                            viewModel.markFolderAsRead(folder.id)
                        }
                        FolderActionItem(
                            icon = Icons.Outlined.Edit,
                            text = stringResource(R.string.chat_folder_menu_change_order)
                        ) {
                            folderActionsTarget = null
                            onChangeFolderOrder()
                        }
                        if (!target.isMainFolder) {
                            Divider()
                            FolderActionItem(
                                icon = Icons.Outlined.Delete,
                                text = stringResource(R.string.chat_folder_action_delete)
                            ) {
                                folderActionsTarget = null
                                folderToDelete = folder
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {}
            )
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (folders.isNotEmpty()) {
                    TabRow(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White),
                        selectedTabIndex = selectedTabIndex,
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                val safeIndex = selectedTabIndex.coerceIn(tabPositions.indices)
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[safeIndex]),
                                    color = Color(0xFF2E83D9)
                                )
                            }
                        },
                        divider = {},
                    ) {
                        folders.forEachIndexed { index, folder ->
                            val onTabClick = {
                                selectedTabIndex = index
                                viewModel.onFolderSelected(folder.id)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White)
                                    .pointerInput(mainFolderId, folder.id) {
                                        detectTapGestures(
                                            onTap = { onTabClick() },
                                            onLongPress = {
                                                folderActionsTarget = FolderActionsTarget(
                                                    folder = folder,
                                                    isMainFolder = mainFolderId != null && folder.id == mainFolderId
                                                )
                                            }
                                        )
                                    }
                            ) {
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = onTabClick,
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
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                IconButton(
                    onClick = {
                        val folder = folders.getOrNull(selectedTabIndex)
                        if (folder != null) {
                            folderActionsTarget = FolderActionsTarget(
                                folder = folder,
                                isMainFolder = mainFolderId != null && folder.id == mainFolderId
                            )
                        } else {
                            onOpenFolderSettings()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.chat_folder_menu_configure),
                        tint = Color(0xFF8083A0)
                    )
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
                            val firstFile = chat.lastMessage.files?.firstOrNull()
                            val attachmentPreview = firstFile?.let { file ->
                                val type = when (file.fileType) {
                                    1 -> ChatAttachmentType.IMAGE
                                    2 -> ChatAttachmentType.VIDEO
                                    else -> ChatAttachmentType.FILE
                                }
                                val path = if (type == ChatAttachmentType.IMAGE) file.path else null
                                if (type != ChatAttachmentType.IMAGE || !path.isNullOrBlank()) {
                                    ChatAttachmentPreview(
                                        path = path,
                                        type = type,
                                    )
                                } else {
                                    null
                                }
                            }
                            val isGroupChat = chat.dialogType != 1
                            val isFromMe = viewModel.isMessageFromMe(chat.lastMessage.ownerId)
                            val authorName = if (isGroupChat && !isFromMe) {
                                val ownerId = chat.lastMessage.ownerId
                                val author = chat.users.firstOrNull { it.userId == ownerId }
                                    ?: chat.interlocutor.takeIf { it.userId == ownerId }
                                author?.fullName?.takeIf { it.isNotBlank() }
                                    ?: author?.nickname?.takeIf { it.isNotBlank() }
                            } else {
                                null
                            }
                            ChatCard(
                                dialogId = chat.dialogId,
                                avatarUrl = avatarUrl,
                                name = displayName,
                                message = chat.lastMessage.text ?: stringResource(R.string.chat_no_messages),
                                time = chat.lastMessage.createdAt ?: stringResource(R.string.chat_unknown_time),
                                newMessagesCount = chat.unreadMessages,
                                attachmentPreview = attachmentPreview,
                                isRepost = false,
                               //     chat.lastMessage.type == 5,
                                isGroupChat = isGroupChat,
                                isFromMe = isFromMe,
                                authorName = authorName,
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

private data class FolderActionsTarget(
    val folder: ChatFolder,
    val isMainFolder: Boolean,
)

@Composable
private fun FolderActionItem(
    icon: Painter,
    text: String,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            Image(icon, contentDescription = null, tint = Color(0xFF2E83D9))
        },
        headlineContent = {
            Text(text = text)
        }
    )
}
