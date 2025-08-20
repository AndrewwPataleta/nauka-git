package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailViewModel
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailUiState
import uddug.com.naukoteka.mvvm.chat.Participant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatGroupDetailComponent(
    viewModel: ChatGroupDetailViewModel,
    onBackPressed: () -> Unit,
    onSearchClick: () -> Unit,
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
                actions = {
                    androidx.compose.material.IconButton(onClick = { onSearchClick() }) {
                        androidx.compose.material.Icon(
                            painter = painterResource(id = R.drawable.ic_search_chat),
                            contentDescription = "Search Icon",
                            tint = Color(0xFF2E83D9)
                        )
                    }
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
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_naukoteka),
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
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
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
                                    ) { }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_profile_call),
                                    contentDescription = "Call",
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
                                    ) { }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_profile_send),
                                    contentDescription = "Share",
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
                                    ) { }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(24.dp),
                                    painter = painterResource(id = R.drawable.ic_profile_more),
                                    contentDescription = "More",
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
fun ParticipantsContent(users: List<Participant>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(users) { participant ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = BuildConfig.IMAGE_SERVER_URL.plus(participant.user.image.orEmpty()),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = participant.user.fullName.orEmpty(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    participant.status?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            androidx.compose.material3.Divider()
        }
    }
}
