package uddug.com.naukoteka.ui.chat.compose


import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.domain.entities.chat.MediaMessage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatDetailUiState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import uddug.com.naukoteka.ui.chat.compose.components.ChatDetailMoreSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatDetailShimmer

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailDialogComponent(
    viewModel: ChatDialogDetailViewModel,
    onBackPressed: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onSearchClick: () -> Unit,
    onChatDeleted: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current


    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()


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
                    Text(text = stringResource(R.string.chat_group_info_title), fontSize = 20.sp, color = Color.Black)
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
                        // Аватарка
                        Avatar(
                            state.profile.image.takeIf { it?.isNotEmpty() == true },
                            state.profile.fullName,
                            size = 100.dp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Имя и титул
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
                    Divider()

                    // Контент табов
                    when (selectedTabIndex) {
                        0 -> MediaContent(state.media)
                        1 -> FilesContent(state.files)
                        2 -> VoiceContent(state.voices)
                        3 -> NotesContent(state.notes)
                    }
                    if (showMoreDialog) {
                        ChatDetailMoreSheetDialog(
                            dialogId = state.dialogId,
                            onNavigateToProfile = onNavigateToProfile,
                            onDismissRequest = { showMoreDialog = false },
                            onChatDeleted = onChatDeleted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaContent(items: List<MediaMessage>) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // 2 столбца
            modifier = Modifier.padding(8.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)

                ) {
                    AsyncImage(
                        model = BuildConfig.IMAGE_SERVER_URL + item.file.path,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun FilesContent(items: List<MediaMessage>) {
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
