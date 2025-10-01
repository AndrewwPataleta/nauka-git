package uddug.com.naukoteka.ui.chat.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListUiState
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.mvvm.chat.SearchResult
import uddug.com.naukoteka.mvvm.chat.SearchResults
import uddug.com.naukoteka.ui.chat.compose.components.ChatFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatListShimmer
import uddug.com.naukoteka.ui.chat.compose.components.ChatTabBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarComponent
import uddug.com.naukoteka.ui.chat.compose.components.SearchField
import uddug.com.naukoteka.ui.chat.compose.components.SearchResultItem

@Composable
fun ChatListComponent(
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
    onCreateChatClick: () -> Unit,
    onShowAttachments: (Long) -> Unit,
    onFolderSettings: () -> Unit,
    onChangeFolderOrder: () -> Unit,
) {
    var selectedDialogId by remember { mutableStateOf<Long?>(null) }
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedChats by viewModel.selectedChats.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        val focusManager = LocalFocusManager.current
        ChatToolbarComponent(
            viewModel = viewModel,
            onCreateChatClick = { onCreateChatClick() },
            onBackPressed = { onBackPressed() },
            isSelectionMode = isSelectionMode,
            selectedCount = selectedChats.size,
            onCloseSelection = { viewModel.clearSelection() },
            onDeleteSelected = { viewModel.deleteSelectedChats() },
            onMoreClick = { }
        )
        var query by remember { mutableStateOf("") }
        var isSearchFieldFocused by remember { mutableStateOf(false) }
        var selectedSearchTab by remember { mutableStateOf(SearchTab.Dialogs) }
        val searchResults by viewModel.searchResults.collectAsState()
        val isSearchLoading by viewModel.isSearchLoading.collectAsState()
        val isSearchActive by viewModel.isSearchActive.collectAsState()

        LaunchedEffect(isSearchActive) {
            if (!isSearchActive) {
                selectedSearchTab = SearchTab.Dialogs
            }
        }

        SearchField(
            title = stringResource(R.string.search_enter_query_full),
            query = query,
            onSearchChanged = {
                query = it
                viewModel.search(it)
                viewModel.onSearchFocusChanged(isSearchFieldFocused || it.isNotEmpty())
                if (it.isEmpty()) {
                    selectedSearchTab = SearchTab.Dialogs
                }
            },
            onFocusChanged = { focused ->
                isSearchFieldFocused = focused
                viewModel.onSearchFocusChanged(focused || query.isNotEmpty())
            },
            placeholderCentered = false,
            showClearIcon = isSearchActive,
            onClearClick = {
                query = ""
                selectedSearchTab = SearchTab.Dialogs
                focusManager.clearFocus()
                isSearchFieldFocused = false
                viewModel.search("")
                viewModel.onSearchFocusChanged(false)
            }
        )
        if (!isSearchActive) {
            Box(modifier = Modifier.weight(1f)) {
                ChatTabBar(
                    viewModel = viewModel,
                    onChatLongClick = { id -> selectedDialogId = id },
                    isSelectionMode = isSelectionMode,
                    selectedChats = selectedChats,
                    onChatSelect = { viewModel.toggleChatSelection(it) },
                    onOpenFolderSettings = onFolderSettings,
                    onChangeFolderOrder = onChangeFolderOrder
                )
            }
        } else {
            SearchResultsContent(
                modifier = Modifier.weight(1f),
                query = query,
                selectedTab = selectedSearchTab,
                onTabSelected = { selectedSearchTab = it },
                results = searchResults,
                isLoading = isSearchLoading,
                onResultClick = { viewModel.onChatClick(it) }
            )
        }
    }

    selectedDialogId?.let { id ->
        val chat = (uiState as? ChatListUiState.Success)?.chats?.firstOrNull { it.dialogId == id }
        val isBlocked = chat?.isBlocked ?: false
        val isPinned = chat?.isPinned ?: false
        val notificationsDisabled = chat?.notificationsDisable ?: false
        ChatFunctionsBottomSheetDialog(
            dialogId = id,
            isBlocked = isBlocked,
            isPinned = isPinned,
            notificationsDisabled = notificationsDisabled,
            onDismissRequest = { selectedDialogId = null },
            onShowAttachments = onShowAttachments,
            onSelectMessages = {
                viewModel.startSelection(id)
                selectedDialogId = null
            },
            onPinChange = { dialogId, pinned ->
                viewModel.updateDialogPin(dialogId, pinned)
            },
            onNotificationsChange = { dialogId, disabled ->
                viewModel.updateDialogNotifications(dialogId, disabled)
            },
            onChatDeleted = {
                viewModel.refreshChats()
            }
        )
    }
}

@Composable
private fun SearchResultsContent(
    modifier: Modifier = Modifier,
    query: String,
    selectedTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit,
    results: SearchResults,
    isLoading: Boolean,
    onResultClick: (Long) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = Color(0xFF2E83D9)
                )
            },
            divider = {},
        ) {
            SearchTab.values().forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = {
                        Text(
                            text = stringResource(id = tab.titleRes),
                            color = if (tab == selectedTab) Color.Black else Color(0xFF8083A0)
                        )
                    }
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            when {
                query.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.search_enter_query),
                        color = Color(0xFF8083A0),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp)
                    )
                }

                query.length < SEARCH_MIN_QUERY_LENGTH -> {
                    Text(
                        text = stringResource(R.string.search_enter_query_min_length),
                        color = Color(0xFF8083A0),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 32.dp)
                    )
                }

                isLoading -> ChatListShimmer()

                else -> {
                    val currentResults: List<SearchResult> = when (selectedTab) {
                        SearchTab.Dialogs -> results.dialogs
                        SearchTab.Messages -> results.messages
                    }
                    if (currentResults.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_empty_result),
                            color = Color(0xFF8083A0),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 32.dp)
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(currentResults) { result ->
                                SearchResultItem(
                                    result = result,
                                    query = query,
                                    onClick = onResultClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class SearchTab(@StringRes val titleRes: Int) {
    Dialogs(R.string.search_tab_chats),
    Messages(R.string.search_tab_messages)
}

private const val SEARCH_MIN_QUERY_LENGTH = 3
