package uddug.com.naukoteka.ui.call.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.CallParticipant
import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallUiState
import uddug.com.domain.entities.call.CallSessionState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    state: CallUiState,
    onBackPressed: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleCamera: () -> Unit,
    onMinimize: () -> Unit,
) {
    val backgroundColor = Color(0xFF0B1020)
    val isGroupCall = state.participants.size > 1
    val primaryParticipant = state.participants.firstOrNull()
    val callTitle = state.callTitle ?: primaryParticipant?.name
    val statusText = when (state.status) {
        CallStatus.DIALING -> stringResource(R.string.call_status_dialing)
        CallStatus.CONNECTING -> stringResource(R.string.call_status_connecting)
        CallStatus.IN_CALL -> stringResource(R.string.call_status_in_call)
        CallStatus.FINISHED -> stringResource(R.string.call_status_finished)
    }
    val resolvedCallTitle = callTitle ?: stringResource(R.string.call_status_in_call)
    var isParticipantsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var participantForActions by remember { mutableStateOf<CallParticipant?>(null) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CallTopBar(
                containerColor = backgroundColor,
                callDurationSeconds = state.callDurationSeconds,
                onOpenChat = {},
                onShowParticipants = { isParticipantsSheetVisible = true },
                onStartRecording = {},
                onMinimize = onMinimize,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = resolvedCallTitle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusText,
                    color = Color(0xFFB0B3C5),
                    fontSize = 16.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGroupCall) {
                CallParticipantsGrid(
                    participants = state.participants,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                )
            } else {
                SingleParticipantPreview(
                    participant = primaryParticipant,
                    status = state.status,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                )
            }

            CallControls(
                sessionState = state.sessionState,
                onToggleMicrophone = onToggleMicrophone,
                onToggleCamera = onToggleCamera,
                onEndCall = onEndCall,
            )
        }
    }

    if (isParticipantsSheetVisible) {
        ParticipantsScreen(
            participants = state.participants,
            onBackClick = { isParticipantsSheetVisible = false },
            onParticipantClick = { participant -> participantForActions = participant },
        )
    }

    participantForActions?.let { participant ->
        ParticipantActionsSheet(
            participant = participant,
            onDismiss = { participantForActions = null },
        )
    }
}

@Composable
private fun CallTopBar(
    containerColor: Color,
    callDurationSeconds: Int,
    onOpenChat: () -> Unit,
    onShowParticipants: () -> Unit,
    onStartRecording: () -> Unit,
    onMinimize: () -> Unit,
) {
    Surface(color = containerColor) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = Color(0xFF1D2239),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = formatCallDuration(callDurationSeconds),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onOpenChat) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat),
                        contentDescription = stringResource(R.string.call_chat),
                        tint = Color.White,
                    )
                }

                IconButton(onClick = onShowParticipants) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_call_participants),
                        contentDescription = stringResource(R.string.call_participants_title),
                        tint = Color.White,
                    )
                }

                IconButton(onClick = onStartRecording) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_call_record),
                        contentDescription = stringResource(R.string.call_record),
                        tint = Color(0xFFFF5656),
                    )
                }
            }

            IconButton(onClick = onMinimize) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = stringResource(R.string.call_minimize),
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
private fun SingleParticipantPreview(
    participant: CallParticipant?,
    status: CallStatus,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Avatar(
            url = participant?.avatarUrl,
            name = participant?.name,
            size = 120.dp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = participant?.name.orEmpty(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (status == CallStatus.DIALING || status == CallStatus.CONNECTING) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun CallParticipantsGrid(
    participants: List<CallParticipant>,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()

    Surface(
        modifier = modifier,
        color = Color.Transparent,
        contentColor = Color.White,
    ) {
        if (participants.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(participants, key = { it.id }) { participant ->
                    ParticipantCard(participant = participant)
                }
            }
        }
    }
}

@Composable
private fun ParticipantCard(
    participant: CallParticipant,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF121732),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true),
                contentAlignment = Alignment.Center,
            ) {
                Avatar(
                    url = participant.avatarUrl,
                    name = participant.name,
                    size = 72.dp,
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF1D2239))
                        .padding(6.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (participant.isMuted) {
                                R.drawable.ic_rec_mic_inactive
                            } else {
                                R.drawable.ic_rec_mic_active
                            }
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = participant.name.orEmpty(),
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CallControls(
    sessionState: CallSessionState,
    onToggleMicrophone: () -> Unit,
    onToggleCamera: () -> Unit,
    onEndCall: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF121732),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CallActionButton(
                iconRes = if (sessionState.micOn) {
                    R.drawable.ic_rec_mic_active
                } else {
                    R.drawable.ic_rec_mic_inactive
                },
                label = stringResource(R.string.call_microphone),
                containerColor = Color.Transparent,
                contentColor = if (sessionState.micOn) Color.White else Color(0xFF8083A0),
                onClick = onToggleMicrophone,
            )
            CallActionButton(
                iconRes = R.drawable.ic_camera,
                label = stringResource(R.string.call_camera),
                containerColor = Color.Transparent,
                contentColor = if (sessionState.camOn) Color.White else Color(0xFF8083A0),
                onClick = onToggleCamera,
            )
            CallActionButton(
                iconRes = R.drawable.ic_close,
                label = stringResource(R.string.call_terminate_action),
                containerColor = Color(0xFFE64C4C),
                contentColor = Color.White,
                onClick = onEndCall,
            )
        }
    }
}

