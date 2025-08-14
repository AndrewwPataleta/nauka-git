package uddug.com.naukoteka.ui.chat.compose


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uddug.com.naukoteka.mvvm.chat.ChatDialogUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogViewModel
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.ui.chat.compose.components.ChatInputBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatMessageItem
import uddug.com.naukoteka.ui.chat.compose.components.ChatMessagesList
import uddug.com.naukoteka.ui.chat.compose.components.ChatTabBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarComponent
import uddug.com.naukoteka.ui.chat.compose.components.ChatTopBar
import uddug.com.naukoteka.ui.chat.compose.components.SearchField
import androidx.compose.foundation.layout.navigationBarsPadding

import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material.Scaffold
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatDialogComponent(viewModel: ChatDialogViewModel, onBackPressed: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .systemBarsPadding() // Учитываем панель состояния (статус бар)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {


            when (uiState) {
                is ChatDialogUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ChatDialogUiState.Error -> {
                    val error = (uiState as ChatDialogUiState.Error).message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error, color = Color.Red)
                    }
                }

                is ChatDialogUiState.Success -> {
                    val state = uiState as ChatDialogUiState.Success
                    val messages = state.chats
                    ChatTopBar(
                        name = state.chatName,
                        image = state.chatImage,
                        isGroup = state.isGroup,
                        onBackPressed = { onBackPressed() },
                        onDetailClick = {
                            viewModel.onChatDetailClick()
                        }
                    )
                    // LazyColumn будет использовать оставшееся пространство
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f) // Занимает оставшееся место
                    ) {
                        LazyColumn(
                            state = scrollState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            reverseLayout = true,
                        ) {
                            items(messages) { message ->
                                ChatMessageItem(message, isMine = message.isMine)
                            }
                        }
                    }

                    // ChatInputBar остается внизу экрана
                    ChatInputBar(
                        currentMessage = state.currentMessage,
                        onMessageChange = { newMessage ->
                            viewModel.updateCurrentMessage(newMessage)
                        },
                        onSendClick = {
                            scope.launch {
                                viewModel.sendMessage(state.currentMessage)
                                scrollState.animateScrollToItem(0)
                                // keyboardController?.hide()
                            }
                        },


                        )
                }
            }
        }
    }
}
