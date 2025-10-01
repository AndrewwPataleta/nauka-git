package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailUiState
import uddug.com.naukoteka.mvvm.chat.ChatGroupDetailViewModel
import uddug.com.naukoteka.mvvm.chat.Participant
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import uddug.com.naukoteka.ui.chat.compose.components.SearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatGroupDetailComponent(
    viewModel: ChatGroupDetailViewModel,
    onBackPressed: () -> Unit,
    onSearchClick: () -> Unit,
    onAddParticipantsClick: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
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
                val tabTitles = listOf(
                    stringResource(
                        R.string.chat_group_tab_participants_count,
                        state.participants.size
                    ),
                    stringResource(R.string.chat_group_tab_media),
                    stringResource(R.string.chat_group_tab_files)
                )

                var selectedParticipant by remember { mutableStateOf<Participant?>(null) }
                val filteredParticipants = remember(searchQuery, state.participants) {
                    if (searchQuery.isBlank()) {
                        state.participants
                    } else {
                        val queryLower = searchQuery.trim().lowercase()
                        state.participants.filter { participant ->
                            val name = participant.user.fullName.orEmpty().lowercase()
                            val nickname = participant.user.nickname.orEmpty().lowercase()
                            name.contains(queryLower) || nickname.contains(queryLower)
                        }
                    }
                }

                if (selectedTabIndex == 0) {
                    ParticipantsTabContent(
                        paddingValues = paddingValues,
                        state = state,
                        tabTitles = tabTitles,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = viewModel::selectTab,
                        searchQuery = searchQuery,
                        onSearchQueryChange = viewModel::onSearchQueryChange,
                        onClearSearch = viewModel::clearSearch,
                        isCurrentUserAdmin = state.isCurrentUserAdmin,
                        onAddParticipantClick = { onAddParticipantsClick(state.dialogId) },
                        participants = filteredParticipants,
                        onParticipantMoreClick = { participant ->
                            selectedParticipant = participant
                        }
                    )
                } else {
                    OtherTabsContent(
                        paddingValues = paddingValues,
                        state = state,
                        tabTitles = tabTitles,
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = viewModel::selectTab,
                    ) {
                        when (selectedTabIndex) {
                            1 -> MediaContent(state.media)
                            2 -> FilesContent(state.files)
                            else -> Unit
                        }
                    }
                }

                val participantForActions = selectedParticipant
                if (participantForActions != null) {
                    ParticipantActionsBottomSheet(
                        participant = participantForActions,
                        onDismissRequest = { selectedParticipant = null },
                        onGrantAdminClick = {
                            participantForActions.user.userId?.let { viewModel.grantAdmin(it) }
                        },
                        onRevokeAdminClick = {
                            participantForActions.user.userId?.let { viewModel.revokeAdmin(it) }
                        },
                        onRemoveClick = {
                            participantForActions.user.userId?.let { viewModel.removeParticipant(it) }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ParticipantsTabContent(
    paddingValues: PaddingValues,
    state: ChatGroupDetailUiState.Success,
    tabTitles: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    isCurrentUserAdmin: Boolean,
    onAddParticipantClick: () -> Unit,
    participants: List<Participant>,
    onParticipantMoreClick: (Participant) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues),
        state = listState,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            GroupHeaderSection(state)
        }
        stickyHeader {
            GroupTabRow(
                tabTitles = tabTitles,
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
        }
        participantsList(
            isCurrentUserAdmin = isCurrentUserAdmin,
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onClearSearch = onClearSearch,
            onAddParticipantClick = onAddParticipantClick,
            participants = participants,
            onParticipantMoreClick = onParticipantMoreClick
        )
    }
}

@Composable
private fun OtherTabsContent(
    paddingValues: PaddingValues,
    state: ChatGroupDetailUiState.Success,
    tabTitles: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(paddingValues)
    ) {
        GroupHeaderSection(state)
        GroupTabRow(
            tabTitles = tabTitles,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected
        )
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun GroupHeaderSection(state: ChatGroupDetailUiState.Success) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(
            url = state.image,
            name = state.name,
            size = 100.dp,
            overrideInitials = if (state.image.isNullOrEmpty()) {
                stringResource(R.string.chat_group_initial)
            } else {
                null
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = state.participants.joinToString(", ") { it.user.fullName.orEmpty() },
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            GroupHeaderAction(
                icon = R.drawable.ic_profile_call,
                label = stringResource(R.string.call_user),
                modifier = Modifier.padding(end = 4.dp)
            )
            GroupHeaderAction(
                icon = R.drawable.ic_profile_send,
                label = stringResource(R.string.profile_shasre),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            GroupHeaderAction(
                icon = R.drawable.ic_profile_more,
                label = stringResource(R.string.profile_more),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun RowScope.GroupHeaderAction(
    icon: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .weight(1f)
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
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = Color(0xFF2E83D9)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF8083A0)
        )
    }
}

@Composable
private fun GroupTabRow(
    tabTitles: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    Column(modifier = Modifier.background(Color.White)) {
        TabRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            selectedTabIndex = selectedTabIndex,
            indicator = { tabPositions ->
                val safeIndex = selectedTabIndex.coerceIn(tabTitles.indices)
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[safeIndex]),
                    color = Color(0xFF2E83D9)
                )
            },
            divider = {},
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
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
        Divider(color = Color(0xFFEAEAF2))
    }
}

private fun LazyListScope.participantsList(
    isCurrentUserAdmin: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onAddParticipantClick: () -> Unit,
    participants: List<Participant>,
    onParticipantMoreClick: (Participant) -> Unit,
) {
    if (isCurrentUserAdmin) {
        item {
            SearchField(
                title = stringResource(R.string.chat_group_search_hint),
                query = searchQuery,
                onSearchChanged = onSearchQueryChange,
                showClearIcon = searchQuery.isNotEmpty(),
                onClearClick = onClearSearch
            )
        }
        item {
            Text(
                text = stringResource(R.string.chat_group_add_participant),
                color = Color(0xFF2E83D9),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onAddParticipantClick() }
                    .padding(vertical = 4.dp)
            )
        }
    }

    if (participants.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(text = stringResource(R.string.chat_search_no_results))
            }
        }
    } else {
        items(
            participants,
            key = { participant ->
                participant.user.userId
                    ?: participant.user.nickname
                    ?: participant.user.fullName
                    ?: participant.hashCode().toString()
            }
        ) { participant ->
            ParticipantRow(
                participant = participant,
                isCurrentUserAdmin = isCurrentUserAdmin,
                onMoreClick = onParticipantMoreClick
            )
            Divider(color = Color(0xFFEAEAF2))
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: Participant,
    isCurrentUserAdmin: Boolean,
    onMoreClick: (Participant) -> Unit,
) {
    val canManageParticipant = isCurrentUserAdmin &&
        !participant.isCurrentUser &&
        !participant.isOwner &&
        participant.user.userId != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            participant.user.image,
            participant.user.fullName,
            size = 44.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = participant.user.fullName.orEmpty(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            if (participant.isOwner) {
                Text(
                    text = stringResource(R.string.chat_group_owner_label),
                    color = Color(0xFF2E83D9),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                )
            } else if (participant.isAdmin) {
                Text(
                    text = stringResource(R.string.chat_group_admin_label),
                    color = Color(0xFF2E83D9),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                )
            }
            participant.status?.takeIf { it.isNotBlank() }?.let { status ->
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = Color(0xFF8083A0)
                )
            }
        }

        if (canManageParticipant) {
            IconButton(onClick = { onMoreClick(participant) }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = Color(0xFFB0B2C3)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantActionsBottomSheet(
    participant: Participant,
    onDismissRequest: () -> Unit,
    onGrantAdminClick: () -> Unit,
    onRevokeAdminClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_group_participant_actions_title),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!participant.isOwner) {
                if (participant.isAdmin) {
                    ParticipantActionRow(
                        text = stringResource(R.string.chat_group_remove_admin),
                        onClick = {
                            onRevokeAdminClick()
                            onDismissRequest()
                        }
                    )
                } else {
                    ParticipantActionRow(
                        text = stringResource(R.string.chat_group_make_admin),
                        onClick = {
                            onGrantAdminClick()
                            onDismissRequest()
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                ParticipantActionRow(
                    text = stringResource(R.string.chat_group_remove_participant),
                    textColor = Color(0xFFFF3B30),
                    onClick = {
                        onRemoveClick()
                        onDismissRequest()
                    }
                )
            }
        }
    }
}

@Composable
private fun ParticipantActionRow(
    text: String,
    textColor: Color = Color.Black,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
