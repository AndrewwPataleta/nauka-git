package uddug.com.naukoteka.ui.chat.compose


import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import uddug.com.naukoteka.mvvm.chat.ChatDialogUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogViewModel
import uddug.com.naukoteka.ui.chat.compose.components.ChatInputBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatMessageItem
import uddug.com.naukoteka.ui.chat.compose.components.MessageFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatTopBar
import uddug.com.domain.entities.chat.MessageChat
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatDialogComponent(viewModel: ChatDialogViewModel, onBackPressed: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var selectedMessage by remember { mutableStateOf<MessageChat?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        val files = uris.mapNotNull { uri -> uriToFile(context, uri) }
        if (files.isNotEmpty()) {
            viewModel.attachFiles(files)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            filePickerLauncher.launch(arrayOf("*/*"))
        }
    }

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
                        status = state.status,
                        onBackPressed = { onBackPressed() },
                        onDetailClick = {
                            viewModel.onChatDetailClick()
                        }
                    )
                    // Список сообщений занимает всё оставшееся пространство экрана
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        reverseLayout = true,
                    ) {
                        items(messages) { message ->
                            ChatMessageItem(
                                message,
                                isMine = message.isMine,
                                onLongPress = { selectedMessage = it }
                            )
                        }
                    }

                    // ChatInputBar остается внизу экрана
                    ChatInputBar(
                        currentMessage = state.currentMessage,
                        attachedFiles = state.attachedFiles,
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
                        onAttachClick = {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        },
                        onRemoveFile = { file ->
                            viewModel.removeAttachedFile(file)
                        }
                    )
                }
            }
        }
        selectedMessage?.let { message ->
            MessageFunctionsBottomSheetDialog(
                message = message,
                onDismissRequest = { selectedMessage = null }
            )
        }
    }
}

private fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val input = context.contentResolver.openInputStream(uri) ?: return null
        val file = File.createTempFile("chat_attach_", null, context.cacheDir)
        input.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}
