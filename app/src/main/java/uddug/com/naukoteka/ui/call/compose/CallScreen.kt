package uddug.com.naukoteka.ui.call.compose

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@Composable
fun CallScreen(
    state: CallUiState,
    onBackPressed: () -> Unit,
    onEndCall: () -> Unit,
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

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    navigationIconContentColor = Color.White
                ),
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            tint = Color.White,
                            contentDescription = null,
                        )
                    }
                }
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
                onEndCall = onEndCall,
            )
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
    onEndCall: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CallActionButton(
                iconRes = R.drawable.ic_rec_mic_active,
                label = stringResource(R.string.call_microphone),
                containerColor = Color(0xFF121732),
                contentColor = Color.White,
            ) {}
            CallActionButton(
                iconRes = R.drawable.ic_profile_call,
                label = stringResource(R.string.call_primary_action),
                containerColor = Color(0xFF121732),
                contentColor = Color.White,
            ) {}
            CallActionButton(
                iconRes = R.drawable.ic_profile_send,
                label = stringResource(R.string.call_secondary_action),
                containerColor = Color(0xFF121732),
                contentColor = Color.White,
            ) {}
        }

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            color = Color(0xFF1D2239),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CallActionButton(
                    iconRes = R.drawable.ic_rec_mic_inactive,
                    label = stringResource(R.string.call_microphone),
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ) {}
                CallActionButton(
                    iconRes = R.drawable.ic_mute_sound_folder,
                    label = stringResource(R.string.call_speaker),
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ) {}
                CallActionButton(
                    iconRes = R.drawable.ic_close,
                    label = stringResource(R.string.call_terminate_action),
                    containerColor = Color(0xFFE64C4C),
                    contentColor = Color.White,
                    onClick = onEndCall,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
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
