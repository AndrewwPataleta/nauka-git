package uddug.com.naukoteka.ui.chat.compose


import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share


import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import coil.compose.AsyncImage
import com.bumptech.glide.Glide
import com.stfalcon.imageviewer.StfalconImageViewer
import uddug.com.domain.entities.chat.MediaFile
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.AvatarUpdateEvent
import uddug.com.naukoteka.mvvm.chat.ChatDetailUiState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import uddug.com.naukoteka.ui.chat.compose.components.ChatAvatarActionDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatDetailMoreSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatDetailShimmer
import uddug.com.naukoteka.ui.chat.compose.components.FileFunctionAction
import uddug.com.naukoteka.ui.chat.compose.components.FileFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.util.uriToFile
import uddug.com.naukoteka.utils.copyToClipboard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailDialogComponent(
    viewModel: ChatDialogDetailViewModel,
    onBackPressed: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSearchClick: () -> Unit,
    onCallClick: (String?, String?, Boolean) -> Unit,
    onChatDeleted: () -> Unit,
    onViewAvatar: (String) -> Unit,
    onEditGroup: (Long) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val file = uriToFile(context, uri)
            if (file != null) {
                viewModel.onAvatarSelected(file)
            } else {
                Toast.makeText(
                    context,
                    R.string.chat_avatar_update_file_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.avatarEvents.collect { event ->
            when (event) {
                is AvatarUpdateEvent.Error -> {
                    val message = event.message?.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.chat_avatar_update_error)
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }

                AvatarUpdateEvent.Success -> Unit
            }
        }
    }


    val tabs = listOf(
        stringResource(R.string.chat_detail_tab_media),
        stringResource(R.string.chat_detail_tab_files),
        stringResource(R.string.chat_detail_tab_voice),
        stringResource(R.string.chat_detail_tab_records)
    )

    Scaffold(
        topBar = {
            androidx.compose.material.TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chat_group_info_title),
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                },
                actions = {

                    androidx.compose.material.IconButton(onClick = { onSearchClick() }) {
                        androidx.compose.material.Icon(
                            painter = painterResource(id = R.drawable.ic_search_chat),
                            contentDescription = "Edit Icon",
                            tint = Color(0xFF2E83D9)
                        )
                    }
                },
                navigationIcon = {
                    androidx.compose.material.IconButton(onClick = {
                        onBackPressed()
                    }) {
                        androidx.compose.material.Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Edit Icon",
                            tint = Color(0xFF2E83D9)
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ChatDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.TopStart
                ) {
                    ChatDetailShimmer()
                }
            }

            is ChatDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }

            is ChatDetailUiState.Success -> {
                var showMoreDialog by remember { mutableStateOf(false) }
                var showAvatarDialog by remember { mutableStateOf(false) }
                var showCallOptions by remember { mutableStateOf(false) }
                var pendingCallName by remember { mutableStateOf<String?>(null) }
                var pendingCallAvatar by remember { mutableStateOf<String?>(null) }
                val callSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                if (showCallOptions) {
                    ModalBottomSheet(
                        onDismissRequest = { showCallOptions = false },
                        sheetState = callSheetState,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.call_create_title),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                            )
                            CallOptionItem(
                                icon = Icons.Filled.Phone,
                                text = stringResource(R.string.call_audio),
                                onClick = {
                                    showCallOptions = false
                                    onCallClick(
                                        pendingCallName ?: state.profile.fullName,
                                        pendingCallAvatar ?: state.avatarPath,
                                        false,
                                    )
                                },
                            )
                            CallOptionItem(
                                icon = Icons.Filled.Call,
                                text = stringResource(R.string.call_video),
                                onClick = {
                                    showCallOptions = false
                                    onCallClick(
                                        pendingCallName ?: state.profile.fullName,
                                        pendingCallAvatar ?: state.avatarPath,
                                        true,
                                    )
                                },
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(paddingValues)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.Transparent)
                                .clickable(
                                    enabled = state.isCurrentUserAdmin && !state.isAvatarUpdating,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showAvatarDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Avatar(
                                url = state.avatarPath.takeIf { it?.isNotEmpty() == true },
                                name = state.profile.fullName,
                                size = 100.dp
                            )
                            if (state.isAvatarUpdating) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color.Black.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))


                        Text(
                            text = state.profile.fullName.orEmpty(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.profile.nickname.orEmpty(),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp)
                                    .background(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF5F5F9)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        pendingCallName = state.profile.fullName
                                        pendingCallAvatar = state.avatarPath
                                        showCallOptions = true
                                    }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_profile_call),
                                    contentDescription = "Media",
                                    tint = Color(0xFF2E83D9)
                                )
                                Text(
                                    text = stringResource(R.string.call_user),
                                    fontSize = 12.sp,
                                    color = Color(0xFF8083A0)
                                )
                            }
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp, end = 4.dp)
                                    .background(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF5F5F9)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        viewModel.shareDialog()
                                    }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_profile_send),
                                    contentDescription = "Media",
                                    tint = Color(0xFF2E83D9)
                                )
                                Text(
                                    text = stringResource(R.string.profile_shasre),
                                    fontSize = 12.sp,
                                    color = Color(0xFF8083A0)
                                )
                            }
                            Column(
                                Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp)
                                    .background(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF5F5F9)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showMoreDialog = true }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_profile_more),
                                    contentDescription = "Media",
                                    tint = Color(0xFF2E83D9)
                                )
                                Text(
                                    text = stringResource(R.string.profile_more),
                                    fontSize = 12.sp,
                                    color = Color(0xFF8083A0)
                                )
                            }
                        }
                    }

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
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { viewModel.selectTab(index) },
                                text = {
                                    Row(verticalAlignment = Alignment.Top) {
                                        androidx.compose.material3.Text(
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
                                    }
                                }
                            )
                        }
                    }
                    Divider()


                    when (selectedTabIndex) {
                        0 -> MediaContent(state.media)
                        1 -> FilesContent(state.files)
                        2 -> VoiceContent(state.voices)
                        3 -> NotesContent(state.notes)
                    }
                    if (showMoreDialog) {
                        ChatDetailMoreSheetDialog(
                            dialogId = state.dialogId,
                            isGroup = true,
                            onNavigateToProfile = onNavigateToProfile,
                            onDismissRequest = { showMoreDialog = false },
                            onChatDeleted = onChatDeleted,
                            onEditGroup = { onEditGroup(state.dialogId) },
                            isCurrentUserAdmin = state.isCurrentUserAdmin,
                            notificationsDisabled = false,
                            onLeaveGroup = {},
                            onNotificationsChanged = {}
                        )
                    }
                    if (showAvatarDialog) {
                        ChatAvatarActionDialog(
                            onDismissRequest = { showAvatarDialog = false },
                            onViewClick = {
                                state.avatarPath?.let { onViewAvatar(it) }
                            },
                            onReplaceClick = {
                                imagePickerLauncher.launch("image/*")
                            },
                            onDeleteClick = {
                                viewModel.onAvatarDeleted()
                            },
                            canView = !state.avatarPath.isNullOrBlank(),
                            canDelete = !state.avatarId.isNullOrBlank(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaContent(items: List<MediaMessage>) {
    val context = LocalContext.current
    val imageUrls = remember(items) {
        items.map { BuildConfig.IMAGE_SERVER_URL + it.file.path }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(8.dp)
        ) {
            itemsIndexed(items, key = { _, item -> item.file.id }) { index, item ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (imageUrls.isNotEmpty()) {
                                StfalconImageViewer.Builder<String>(
                                    context,
                                    imageUrls
                                ) { imageView, image ->
                                    Glide.with(context)
                                        .load(image)
                                        .into(imageView)
                                }
                                    .withStartPosition(index)
                                    .show()
                            }
                        }
                ) {
                    AsyncImage(
                        model = BuildConfig.IMAGE_SERVER_URL + item.file.path,
                        contentDescription = "Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun FilesContent(items: List<MediaMessage>) {
    val context = LocalContext.current
    var selectedItem by remember { mutableStateOf<MediaMessage?>(null) }
    val linkCopiedText = stringResource(R.string.chat_file_link_copied)
    val notAvailableText = stringResource(R.string.chat_file_action_not_available)
    val shareTitle = stringResource(R.string.chat_file_share_title)

    selectedItem?.let { mediaMessage ->
        FileFunctionsBottomSheetDialog(
            fileName = mediaMessage.file.fileName,
            onDismissRequest = { selectedItem = null },
            onActionSelected = { action ->
                when (action) {
                    FileFunctionAction.COPY_LINK -> {
                        val link = BuildConfig.IMAGE_SERVER_URL + mediaMessage.file.path
                        context.copyToClipboard(link)
                        Toast.makeText(context, linkCopiedText, Toast.LENGTH_SHORT).show()
                    }

                    FileFunctionAction.DOWNLOAD -> {
                        downloadMediaFile(context, mediaMessage.file)
                    }

                    FileFunctionAction.SHARE -> {
                        shareMediaFile(context, mediaMessage.file, shareTitle)
                    }

                    FileFunctionAction.EDIT_INFO,
                    FileFunctionAction.SELECT,
                    FileFunctionAction.DELETE -> {
                        Toast.makeText(context, notAvailableText, Toast.LENGTH_SHORT).show()
                    }
                }
                selectedItem = null
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(items, key = { it.file.id }) { item ->
            FileItem(
                item = item,
                onShowOptions = { selectedItem = it }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileItem(
    item: MediaMessage,
    onShowOptions: (MediaMessage) -> Unit,
) {
    val sizeText = remember(item.file.fileSize) { formatFileSize(item.file.fileSize) }
    val dateText = remember(item.createdAt) { formatFileDate(item.createdAt) }
    val metaText = remember(sizeText, dateText) {
        listOfNotNull(sizeText, dateText).joinToString(separator = " • ")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { onShowOptions(item) }
            ),
        elevation = 0.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(width = 1.dp, color = Color(0xFFE7E9EC))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_file_type_png),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.file.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (metaText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_file_download),
                            contentDescription = null,
                            tint = Color(0xFF7F838D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = metaText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF7F838D)
                        )
                    }
                }
            }
            IconButton(onClick = { onShowOptions(item) }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_vertical),
                    contentDescription = null,
                    tint = Color(0xFF7F838D)
                )
            }
        }
    }
}

private fun formatFileSize(sizeInBytes: Int?): String? {
    val size = sizeInBytes ?: return null
    if (size <= 0) return null
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = size.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    val pattern = if (value >= 10 || unitIndex == 0) "%.0f" else "%.1f"
    return String.format(
        Locale.getDefault(),
        pattern,
        value
    ) + units[unitIndex].lowercase(Locale.getDefault())
}

private fun formatFileDate(rawDate: String): String? {
    return try {
        val instant = Instant.parse(rawDate)
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        localDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))
    } catch (error: Exception) {
        null
    }
}

private fun downloadMediaFile(context: Context, file: MediaFile) {
    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
    val fileName = file.fileName.ifBlank { "chat_file_${file.id}" }
    val request = DownloadManager.Request(Uri.parse(BuildConfig.IMAGE_SERVER_URL + file.path))
        .setTitle(fileName)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
    file.contentType?.let { request.setMimeType(it) }
    downloadManager.enqueue(request)
    Toast.makeText(
        context,
        context.getString(R.string.chat_file_download_started),
        Toast.LENGTH_SHORT
    ).show()
}

private fun shareMediaFile(context: Context, file: MediaFile, chooserTitle: String) {
    val link = BuildConfig.IMAGE_SERVER_URL + file.path
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, link)
    }
    val chooserIntent = Intent.createChooser(intent, chooserTitle)
    try {
        context.startActivity(chooserIntent)
    } catch (error: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.chat_file_share_error),
            Toast.LENGTH_SHORT
        ).show()
    }
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
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF8083A0),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Light,
        )
    }
}

@Composable
fun VoiceContent(items: List<MediaMessage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(items) { item ->
            Text(
                text = item.file.fileName,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun NotesContent(items: List<MediaMessage>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(items) { item ->
            Text(
                text = item.file.fileName,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}
