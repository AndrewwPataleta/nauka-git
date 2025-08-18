package uddug.com.naukoteka.ui.chat.compose


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailDialogComponent(viewModel: ChatDialogDetailViewModel, onBackPressed: () -> Unit) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current


    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()


    val tabs = listOf("Медиа", "Файлы", "Голосовые", "Записи")

    Scaffold(
        topBar = {
            androidx.compose.material.TopAppBar(
                title = {
                    Text(text = "Информация", fontSize = 20.sp, color = Color.Black)
                },
                actions = {

                    androidx.compose.material.IconButton(onClick = { }) {
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
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
                        AsyncImage(
                            model = BuildConfig.IMAGE_SERVER_URL.plus(state.profile.image),
                            contentDescription = "Аватар",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
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
                        if (state.status.isNotBlank()) {
                            Text(
                                text = state.status,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }

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
                                    .clickable {

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
                                    .clickable {

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
                                    .clickable { }
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
                        0 -> MediaContent(state.currentMedia)
                        1 -> FilesContent()
                        2 -> VoiceContent()
                        3 -> NotesContent()
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
fun FilesContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

    }
}

@Composable
fun VoiceContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Голосовые сообщения")
    }
}

@Composable
fun NotesContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Записи")
    }
}