@Composable
private fun CallActionButton(
    iconRes: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            color = containerColor,
            contentColor = contentColor,
            onClick = onClick,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = contentColor,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantsScreen(
    participants: List<CallParticipant>,
    onBackClick: () -> Unit,
    onParticipantClick: (CallParticipant) -> Unit,
) {
    val backgroundColor = Color(0xFF0B1020)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredParticipants = remember(participants, searchQuery) {
        val queryLower = searchQuery.trim().lowercase()
        if (queryLower.isEmpty()) {
            participants
        } else {
            participants.filter { participant ->
                participant.name?.lowercase()?.contains(queryLower) == true
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White,
                    ),
                    title = { Text(text = stringResource(R.string.call_participants_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.call_participants_count,
                        participants.size
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            tint = Color(0xFF8083A0),
                        )
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.call_participants_search_placeholder),
                            color = Color(0xFF8083A0),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF121732),
                        unfocusedContainerColor = Color(0xFF121732),
                        disabledContainerColor = Color(0xFF121732),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    color = Color(0xFF0B1020),
                    contentColor = Color.White,
                ) {
                    if (filteredParticipants.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.chat_search_no_results),
                                color = Color(0xFFB0B3C5),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                        ) {
                            items(filteredParticipants) { participant ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color(0xFF121732),
                                    shape = RoundedCornerShape(12.dp),
                                ) {
                                    ParticipantListItem(
                                        participant = participant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onParticipantClick(participant) }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantListItem(
    participant: CallParticipant,
    modifier: Modifier = Modifier,
) {
    val statusText = if (participant.isMuted) {
        stringResource(R.string.call_participant_status_muted)
    } else {
        stringResource(R.string.call_participant_status_active)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(
            url = participant.avatarUrl,
            name = participant.name,
            size = 48.dp,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = participant.name.orEmpty(),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(
                        id = if (participant.isMuted) {
                            R.drawable.ic_rec_mic_inactive
                        } else {
                            R.drawable.ic_rec_mic_active
                        }
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = statusText,
                    color = Color(0xFFB0B3C5),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantActionsSheet(
    participant: CallParticipant,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val muteActionLabel = if (participant.isMuted) {
        stringResource(R.string.call_participant_action_unmute)
    } else {
        stringResource(R.string.call_participant_action_mute)
    }
    val muteIcon = if (participant.isMuted) {
        R.drawable.ic_rec_mic_active
    } else {
        R.drawable.ic_rec_mic_inactive
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B1020),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ParticipantListItem(
                participant = participant,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                text = stringResource(R.string.call_participant_actions_title),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF121732),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SheetActionItem(
                        iconRes = muteIcon,
                        label = muteActionLabel,
                        onClick = onDismiss,
                    )
                    SheetActionItem(
                        iconRes = R.drawable.ic_copy,
                        label = stringResource(R.string.call_participant_action_copy),
                        onClick = onDismiss,
                    )
                    SheetActionItem(
                        iconRes = R.drawable.ic_profile_info,
                        label = stringResource(R.string.call_participant_action_profile),
                        onClick = onDismiss,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1D2239),
                shape = RoundedCornerShape(12.dp),
                onClick = onDismiss,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.call_sheet_close),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SheetActionItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun formatCallDuration(callDurationSeconds: Int): String {
    val safeSeconds = callDurationSeconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
