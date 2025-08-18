package uddug.com.naukoteka.ui.chat.compose.components

import ChatCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel


enum class MessageState {
    REPOST,  // Репост
    MEDIA,   // Медиа-вложение
    FROM_ME, // Сообщение от меня
    NORMAL   // Обычное сообщение
}

enum class TabContent(val content: String) {
    ALL("Все сообщения"),
    PEOPLE("Сообщения от людей"),
    WORK("Сообщения по работе"),
    SCIENCE("Научные сообщения"),
    STUDY("Учебные сообщения")
}

@Composable
fun ChatTabBar(
    viewModel: ChatListViewModel,
    onChatLongClick: (Long) -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabTitles = listOf("Все", "Люди", "Работа", "Наука", "Учеба")

    val uiState by viewModel.uiState.collectAsState()

    val selectedTabContent = TabContent.values()[selectedTabIndex]

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                divider = {}
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        modifier = Modifier.background(Color.White),
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    maxLines = 1,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Bold,
                                        color = if (selectedTabIndex == index) Color.Black else Color(
                                            0xFF8083A0
                                        )
                                    )
                                )
//                            Spacer(modifier = Modifier.width(4.dp))
//                            if (tabCounts[index].isNotEmpty()) {
//                                Box(
//                                    contentAlignment = Alignment.Center,
//                                    modifier = Modifier
//                                        .size(20.dp)
//                                        .background(Color.Blue, shape = CircleShape)
//                                ) {
//                                    Text(
//                                        text = tabCounts[index],
//                                        color = Color.White,
//                                        fontSize = 12.sp,
//                                        fontWeight = FontWeight.Bold
//                                    )
//                                }
//                            }
                            }
                        }
                    )
                }
            }


            when (uiState) {
                is ChatListUiState.Error -> {

                }

                ChatListUiState.Loading -> {

                }

                is ChatListUiState.Success -> {

                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTabContent) {
                            TabContent.ALL -> {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items((uiState as ChatListUiState.Success).chats) { chat ->
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
                                            onChatClick = {
                                                viewModel.onChatClick(it)
                                            },
                                            onChatLongClick = {
                                                onChatLongClick(it)
                                            }
                                        )
                                    }
                                }
                            }

                            TabContent.PEOPLE -> {

                            }

                            TabContent.WORK -> {

                            }

                            TabContent.SCIENCE -> {

                            }

                            TabContent.STUDY -> {

                            }
                        }
                    }
                }
            }
        }



    }



}
