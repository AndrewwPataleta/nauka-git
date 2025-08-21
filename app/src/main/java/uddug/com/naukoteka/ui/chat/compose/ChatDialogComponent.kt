package uddug.com.naukoteka.ui.chat.compose


import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import uddug.com.naukoteka.mvvm.chat.ChatDialogUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogViewModel
import uddug.com.naukoteka.mvvm.chat.ContactInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.chat.compose.components.ChatInputBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatMessageItem
import uddug.com.naukoteka.ui.chat.compose.components.MessageFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.AttachOptionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatTopBar
import uddug.com.naukoteka.ui.chat.compose.components.MessageListShimmer
import uddug.com.domain.entities.chat.MessageChat
import java.io.File
import uddug.com.naukoteka.ui.chat.AudioRecorder

private enum class AttachmentPickerType { MEDIA, FILE, CONTACT }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatDialogComponent(
    viewModel: ChatDialogViewModel,
    onBackPressed: () -> Unit,
    onSearchClick: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    var selectedMessage by remember { mutableStateOf<MessageChat?>(null) }
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedMessages by viewModel.selectedMessages.collectAsState()
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var pendingPickerType by remember { mutableStateOf<AttachmentPickerType?>(null) }

    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            audioRecorder.start()
            isRecording = true
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        val files = uris.mapNotNull { uri -> uriToFile(context, uri) }
        if (files.isNotEmpty()) {
            viewModel.attachFiles(files)
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            getContactInfo(context, it)?.let { info ->
                viewModel.attachContact(info)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            when (pendingPickerType) {
                AttachmentPickerType.MEDIA ->
                    filePickerLauncher.launch(arrayOf("image/*", "video/*"))
                AttachmentPickerType.FILE ->
                    filePickerLauncher.launch(arrayOf("*/*"))
                AttachmentPickerType.CONTACT ->
                    contactPickerLauncher.launch(null)
                null -> {}
            }
        }
        pendingPickerType = null
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


            when (val state = uiState) {
                is ChatDialogUiState.Loading -> {
                    ChatTopBar(
                        name = state.chatName,
                        image = state.chatImage,
                        isGroup = state.isGroup,
                        status = state.status,
                        firstParticipantName = state.firstParticipantName,
                        onDetailClick = {},
                        onSearchClick = onSearchClick,
                        onBackPressed = { onBackPressed() },
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        MessageListShimmer()
                    }
                }

                is ChatDialogUiState.Error -> {
                    val error = state.message
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error, color = Color.Red)
                    }
                }

                is ChatDialogUiState.Success -> {
                    val messages = state.chats
                    if (isSelectionMode) {
                        TopAppBar(
                            title = {
                                Text(text = selectedMessages.size.toString(), fontSize = 20.sp, color = Color.Black)
                            },
                            navigationIcon = {
                                IconButton(onClick = { viewModel.clearSelection() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "Close",
                                        tint = Color.Black
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { viewModel.deleteSelectedMessages() }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_trash),
                                        contentDescription = "Delete",
                                        tint = Color.Black
                                    )
                                }
                            },
                            backgroundColor = Color.White,
                            elevation = 4.dp
                        )
                    } else {
                        ChatTopBar(
                            name = state.chatName,
                            image = state.chatImage,
                            isGroup = state.isGroup,
                            status = state.status,
                            firstParticipantName = state.firstParticipantName,
                            onDetailClick = {
                                viewModel.onChatDetailClick()
                            },
                            onSearchClick = onSearchClick,
                            onBackPressed = { onBackPressed() },
                        )
                    }
                    // Список сообщений занимает всё оставшееся пространство экрана
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        items(messages) { message ->
                            ChatMessageItem(
                                message = message,
                                isMine = message.isMine,
                                    selectionMode = isSelectionMode,
                                    isSelected = selectedMessages.contains(message.id),
                                    onSelectChange = { viewModel.toggleMessageSelection(message.id) },
                                onLongPress = { selectedMessage = it }
                            )
                        }
                    }

                    // ChatInputBar остается внизу экрана
                    ChatInputBar(
                        currentMessage = state.currentMessage,
                        attachedFiles = state.attachedFiles,
                        replyMessage = state.replyMessage,
                        isRecording = isRecording,
                        onMessageChange = { newMessage ->
                            viewModel.updateCurrentMessage(newMessage)
                        },
                        onSendClick = {
                            scope.launch {
                                viewModel.sendMessage(state.currentMessage)
                                scrollState.animateScrollToItem(messages.size - 1)
                                // keyboardController?.hide()
                            }
                        },
                        onVoiceClick = {
                            if (isRecording) {
                                audioRecorder.stop()?.let { file ->
                                    scope.launch {
                                        viewModel.sendVoiceMessage(file)
                                        scrollState.animateScrollToItem(messages.size - 1)
                                    }
                                }
                                isRecording = false
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onAttachClick = {
                            showAttachmentSheet = true
                        },
                        onRemoveFile = { file ->
                            viewModel.removeAttachedFile(file)
                        },
                        onCancelReply = {
                            viewModel.clearReplyMessage()
                        }
                    )
                }
            }
        }
        if (showAttachmentSheet) {
            AttachOptionsBottomSheetDialog(
                onDismissRequest = { showAttachmentSheet = false },
                onMediaClick = {
                    pendingPickerType = AttachmentPickerType.MEDIA
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    showAttachmentSheet = false
                },
                onFileClick = {
                    pendingPickerType = AttachmentPickerType.FILE
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    showAttachmentSheet = false
                },
                onPollClick = { showAttachmentSheet = false },
                onContactClick = {
                    pendingPickerType = AttachmentPickerType.CONTACT
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    showAttachmentSheet = false
                }
            )
        }
        selectedMessage?.let { message ->
            MessageFunctionsBottomSheetDialog(
                message = message,
                onDismissRequest = { selectedMessage = null },
                onSelectMessage = {
                    viewModel.startSelection(message.id)
                    selectedMessage = null
                },
                onReply = { msg ->
                    viewModel.setReplyMessage(msg)
                }
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

private fun getContactInfo(context: Context, uri: Uri): ContactInfo? {
    val projection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.DISPLAY_NAME
    )
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
            var phone = ""
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(id),
                null
            )
            phoneCursor?.use { pc ->
                if (pc.moveToFirst()) {
                    phone = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
            }
            return ContactInfo(name = name, phone = phone)
        }
    }
    return null
}
