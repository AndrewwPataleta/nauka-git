package uddug.com.naukoteka.ui.chat.compose


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.bumptech.glide.Glide
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.mvvm.chat.CallMemberPreview
import uddug.com.naukoteka.mvvm.chat.ChatDialogUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogViewModel
import uddug.com.naukoteka.mvvm.chat.ContactInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.chat.compose.components.ChatInputBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatMessageDateBadge
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import uddug.com.naukoteka.ui.chat.compose.components.ChatMessageItem
import uddug.com.naukoteka.ui.chat.compose.components.TypingIndicator
import uddug.com.naukoteka.ui.chat.compose.components.ChatDetailMoreSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.MessageFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.AttachOptionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatTopBar
import uddug.com.naukoteka.ui.chat.compose.components.PinnedMessageBanner
import uddug.com.naukoteka.ui.chat.compose.components.VoicePlaybackBanner
import uddug.com.naukoteka.ui.chat.compose.components.MessageListShimmer
import uddug.com.naukoteka.ui.chat.compose.util.formatVoiceDuration
import uddug.com.naukoteka.ui.chat.compose.util.formatVoiceDurationFromMillis
import uddug.com.naukoteka.ui.chat.compose.util.parseVoiceDurationToMillis
import uddug.com.naukoteka.ui.chat.compose.util.uriToFile
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.File as ChatAttachmentFile
import uddug.com.naukoteka.ui.chat.AudioRecorder
import uddug.com.naukoteka.ui.chat.compose.formatMessageDate
import uddug.com.naukoteka.ui.chat.compose.messageDate
import uddug.com.naukoteka.ui.chat.compose.shouldShowDateBadge
import java.io.File
import java.time.ZoneId

