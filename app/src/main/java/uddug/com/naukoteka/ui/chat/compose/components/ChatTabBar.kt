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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.domain.entities.chat.MessageType
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
    onEditFolder: (Long) -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val folders by viewModel.folders.collectAsState()
    val currentFolderId by viewModel.currentFolderId.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    val mainFolderId = folders.firstOrNull()?.id
    var folderActionsTarget by remember { mutableStateOf<FolderActionsTarget?>(null) }
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
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        FolderActionItem(
                            icon = painterResource(R.drawable.ic_create_new_folder),
                            text = stringResource(R.string.chat_folder_menu_configure)
                        ) {
                            folderActionsTarget = null
                            onOpenFolderSettings()
                        }
                        if (!target.isMainFolder) {
                            FolderActionItem(
                                icon = painterResource(R.drawable.ic_settings),
                                text = stringResource(R.string.chat_folder_action_rename)
                            ) {
                                folderActionsTarget = null
                                onEditFolder(folder.id)
                            }
                        }
                        FolderActionItem(
                            icon = painterResource(R.drawable.ic_nonread_chat_folder),
                            text = stringResource(R.string.chat_folder_action_mark_read)
                        ) {
                            folderActionsTarget = null
                            viewModel.markFolderAsRead(folder.id)
                        }
                        FolderActionItem(
                            icon = painterResource(R.drawable.ic_swap_possition_folder),
                            text = stringResource(R.string.chat_folder_menu_change_order)
                        ) {
                            folderActionsTarget = null
                            onChangeFolderOrder()
                        }
                        if (!target.isMainFolder) {
                            Divider()
                            FolderActionItem(
                                icon = painterResource(R.drawable.ic_remove_folder),
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
                    Button(
                        onClick = {
                            viewModel.deleteFolder(folder.id)
                            folderToDelete = null
                        }
                    ) {
                        Text(text = stringResource(R.string.chat_folder_action_delete_confirm_positive))
                    }
                },
                dismissButton = {
                    Button(onClick = { folderToDelete = null }) {
                        Text(text = stringResource(R.string.chat_folder_action_delete_confirm_negative))
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.main_background)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.main_background)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (folders.isNotEmpty()) {
                    TabRow(
                        modifier = Modifier
                            .weight(1f)
                            .background(colorResource(id = R.color.main_background)),
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
                                    .background(colorResource(id = R.color.main_background))
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
                                                    color = if (selectedTabIndex == index) {
                                                        colorResource(id = R.color.main_text)
                                                    } else {
                                                        colorResource(id = R.color.secondary_text)
                                                    }
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
                        tint = colorResource(id = R.color.secondary_text)
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
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
                            val messageType = MessageType.fromInt(chat.lastMessage.type ?: 0)
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
                                message = chat.lastMessage.text.orEmpty(),
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
                                messageType = messageType,
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
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        leadingContent = {
            Image(painter = icon, contentDescription = null)
        },
        headlineContent = {
            Text(text = text)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    )
}

