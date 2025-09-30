package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatDetailUiState
import uddug.com.naukoteka.mvvm.chat.ChatDialogDetailViewModel
import uddug.com.naukoteka.ui.chat.compose.components.SearchField
import uddug.com.naukoteka.ui.chat.compose.components.ChatMessagesList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailDialogSearchComponent(
    viewModel: ChatDialogDetailViewModel,
    onBackPressed: () -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.chat_detail_tab_messages),
        stringResource(R.string.chat_detail_tab_media),
        stringResource(R.string.chat_detail_tab_files),
        stringResource(R.string.chat_detail_tab_records)
    )

    val uiState by viewModel.uiState.collectAsState()
    val searchMessages by viewModel.searchMessages.collectAsState()
    val searchMedia by viewModel.searchMedia.collectAsState()
    val searchFiles by viewModel.searchFiles.collectAsState()
    val searchNotes by viewModel.searchNotes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchField(
                        title = stringResource(R.string.search_country),
                        query = searchQuery,
                        onSearchChanged = { searchQuery = it }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color(0xFF2E83D9)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LaunchedEffect(uiState, searchQuery) {
            val state = uiState
            if (state is ChatDetailUiState.Success) {
                viewModel.search(state.dialogId, searchQuery)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF2E83D9)
                    )
                },
                divider = {},
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
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

            when (selectedTabIndex) {
                0 -> if (searchMessages.isEmpty()) {
                    NoResults()
                } else {
                    ChatMessagesList(searchMessages)
                }

                1 -> if (searchMedia.isEmpty()) {
                    NoResults()
                } else {
                    MediaContent(searchMedia)
                }

                2 -> if (searchFiles.isEmpty()) {
                    NoResults()
                } else {
                    FilesContent(searchFiles)
                }

                3 -> if (searchNotes.isEmpty()) {
                    NoResults()
                } else {
                    NotesContent(searchNotes)
                }
            }
        }
    }
}

@Composable
private fun NoResults() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = stringResource(R.string.chat_search_no_results),
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}
