package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.domain.entities.chat.User
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailViewModel
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatGroupDetailComponent(
    viewModel: ChatGroupDetailViewModel,
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val tabs = listOf("Участники", "Медиа", "Файлы")
    Scaffold(
        topBar = {
            androidx.compose.material.TopAppBar(
                title = {
                    Text(text = "Информация", fontSize = 20.sp, color = Color.Black)
                },
                navigationIcon = {
                    androidx.compose.material.IconButton(onClick = { onBackPressed() }) {
                        androidx.compose.material.Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back Icon",
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
            is ChatGroupDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ChatGroupDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }
            is ChatGroupDetailUiState.Success -> {
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
                        AsyncImage(
                            model = BuildConfig.IMAGE_SERVER_URL.plus(state.image.orEmpty()),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.participants.size} участников",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
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
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTabIndex == index) Color.Black else Color(0xFF8083A0)
                                        )
                                    )
                                }
                            )
                        }
                    }
                    androidx.compose.material3.Divider()
                    when (selectedTabIndex) {
                        0 -> ParticipantsContent(state.participants)
                        1 -> MediaContent(state.media)
                        2 -> FilesContent(state.files)
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantsContent(users: List<User>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(users) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = BuildConfig.IMAGE_SERVER_URL.plus(user.image.orEmpty()),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = user.fullName.orEmpty(), fontWeight = FontWeight.Bold)
                    Text(text = user.nickname.orEmpty(), fontSize = 12.sp, color = Color.Gray)
                }
            }
            androidx.compose.material3.Divider()
        }
    }
}
