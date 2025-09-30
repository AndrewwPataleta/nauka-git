package uddug.com.naukoteka.ui.chat.compose


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.gun0912.tedimagepicker.builder.TedImagePicker
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
import uddug.com.naukoteka.ui.chat.AudioRecorder
import java.io.File

private enum class AttachmentPickerType { MEDIA, FILE }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatDialogComponent(
    viewModel: ChatDialogViewModel,
    onBackPressed: () -> Unit,
    onSearchClick: () -> Unit,
    onContactClick: () -> Unit,
    onForwardMessage: (MessageChat) -> Unit,
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
    val mediaPlayer = remember { MediaPlayer() }
    var isRecording by remember { mutableStateOf(false) }
    var recordedAudio by remember { mutableStateOf<File?>(null) }
    var recordingTime by remember { mutableStateOf(0L) }
    var isRecordingPlaying by remember { mutableStateOf(false) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            audioRecorder.start()
            recordedAudio = null
            recordingTime = 0L
            isRecording = true
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                delay(1000)
                recordingTime += 1000
            }
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

    val mediaPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    val filePermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            emptyArray()
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun attachMediaFiles(uris: List<Uri>) {
        val files = uris.mapNotNull { uri -> uriToFile(context, uri) }
        if (files.isNotEmpty()) {
            viewModel.attachFiles(files)
        }
    }

    fun openMediaPicker() {
        TedImagePicker.with(context)
            .showCameraTile(true)
            .startMultiImage { uriList ->
                attachMediaFiles(uriList)
            }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            when (pendingPickerType) {
                AttachmentPickerType.MEDIA -> openMediaPicker()
                AttachmentPickerType.FILE -> filePickerLauncher.launch(arrayOf("*/*"))
                null -> Unit
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
                        Surface(elevation = 4.dp) {
                            Column {
                                TopAppBar(
                                    modifier = Modifier.height(68.dp),
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
                                    elevation = 0.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFEAEAF2))
                                )
                            }
                        }
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
                        editingMessage = state.editingMessage,
                        isRecording = isRecording,
                        recordedAudio = recordedAudio,
                        recordingTime = String.format("%02d:%02d", recordingTime / 60000, (recordingTime / 1000) % 60),
                        selectedContact = state.selectedContact,
                        attachedContact = state.attachedContact,
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
                                recordedAudio = audioRecorder.stop()
                                isRecording = false
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onCancelRecording = {
                            audioRecorder.stop()?.delete()
                            recordedAudio = null
                            recordingTime = 0L
                            isRecording = false
                        },
                        onAttachClick = {
                            showAttachmentSheet = true
                        },
                        onRemoveFile = { file ->
                            viewModel.removeAttachedFile(file)
                        },
                        onCancelReply = {
                            viewModel.clearReplyMessage()
                        },
                        onCancelEditing = {
                            viewModel.clearEditingMessage()
                        },
                        onRemoveSelectedContact = {
                            viewModel.clearSelectedContact()
                        },
                        onRemoveAttachedContact = {
                            viewModel.clearAttachedContact()
                        },
                        onDeleteRecording = {
                            recordedAudio?.delete()
                            recordedAudio = null
                            recordingTime = 0L
                            if (isRecordingPlaying) {
                                mediaPlayer.stop()
                                isRecordingPlaying = false
                            }
                        },
                        onSendRecording = {
                            recordedAudio?.let { file ->
                                scope.launch {
                                    viewModel.sendVoiceMessage(file)
                                    scrollState.animateScrollToItem(messages.size - 1)
                                }
                            }
                            recordedAudio = null
                            recordingTime = 0L
                            if (isRecordingPlaying) {
                                mediaPlayer.stop()
                                isRecordingPlaying = false
                            }
                        },
                        onPlayRecording = {
                            recordedAudio?.let { file ->
                                if (isRecordingPlaying) {
                                    mediaPlayer.pause()
                                    isRecordingPlaying = false
                                } else {
                                    mediaPlayer.reset()
                                    mediaPlayer.setDataSource(file.absolutePath)
                                    mediaPlayer.prepare()
                                    mediaPlayer.start()
                                    isRecordingPlaying = true
                                    mediaPlayer.setOnCompletionListener {
                                        isRecordingPlaying = false
                                    }
                                }
                            }
                        },
                        isRecordingPlaying = isRecordingPlaying
                    )
                }
            }
        }
        if (showAttachmentSheet) {
            AttachOptionsBottomSheetDialog(
                onDismissRequest = { showAttachmentSheet = false },
                onMediaClick = {
                    showAttachmentSheet = false
                    val hasPermissions = mediaPermissions.all { permission ->
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    }
                    if (hasPermissions) {
                        openMediaPicker()
                    } else {
                        pendingPickerType = AttachmentPickerType.MEDIA
                        permissionLauncher.launch(mediaPermissions)
                    }
                },
                onFileClick = {
                    showAttachmentSheet = false
                    val hasPermissions = filePermissions.all { permission ->
                        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                    }
                    if (hasPermissions) {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    } else {
                        pendingPickerType = AttachmentPickerType.FILE
                        permissionLauncher.launch(filePermissions)
                    }
                },
                onPollClick = { showAttachmentSheet = false },
                onContactClick = {
                    showAttachmentSheet = false
                    onContactClick()
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
                },
                onEdit = { msg ->
                    viewModel.startEditingMessage(msg)
                    selectedMessage = null
                },
                onForward = { msg ->
                    onForwardMessage(msg)
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