private enum class AttachmentPickerType { MEDIA, FILE }

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatDialogComponent(
    viewModel: ChatDialogViewModel,
    onBackPressed: () -> Unit,
    onCallClick: (name: String, avatar: String, isVideoCall: Boolean) -> Unit = { _, _, _ -> },
    onContactClick: () -> Unit,
    onCreatePoll: () -> Unit,
    onOpenPollResults: (String, uddug.com.domain.entities.chat.Poll?) -> Unit,
    onForwardMessage: (MessageChat) -> Unit,
    onForwardSelected: (Set<Long>) -> Unit = {},
    onEditGroup: (Long) -> Unit,
    onChatDeleted: () -> Unit,
    onUserClick: (String) -> Unit = {},
    initialMessageId: Long? = null,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    var selectedMessage by remember { mutableStateOf<MessageChat?>(null) }
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedMessages by viewModel.selectedMessages.collectAsState()
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var pendingPickerType by remember { mutableStateOf<AttachmentPickerType?>(null) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var showCallOptions by remember { mutableStateOf(false) }
    val callSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentDialogId by viewModel.currentDialogId.collectAsState()
    val isCurrentUserAdmin by viewModel.isCurrentUserAdmin.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val notificationsDisabled by viewModel.notificationsDisabled.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val typingUsers by viewModel.typingUsers.collectAsState()
    // (fullName, userId) участников — для подсветки @упоминаний в сообщениях.
    val mentionUsers = remember(participants) {
        participants.mapNotNull { u ->
            val name = u.fullName
            val id = u.userId
            if (!name.isNullOrBlank() && !id.isNullOrBlank()) name to id else null
        }
    }
    val pollRevoteTriggers = remember { mutableStateMapOf<String, Int>() }
    val isGroupChat = when (val state = uiState) {
        is ChatDialogUiState.Success -> state.isGroup
        is ChatDialogUiState.Loading -> state.isGroup
        else -> false
    }

    // Paginate older messages when the user scrolls near the top of the list.
    val shouldLoadOlder by remember {
        androidx.compose.runtime.derivedStateOf {
            val firstVisible = scrollState.firstVisibleItemIndex
            val totalItems = scrollState.layoutInfo.totalItemsCount
            totalItems > 0 && firstVisible <= 3
        }
    }
    LaunchedEffect(shouldLoadOlder) {
        if (shouldLoadOlder) viewModel.loadOlderMessages()
    }

    val audioRecorder = remember { AudioRecorder(context) }
    val mediaPlayer = remember { MediaPlayer() }
    var isRecording by remember { mutableStateOf(false) }
    var recordedAudio by remember { mutableStateOf<File?>(null) }
    var recordingTime by remember { mutableStateOf(0L) }
    var isRecordingPlaying by remember { mutableStateOf(false) }
    var voicePlayingMessageId by remember { mutableStateOf<Long?>(null) }
    var voicePlayingMessage by remember { mutableStateOf<MessageChat?>(null) }
    var voicePlayingFile by remember { mutableStateOf<ChatAttachmentFile?>(null) }
    var voiceRemainingTime by remember { mutableStateOf(0L) }
    var isVoicePlaying by remember { mutableStateOf(false) }
    var isVoicePreparing by remember { mutableStateOf(false) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            audioRecorder.start()
            recordedAudio = null
            recordingTime = 0L
            isRecording = true
            // #189:2 — «записывает голосовое» для собеседников.
            viewModel.startUserActivity(2)
        }
    }

    fun openImageViewerByUrl(imageUrl: String) {
        StfalconImageViewer.Builder<String>(context, listOf(imageUrl)) { imageView, image ->
            Glide.with(context)
                .load(image)
                .into(imageView)
        }.show()
    }

    fun openImageViewer(file: ChatAttachmentFile) {
        val imageUrl = BuildConfig.IMAGE_SERVER_URL + file.path
        openImageViewerByUrl(imageUrl)
    }

    fun openVideoPlayer(videoUrl: String) {
        runCatching {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(android.net.Uri.parse(videoUrl), "video/*")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }.onFailure {
            android.widget.Toast.makeText(
                context,
                "Не удалось открыть видео",
                android.widget.Toast.LENGTH_SHORT,
            ).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer.reset()
            } catch (_: IllegalStateException) {
            }
            mediaPlayer.release()
        }
    }

    fun resetVoicePlayback() {
        voicePlayingMessageId = null
        voicePlayingMessage = null
        voicePlayingFile = null
        voiceRemainingTime = 0L
        isVoicePlaying = false
        isVoicePreparing = false
        mediaPlayer.setOnPreparedListener(null)
        mediaPlayer.setOnCompletionListener(null)
    }

    fun updateVoiceRemainingTimeFromPlayer() {
        val remaining = runCatching {
            val duration = mediaPlayer.duration
            val position = mediaPlayer.currentPosition
            (duration - position).coerceAtLeast(0)
        }.getOrDefault(0)
        voiceRemainingTime = remaining.toLong()
    }

    fun handleVoiceMessageClick(message: MessageChat, file: ChatAttachmentFile) {
        voicePlayingMessage = message
        voicePlayingFile = file
        if (voicePlayingMessageId == message.id) {
            if (isVoicePreparing) return
            if (isVoicePlaying) {
                try {
                    mediaPlayer.pause()
                    isVoicePlaying = false
                    updateVoiceRemainingTimeFromPlayer()
                } catch (_: IllegalStateException) {
                    resetVoicePlayback()
                }
            } else {
                try {
                    mediaPlayer.start()
                    isVoicePlaying = true
                    updateVoiceRemainingTimeFromPlayer()
                } catch (_: IllegalStateException) {
                    resetVoicePlayback()
                }
            }
            return
        }

        voiceRemainingTime = parseVoiceDurationToMillis(file.duration) ?: 0L
        voicePlayingMessageId = message.id
        isVoicePreparing = true
        isVoicePlaying = false

        if (isRecordingPlaying) {
            try {
                mediaPlayer.stop()
            } catch (_: IllegalStateException) {
            }
            isRecordingPlaying = false
        }

        try {
            mediaPlayer.reset()
            mediaPlayer.setOnPreparedListener { player ->
                isVoicePreparing = false
                isVoicePlaying = true
                voiceRemainingTime = player.duration.toLong()
                player.start()
            }
            mediaPlayer.setOnCompletionListener {
                try {
                    mediaPlayer.reset()
                } catch (_: IllegalStateException) {
                }
                resetVoicePlayback()
            }
            val source = BuildConfig.IMAGE_SERVER_URL + file.path
            mediaPlayer.setDataSource(source)
            mediaPlayer.prepareAsync()
        } catch (_: Exception) {
            mediaPlayer.reset()
            resetVoicePlayback()
            Toast.makeText(
                context,
                context.getString(R.string.chat_voice_message_playback_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun stopVoicePlayback() {
        try {
            mediaPlayer.stop()
        } catch (_: IllegalStateException) {
        }
        try {
            mediaPlayer.reset()
        } catch (_: IllegalStateException) {
        }
        resetVoicePlayback()
    }

    suspend fun scrollToLastMessageIfAny(messages: List<MessageChat>) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.lastIndex)
        }
    }

    LaunchedEffect(voicePlayingMessageId, isVoicePlaying, isVoicePreparing) {
        if (voicePlayingMessageId == null) {
            voiceRemainingTime = 0L
            return@LaunchedEffect
        }
        if (isVoicePreparing) {
            return@LaunchedEffect
        }
        updateVoiceRemainingTimeFromPlayer()
        while (isVoicePlaying) {
            delay(250)
            updateVoiceRemainingTimeFromPlayer()
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
        // Копирование (особенно видео) выносим в IO, чтобы не блокировать UI-поток.
        scope.launch(Dispatchers.IO) {
            val files = uris.mapNotNull { uri -> uriToFile(context, uri) }
            if (files.isNotEmpty()) {
                viewModel.attachFiles(files)
            }
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
        if (uris.isEmpty()) return
        scope.launch(Dispatchers.IO) {
            val files = uris.mapNotNull { uri -> uriToFile(context, uri) }
            if (files.isNotEmpty()) {
                viewModel.attachFiles(files)
            }
        }
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        attachMediaFiles(uris)
    }

    fun openMediaPicker() {
        mediaPickerLauncher.launch(
            PickVisualMediaRequest(PickVisualMedia.ImageAndVideo)
        )
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
            .background(MaterialTheme.colors.background)
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
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
                        activity = typingUsers,
                        firstParticipantName = state.firstParticipantName,
                        onDetailClick = {},
                        onBackPressed = { onBackPressed() },
                        onMoreClick = {}
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
                    var pendingMessageId by remember(initialMessageId) { mutableStateOf(initialMessageId) }
                    val messageIndexById = remember(messages) {
                        messages.mapIndexed { index, message -> message.id to index }.toMap()
                    }

                    LaunchedEffect(messages.size) {
                        if (initialMessageId == null && messages.isNotEmpty()) {
                            scrollState.scrollToItem(messages.size - 1)
                        }
                    }
                    val isImeVisible = WindowInsets.isImeVisible
                    LaunchedEffect(isImeVisible) {
                        if (isImeVisible && initialMessageId == null && messages.isNotEmpty()) {
                            scrollState.scrollToItem(messages.size - 1)
                        }
                    }
                    LaunchedEffect(messages, pendingMessageId) {
                        val targetId = pendingMessageId ?: return@LaunchedEffect
                        val index = messageIndexById[targetId]
                        if (index != null) {
                            scrollState.scrollToItem(index)
                            pendingMessageId = null
                        }
                    }
                    // Индикатор «печатает…» — футер списка. Чтобы он появлялся
                    // одновременно с шапкой (а не «под сгибом»), при появлении
                    // активности подскроллим к низу, если пользователь уже там.
                    LaunchedEffect(typingUsers.isNotEmpty()) {
                        if (typingUsers.isNotEmpty() && messages.isNotEmpty()) {
                            val li = scrollState.layoutInfo
                            val lastVisible = li.visibleItemsInfo.lastOrNull()?.index ?: 0
                            val nearBottom = lastVisible >= li.totalItemsCount - 2
                            if (nearBottom) {
                                scrollState.animateScrollToItem(li.totalItemsCount)
                            }
                        }
                    }
                    val playbackMessage = voicePlayingMessage
                    val playbackFile = voicePlayingFile

                    if (isSelectionMode) {
                        Surface(elevation = 4.dp) {
                            Column {
                                TopAppBar(
                                    modifier = Modifier.height(68.dp),
                                    title = {
                                        Text(
                                            text = selectedMessages.size.toString(),
                                            style = MaterialTheme.typography.h6.copy(color = MaterialTheme.colors.onBackground)
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { viewModel.clearSelection() }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_close),
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colors.onBackground
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(
                                            onClick = {
                                                val ids = selectedMessages.toSet()
                                                viewModel.clearSelection()
                                                onForwardSelected(ids)
                                            },
                                            enabled = selectedMessages.isNotEmpty()
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_forward),
                                                contentDescription = "Forward",
                                                tint = MaterialTheme.colors.onBackground
                                            )
                                        }
                                        IconButton(onClick = { viewModel.deleteSelectedMessages() }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_trash),
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colors.onBackground
                                            )
                                        }
                                    },
                                    backgroundColor = MaterialTheme.colors.background,
                                    elevation = 0.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
                                )
                            }
                        }
                    } else {
                        ChatTopBar(
                            name = state.chatName,
                            image = state.chatImage,
                            isGroup = state.isGroup,
                            status = state.status,
                            activity = typingUsers,
                            firstParticipantName = state.firstParticipantName,
                            onDetailClick = {
                                viewModel.onChatDetailClick()
                            },
                            onCallClick = { showCallOptions = true },
                            onBackPressed = { onBackPressed() },
                            onMoreClick = { showMoreDialog = true }
                        )
                        state.pinnedMessages.firstOrNull()?.let { pinned ->
                            PinnedMessageBanner(
                                message = pinned,
                                onClick = {
                                    val targetIndex = messages.indexOfFirst { it.id == pinned.id }
                                    if (targetIndex >= 0) {
                                        scope.launch {
                                            scrollState.animateScrollToItem(targetIndex)
                                        }
                                    }
                                },
                            )
                        }
                    }

                    state.activeCall?.let { activeCall ->
                        // Treat the call as video only when explicitly flagged
                        // (type/format 3). Otherwise it is audio — joining must
                        // not turn the camera on.
                        val isVideoCall = activeCall.type == 3 || activeCall.format == 3
                        OngoingCallBanner(
                            isVideoCall = isVideoCall,
                            participantsCount = state.activeCallParticipantsCount,
                            members = state.activeCallMembers,
                            avatarUrl = state.chatImage,
                            callTitle = state.chatName,
                            onJoinClick = {
                                onCallClick(
                                    state.chatName,
                                    state.chatImage,
                                    isVideoCall,
                                )
                            },
                        )
                    }

                    if (playbackMessage != null) {
                        val playbackName = playbackMessage.ownerName?.takeIf { it.isNotBlank() }
                            ?: stringResource(id = R.string.chat_unknown_user)
                        val remainingText = if (voiceRemainingTime > 0L) {
                            formatVoiceDurationFromMillis(voiceRemainingTime)
                        } else {
                            formatVoiceDuration(playbackFile?.duration)
                                ?: stringResource(id = R.string.chat_unknown_time)
                        }
                        VoicePlaybackBanner(
                            isPlaying = isVoicePlaying,
                            isLoading = isVoicePreparing,
                            senderName = playbackName,
                            remainingTimeText = remainingText,
                            onPlayPauseClick = {
                                if (playbackFile != null) {
                                    handleVoiceMessageClick(playbackMessage, playbackFile)
                                }
                            },
                            onCloseClick = { stopVoicePlayback() }
                        )
                    }

                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        itemsIndexed(messages) { index, message ->
                            val previousMessage = messages.getOrNull(index - 1)
                            if (shouldShowDateBadge(previousMessage, message, zoneId)) {
                                ChatMessageDateBadge(
                                    text = formatMessageDate(
                                        context = context,
                                        date = message.messageDate(zoneId),
                                        zoneId = zoneId
                                    )
                                )
                            }
                            ChatMessageItem(
                                message = message,
                                isMine = message.isMine,
                                selectionMode = isSelectionMode,
                                isSelected = selectedMessages.contains(message.id),
                                onSelectChange = { viewModel.toggleMessageSelection(message.id) },
                                onLongPress = { selectedMessage = it },
                                onReplyReferenceClick = { targetId ->
                                    messageIndexById[targetId]?.let { index ->
                                        scope.launch {
                                            scrollState.animateScrollToItem(index)
                                        }
                                    }
                                },
                                onPollVote = { pollId, optionIds ->
                                    viewModel.voteInPoll(pollId, optionIds)
                                },
                                onPollResults = { pollId, poll ->
                                    onOpenPollResults(pollId, poll)
                                },
                                pollRevoteTrigger = message.poll?.let { pollRevoteTriggers[it.id] ?: 0 } ?: 0,
                                onImageClick = { url -> openImageViewerByUrl(url) },
                                onVideoClick = { url -> openVideoPlayer(url) },
                                onVoiceMessageClick = { msg, file ->
                                    handleVoiceMessageClick(msg, file)
                                },
                                isPollAuthor = message.poll?.authorId != null &&
                                    message.poll?.authorId == currentUserId,
                                mentionUsers = mentionUsers,
                                onMentionClick = { userId -> onUserClick(userId) },
                                onUserClick = { userId -> onUserClick(userId) },
                            )
                        }
                        // Индикатор всегда в списке — плавно появляется/сворачивается
                        // (fade + shrink), чтобы окончание печати не «дёргало» ленту.
                        item(key = "typing_indicator") {
                            val typingVisible = typingUsers.isNotEmpty()
                            var lastTypingUsers by remember {
                                mutableStateOf(emptyList<uddug.com.domain.entities.chat.User>())
                            }
                            if (typingVisible) lastTypingUsers = typingUsers.map { it.user }
                            AnimatedVisibility(
                                visible = typingVisible,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically(),
                            ) {
                                TypingIndicator(users = lastTypingUsers)
                            }
                        }
                    }

                    
                    // @упоминания (только в групповом чате): кандидаты — все
                    // участники, кроме себя. Фильтрацию по @query, вставку и
                    // подсветку делает сам ChatInputBar (нужен контроль курсора).
                    val mentionCandidates = remember(participants, currentUserId) {
                        participants.filter {
                            !it.userId.isNullOrBlank() && it.userId != currentUserId
                        }
                    }

                    ChatInputBar(
                        currentMessage = state.currentMessage,
                        mentionCandidates = mentionCandidates,
                        mentionsEnabled = isGroupChat,
                        attachedFiles = state.attachedFiles,
                        replyMessage = state.replyMessage,
                        forwardMessage = state.pendingForward,
                        editingMessage = state.editingMessage,
                        isRecording = isRecording,
                        recordedAudio = recordedAudio,
                        recordingTime = String.format("%02d:%02d", recordingTime / 60000, (recordingTime / 1000) % 60),
                        selectedContact = state.selectedContact,
                        attachedContact = state.attachedContact,
                        onMessageChange = { newMessage ->
                            viewModel.updateCurrentMessage(newMessage)
                            if (newMessage.isNotBlank()) viewModel.onUserTyping()
                        },
                        onSendClick = {
                            scope.launch {
                                viewModel.sendMessage(state.currentMessage)
                                scrollToLastMessageIfAny(messages)
                                
                            }
                        },
                        onVoiceClick = {
                            if (isRecording) {
                                recordedAudio = audioRecorder.stop()
                                isRecording = false
                                viewModel.stopUserActivity()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        onCancelRecording = {
                            audioRecorder.stop()?.delete()
                            recordedAudio = null
                            recordingTime = 0L
                            isRecording = false
                            viewModel.stopUserActivity()
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
                        onCancelForward = {
                            viewModel.clearForwardMessage()
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
                                    scrollToLastMessageIfAny(messages)
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
                                    try {
                                        mediaPlayer.pause()
                                    } catch (_: IllegalStateException) {
                                    }
                                    isRecordingPlaying = false
                                } else {
                                    if (voicePlayingMessageId != null || isVoicePreparing) {
                                        try {
                                            mediaPlayer.stop()
                                        } catch (_: IllegalStateException) {
                                        }
                                        try {
                                            mediaPlayer.reset()
                                        } catch (_: IllegalStateException) {
                                        }
                                        resetVoicePlayback()
                                    }
                                    try {
                                        mediaPlayer.reset()
                                        mediaPlayer.setOnPreparedListener(null)
                                        mediaPlayer.setDataSource(file.absolutePath)
                                        mediaPlayer.prepare()
                                        mediaPlayer.start()
                                        isRecordingPlaying = true
                                        mediaPlayer.setOnCompletionListener {
                                            isRecordingPlaying = false
                                        }
                                    } catch (_: Exception) {
                                        try {
                                            mediaPlayer.reset()
                                        } catch (_: IllegalStateException) {
                                        }
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
                    // PickMultipleVisualMedia (системный Photo Picker) не требует
                    // разрешений READ_MEDIA_*. Запускаем напрямую, как и файловый пикер
                    // на API 33+. Прежний gate на partial-access (Android 13/14) возвращал
                    // granted=false и пикер не открывался -> вложения не прикреплялись.
                    openMediaPicker()
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
                onPollClick = {
                    showAttachmentSheet = false
                    onCreatePoll()
                },
                onContactClick = {
                    showAttachmentSheet = false
                    onContactClick()
                },
                isGroupChat = isGroupChat
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
                    val enriched = if (msg.ownerName.isNullOrBlank()) {
                        val chatName = (uiState as? ChatDialogUiState.Success)?.chatName
                        val fallbackName = if (msg.isMine) viewModel.currentUserName else chatName
                        msg.copy(ownerName = fallbackName)
                    } else msg
                    onForwardMessage(enriched)
                },
                isCurrentUserAdmin = isCurrentUserAdmin,
                isPollAuthor = message.poll?.authorId != null &&
                    message.poll?.authorId == currentUserId,
                onRevotePoll = { msg ->
                    msg.poll?.id?.let { pollId ->
                        val current = pollRevoteTriggers[pollId] ?: 0
                        pollRevoteTriggers[pollId] = current + 1
                    }
                },
                onStopPoll = { msg ->
                    msg.poll?.id?.let { pollId ->
                        viewModel.stopPoll(pollId)
                    }
                }
            )
        }
        if (showMoreDialog) {
            val dialogId = currentDialogId
            if (dialogId != null) {
                ChatDetailMoreSheetDialog(
                    dialogId = dialogId,
                    isGroup = (uiState as? ChatDialogUiState.Success)?.isGroup
                        ?: (uiState as? ChatDialogUiState.Loading)?.isGroup
                        ?: false,
                    onNavigateToProfile = {
                        viewModel.onChatDetailClick()
                    },
                    onDismissRequest = { showMoreDialog = false },
                    onChatDeleted = {
                        showMoreDialog = false
                        onChatDeleted()
                    },
                    onEditGroup = { onEditGroup(dialogId) },
                    isCurrentUserAdmin = isCurrentUserAdmin,
                    notificationsDisabled = notificationsDisabled,
                    onLeaveGroup = { onChatDeleted() },
                    onNotificationsChanged = { disabled ->
                        viewModel.updateNotificationsDisabled(disabled)
                    }
                )
            } else {
                showMoreDialog = false
            }
        }
        if (showCallOptions) {
            val callState = uiState as? ChatDialogUiState.Success
            ModalBottomSheet(
                onDismissRequest = { showCallOptions = false },
                sheetState = callSheetState,
                containerColor = colorResource(id = R.color.main_background),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = 8.dp),
                        text = stringResource(R.string.call_create_title),
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onBackground,
                        ),
                    )
                    CallOptionItem(
                        icon = Icons.Filled.Phone,
                        text = stringResource(R.string.call_audio),
                    ) {
                        showCallOptions = false
                        onCallClick(
                            callState?.chatName.orEmpty(),
                            callState?.chatImage.orEmpty(),
                            false,
                        )
                    }
                    CallOptionItem(
                        icon = Icons.Filled.Call,
                        text = stringResource(R.string.call_video),
                    ) {
                        showCallOptions = false
                        onCallClick(
                            callState?.chatName.orEmpty(),
                            callState?.chatImage.orEmpty(),
                            true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OngoingCallBanner(
    isVideoCall: Boolean,
    participantsCount: Int,
    members: List<CallMemberPreview>,
    avatarUrl: String?,
    callTitle: String?,
    onJoinClick: () -> Unit,
) {
    // Бэкенд не отдаёт время начала звонка, поэтому длительность считаем
    // локально с момента появления баннера. Когда звонок завершается, баннер
    // уходит из композиции и счётчик сбрасывается сам собой.
    var elapsedSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            elapsedSeconds++
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.main_background))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column {
            Text(
                text = stringResource(
                    if (isVideoCall) R.string.chat_ongoing_call_video
                    else R.string.chat_ongoing_call_audio
                ),
                color = colorResource(id = R.color.main_text),
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (participantsCount > 0) {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.chat_ongoing_call_participants,
                            count = participantsCount,
                            participantsCount,
                        ),
                        color = colorResource(id = R.color.secondary_text),
                        fontSize = 13.sp,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colorResource(id = R.color.main_background_input))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = formatOngoingCallDuration(elapsedSeconds),
                        color = colorResource(id = R.color.secondary_text),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Кластер аватарок тех, кто сейчас в звонке (внахлёст, инициалы если нет
        // фото), по центру между заголовком и кнопкой. Если участников ещё не
        // подтянули — аватар чата как запасной.
        if (members.isNotEmpty()) {
            CallParticipantsCluster(members = members, totalCount = participantsCount)
        } else {
            Avatar(url = avatarUrl, name = callTitle, size = 40.dp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(id = R.color.object_main))
                .clickable { onJoinClick() }
                .padding(horizontal = 16.dp, vertical = 9.dp),
        ) {
            Text(
                text = stringResource(R.string.chat_ongoing_call_join),
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
        }
    }
}

/**
 * Кластер аватарок участников звонка внахлёст (до 3, с тонким кольцом фона между
 * ними для эффекта «одна на одну»). Если участников больше — вместо числа
 * показываем кружок с тремя точками. Показывается в баннере «К звонку».
 */
@Composable
private fun CallParticipantsCluster(
    members: List<CallMemberPreview>,
    totalCount: Int,
) {
    val shown = members.take(3)
    // Overflow-индикатор («…»), если в звонке больше, чем показано аватарок.
    val hasMore = totalCount > shown.size
    Row(
        horizontalArrangement = Arrangement.spacedBy((-12).dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        shown.forEach { m ->
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.main_background))
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Avatar(url = m.imageUrl, name = m.name, size = 34.dp)
            }
        }
        if (hasMore) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.main_background))
                    .padding(2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colorResource(id = R.color.main_background_input)),
                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.main_text)),
                        )
                    }
                }
            }
        }
    }
}

/** Длительность звонка в формате MM:SS для баннера активного звонка. */
private fun formatOngoingCallDuration(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    return "%02d:%02d".format(safeSeconds / 60, safeSeconds % 60)
}

@Composable
private fun CallOptionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colors.primary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onBackground,
            ),
        )
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
