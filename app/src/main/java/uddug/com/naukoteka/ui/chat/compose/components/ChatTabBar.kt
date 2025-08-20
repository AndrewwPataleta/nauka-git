package uddug.com.naukoteka.ui.chat.compose.components

import ChatCard
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel

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
    var showFolderMenu by remember { mutableStateOf(false) }

    val folders by viewModel.folders.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    Box {
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
                        Box {
                            Tab(
                                modifier = Modifier
                                    .background(Color.White)
                                    .let {
                                        if (index == 0) {
                                            it.pointerInput(Unit) {
                                                detectTapGestures(onLongPress = { showFolderMenu = true })
                                            }
                                        } else {
                                            it
                                        }
                                    },
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
                                                Text(text = folder.unreadCount.toString(), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            )
                            if (index == 0) {
                                DropdownMenu(
                                    expanded = showFolderMenu,
                                    onDismissRequest = { showFolderMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Настроить папки") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Folder, contentDescription = null)
                                        },
                                        onClick = {
                                            showFolderMenu = false
                                            onOpenFolderSettings()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Изменить порядок") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Sort, contentDescription = null)
                                        },
                                        onClick = {
                                            showFolderMenu = false
                                            onChangeFolderOrder()
                                        }
                                    )
                                }
                            }
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
                            ChatCard(
                                dialogId = chat.dialogId,
                                avatarUrl = chat.interlocutor.image,
                                name = chat.interlocutor.fullName ?: "Неизвестный",
                                message = chat.lastMessage.text ?: "Нет сообщений",
                                time = chat.lastMessage.createdAt ?: "Неизвестно",
                                newMessagesCount = chat.unreadMessages,
                                attachment = chat.lastMessage.files?.firstOrNull()?.path,
                                isRepost = chat.lastMessage.type == 5,
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
