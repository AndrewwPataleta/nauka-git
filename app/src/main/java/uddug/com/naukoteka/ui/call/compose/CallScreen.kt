package uddug.com.naukoteka.ui.call.compose

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.ui.chat.compose.components.getGradientForName
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.flashphoner.fpwcsapi.FPSurfaceViewRenderer
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.RendererCommon
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.AudioRoute
import uddug.com.naukoteka.mvvm.call.AudioRouteType
import uddug.com.naukoteka.mvvm.call.CallParticipant
import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallUiState
import uddug.com.domain.entities.call.CallSessionState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import com.flashphoner.fpwcsapi.Flashphoner
import com.flashphoner.fpwcsapi.session.Session
import com.flashphoner.fpwcsapi.session.SessionOptions
import com.flashphoner.fpwcsapi.room.RoomManager
import com.flashphoner.fpwcsapi.room.RoomManagerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    state: CallUiState,
    onBackPressed: () -> Unit,
    onEndCall: () -> Unit,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleRecording: () -> Unit,
    onStartRecording: (String) -> Unit,
    onMinimize: () -> Unit,
    onRemoteRendererReady: (SurfaceViewRenderer) -> Unit,
    clearRemoteRenderer: () -> Unit,
    onRemoteRendererReleased: (SurfaceViewRenderer) -> Unit,
    onBindLocalRenderer: (FPSurfaceViewRenderer) -> Unit,
    onBindRemoteRenderer: (FPSurfaceViewRenderer) -> Unit,
    onReleaseLocalRenderer: () -> Unit,
    onReleaseRemoteRenderer: () -> Unit,
    onBindParticipantRenderer: (String, FPSurfaceViewRenderer) -> Unit,
    onReleaseParticipantRenderer: (String) -> Unit,
    onMuteParticipant: (String) -> Unit,
    onToastConsumed: () -> Unit,
    onMicPermissionDenied: () -> Unit,
    onAudioFocusFailed: (String) -> Unit,
    onSwitchCamera: () -> Unit = {},
    onSelectCamera: (String) -> Unit = {},
    onToggleHand: () -> Unit = {},
    onSelectAudioRoute: (String) -> Unit = {},
    onSetParticipantPermit: (String, String, Boolean) -> Unit = { _, _, _ -> },
    onSetCallVolume: (Float) -> Unit = {},
    getCallVolume: () -> Float = { 0f },
    onShareLink: () -> Unit = {},
    onLeaveCall: () -> Unit = {},
    onEndForAll: () -> Unit = {},
    onAllowParticipant: (String) -> Unit = {},
    onAllowAll: () -> Unit = {},
    onKickParticipant: (String) -> Unit = {},
    onAssignAdmin: (String) -> Unit = {},
    onMuteAllMics: () -> Unit = {},
    onDisableAllCameras: () -> Unit = {},
    onForbidAllRaiseHand: () -> Unit = {},
    onMuteParticipantMic: (String) -> Unit = {},
    onDisableParticipantCamera: (String) -> Unit = {},
    onForbidParticipantRaiseHand: (String) -> Unit = {},
    onWritePrivate: (String) -> Unit = {},
    onOpenParticipantProfile: (String) -> Unit = {},
) {
    val backgroundColor = Color(0xFF0B1020)
    val context = LocalContext.current
    val isGroupCall = state.isGroupCall
    val primaryParticipant = state.participants.firstOrNull()
    val callTitle = state.callTitle ?: primaryParticipant?.name
    val statusText = when (state.status) {
        CallStatus.INCOMING -> stringResource(R.string.call_status_incoming)
        CallStatus.DIALING -> stringResource(R.string.call_status_dialing)
        CallStatus.CONNECTING -> stringResource(R.string.call_status_connecting)
        CallStatus.IN_CALL -> stringResource(R.string.call_status_in_call)
        CallStatus.FINISHED -> stringResource(R.string.call_status_finished)
    }
    val resolvedCallTitle = callTitle ?: stringResource(R.string.call_status_in_call)
    var isParticipantsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var participantForActions by remember { mutableStateOf<CallParticipant?>(null) }
    var isRecordingSetupVisible by rememberSaveable { mutableStateOf(false) }
    var isSettingsSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isAudioDeviceSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isCameraDeviceSheetVisible by rememberSaveable { mutableStateOf(false) }
    var isEndCallOptionsVisible by rememberSaveable { mutableStateOf(false) }
    var participantForPermits by remember { mutableStateOf<CallParticipant?>(null) }

    // Красная кнопка «завершить»: в 1-на-1 завершаем у обоих (REST stop); в группе
    // админ/организатор выбирает «Выйти» или «Завершить для всех», остальные —
    // просто выходят. См. iOS-parity C1 / docs/calls.md §7.
    val onEndCallPressed: () -> Unit = {
        when {
            !state.isGroupCall -> onEndForAll()
            state.isCurrentUserAdmin -> isEndCallOptionsVisible = true
            else -> onLeaveCall()
        }
    }
    // Когда пользователь открыл список участников из «Настройки для участников»,
    // клик по участнику ведёт в редактор прав, а не в обычные действия.
    var pickParticipantForPermits by rememberSaveable { mutableStateOf(false) }

    // Общее действие «Запись»: используется и в шапке, и в шторке настроек.
    val onRecordAction: () -> Unit = {
        when {
            state.isRecording -> onToggleRecording()
            state.status != CallStatus.IN_CALL -> Unit
            !state.canRecordCall -> onToggleRecording()
            else -> isRecordingSetupVisible = true
        }
    }

    state.toastMessage?.let { message ->
        LaunchedEffect(message) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onToastConsumed()
        }
    }

    val isCallActive = state.status == CallStatus.CONNECTING ||
        state.status == CallStatus.IN_CALL ||
        state.status == CallStatus.DIALING
    BackHandler(enabled = isCallActive) {
        onMinimize()
    }

    if (state.status == CallStatus.INCOMING) {
        IncomingCallContent(
            backgroundColor = backgroundColor,
            callTitle = resolvedCallTitle,
            participant = primaryParticipant,
            isVideoCall = state.isVideoCall,
            onAcceptCall = onAcceptCall,
            onDeclineCall = onDeclineCall,
        )
        return
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CallTopBar(
                containerColor = backgroundColor,
                callDurationSeconds = state.callDurationSeconds,
                onOpenChat = {},
                onShowParticipants = { isParticipantsSheetVisible = true },
                isRecording = state.isRecording,
                onRecordClick = onRecordAction,
                onOpenSettings = { isSettingsSheetVisible = true },
                onMinimize = onMinimize,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = if (isGroupCall) 6.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isGroupCall) {
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = resolvedCallTitle,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
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
                    state.errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = message,
                            color = Color(0xFFE64C4C),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Групповой звонок ВСЕГДА показывает видео-сетку: она сама рисует
            // аватар (NoVideoAvatar) для тех, у кого камера выключена, и видео —
            // у кого включена. Раньше при перезаходе в «аудио»-звонок (где
            // участники по факту с камерами) показывалась аудио-сетка и чужое
            // видео не рендерилось вовсе («никого не вижу, как аудиозвонок»).
            if (isGroupCall) {
                GroupVideoCallGrid(
                    participants = state.participants,
                    currentUserId = state.currentUserId,
                    selfAvatarUrl = state.currentUserAvatarUrl,
                    sessionState = state.sessionState,
                    status = state.status,
                    speakingIds = state.speakingParticipantIds,
                    selfSpeaking = state.isSelfSpeaking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    onBindLocalRenderer = onBindLocalRenderer,
                    onReleaseLocalRenderer = onReleaseLocalRenderer,
                    onBindParticipantRenderer = onBindParticipantRenderer,
                    onReleaseParticipantRenderer = onReleaseParticipantRenderer,
                )
            } else {
                if (state.isVideoCall) {
                    SingleParticipantVideo(
                        participant = primaryParticipant,
                        status = state.status,
                        isLocalVideoEnabled = state.sessionState.camOn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                        onBindLocalRenderer = onBindLocalRenderer,
                        onBindRemoteRenderer = onBindRemoteRenderer,
                        onReleaseLocalRenderer = onReleaseLocalRenderer,
                        onReleaseRemoteRenderer = onReleaseRemoteRenderer,
                    )
                } else {
                    SingleParticipantPreview(
                        participant = primaryParticipant,
                        status = state.status,
                        isVideoCall = state.isVideoCall,
                    onRemoteRendererReady = onRemoteRendererReady,
                    onRemoteRendererReleased = clearRemoteRenderer,modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true),
                    )
                }
            }

            // Баннер «X хочет войти» — приходит админу, когда участник в комнате
            // ожидания (status 6). Кнопки Разрешить/Нет прямо в звонке (дизайн).
            if (state.isCurrentUserAdmin) {
                state.lobbyParticipants.firstOrNull()?.let { pending ->
                    JoinRequestBanner(
                        participant = pending,
                        onAllow = { onAllowParticipant(pending.id) },
                        onDeny = { onKickParticipant(pending.id) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }

            CallControls(
                sessionState = state.sessionState,
                // Camera is always usable in a group call (even one that started
                // as audio, or came back as audio after a reconnect): the user
                // can enable video at any time. In 1-to-1 calls it stays tied to
                // the call type.
                canUseCamera = state.isVideoCall || state.isGroupCall,
                onToggleMicrophone = onToggleMicrophone,
                onToggleCamera = onToggleCamera,
                onEndCall = onEndCallPressed,
            )
        }
    }

    if (isParticipantsSheetVisible) {
        ParticipantsScreen(
            roster = state.rosterParticipants,
            lobby = state.lobbyParticipants,
            isAdmin = state.isCurrentUserAdmin,
            selfId = state.currentUserId,
            onBackClick = {
                isParticipantsSheetVisible = false
                pickParticipantForPermits = false
            },
            onAllow = { participant -> onAllowParticipant(participant.id) },
            onAllowAll = onAllowAll,
            onMuteAll = onMuteAllMics,
            onDisableAllCameras = onDisableAllCameras,
            onForbidAllRaiseHand = onForbidAllRaiseHand,
            onParticipantClick = { participant ->
                if (pickParticipantForPermits) {
                    pickParticipantForPermits = false
                    isParticipantsSheetVisible = false
                    participantForPermits = participant
                } else {
                    participantForActions = participant
                }
            },
        )
    }

    participantForActions?.let { participant ->
        ParticipantActionsSheet(
            participant = participant,
            isAdmin = state.isCurrentUserAdmin,
            canAssignAdmin = state.isCurrentUserOrganizer,
            onMuteMic = {
                onMuteParticipantMic(participant.id)
                participantForActions = null
            },
            onDisableCamera = {
                onDisableParticipantCamera(participant.id)
                participantForActions = null
            },
            onForbidRaiseHand = {
                onForbidParticipantRaiseHand(participant.id)
                participantForActions = null
            },
            onWritePrivate = {
                onWritePrivate(participant.id)
                participantForActions = null
            },
            onAssignAdmin = {
                onAssignAdmin(participant.id)
                participantForActions = null
            },
            onKick = {
                onKickParticipant(participant.id)
                participantForActions = null
            },
            onOpenProfile = {
                onOpenParticipantProfile(participant.id)
                participantForActions = null
            },
            onDismiss = { participantForActions = null },
        )
    }

    if (isRecordingSetupVisible) {
        val defaultFileName = remember(resolvedCallTitle) {
            defaultRecordingFileName(resolvedCallTitle)
        }
        CallRecordingSetupScreen(
            defaultFileName = defaultFileName,
            onBack = { isRecordingSetupVisible = false },
            onStartRecording = { fileName ->
                isRecordingSetupVisible = false
                onStartRecording(fileName)
            },
        )
    }

    if (isSettingsSheetVisible) {
        CallSettingsSheet(
            state = state,
            onDismiss = { isSettingsSheetVisible = false },
            onOpenAudioDevices = {
                isSettingsSheetVisible = false
                isAudioDeviceSheetVisible = true
            },
            onOpenCameraDevices = {
                isSettingsSheetVisible = false
                isCameraDeviceSheetVisible = true
            },
            onToggleHand = onToggleHand,
            onRecordClick = onRecordAction,
            onOpenParticipants = {
                isSettingsSheetVisible = false
                isParticipantsSheetVisible = true
            },
            onOpenParticipantPermits = {
                isSettingsSheetVisible = false
                // Открываем список участников, чтобы выбрать, кому менять права.
                pickParticipantForPermits = true
                isParticipantsSheetVisible = true
            },
            onShareLink = {
                isSettingsSheetVisible = false
                onShareLink()
            },
        )
    }

    if (isAudioDeviceSheetVisible) {
        AudioDeviceSheet(
            routes = state.audioRoutes,
            currentRouteId = state.currentAudioRouteId,
            onSelect = { routeId ->
                onSelectAudioRoute(routeId)
            },
            onDismiss = { isAudioDeviceSheetVisible = false },
        )
    }

    if (isCameraDeviceSheetVisible) {
        CameraDeviceSheet(
            cameras = state.availableCameras,
            currentCameraId = state.currentCameraId,
            onSelect = { cameraId -> onSelectCamera(cameraId) },
            onDismiss = { isCameraDeviceSheetVisible = false },
        )
    }

    participantForPermits?.let { participant ->
        ParticipantPermitsSheet(
            participant = participant,
            initialVolume = getCallVolume(),
            onSetPermit = { permit, grant ->
                onSetParticipantPermit(participant.id, permit, grant)
            },
            onVolumeChange = onSetCallVolume,
            onDismiss = { participantForPermits = null },
        )
    }

    if (isEndCallOptionsVisible) {
        EndCallOptionsSheet(
            onLeave = {
                isEndCallOptionsVisible = false
                onLeaveCall()
            },
            onEndForAll = {
                isEndCallOptionsVisible = false
                onEndForAll()
            },
            onDismiss = { isEndCallOptionsVisible = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndCallOptionsSheet(
    onLeave: () -> Unit,
    onEndForAll: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B1020),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.call_end_options_title),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
            )
            EndCallOptionButton(
                label = stringResource(R.string.call_end_leave),
                textColor = Color.White,
                background = Color(0xFF1D2239),
                onClick = onLeave,
            )
            EndCallOptionButton(
                label = stringResource(R.string.call_end_for_all),
                textColor = Color.White,
                background = Color(0xFFE64C4C),
                onClick = onEndForAll,
            )
            EndCallOptionButton(
                label = stringResource(R.string.call_sheet_close),
                textColor = Color(0xFFB0B3C5),
                background = Color(0xFF121732),
                onClick = onDismiss,
            )
        }
    }
}

@Composable
private fun EndCallOptionButton(
    label: String,
    textColor: Color,
    background: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = background,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun IncomingCallContent(
    backgroundColor: Color,
    callTitle: String,
    participant: CallParticipant?,
    isVideoCall: Boolean,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(
                url = participant?.avatarUrl,
                name = participant?.name ?: callTitle,
                size = 120.dp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = callTitle,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    if (isVideoCall) {
                        R.string.call_incoming_video
                    } else {
                        R.string.call_incoming_audio
                    }
                ),
                color = Color(0xFFB0B3C5),
                fontSize = 16.sp,
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
//                IncomingCallSecondaryAction(
//                    icon = Icons.Filled.,
//                    label = stringResource(R.string.call_incoming_remind),
//                )
//                IncomingCallSecondaryAction(
//                    icon = Icons.Filled.Message,
//                    label = stringResource(R.string.call_incoming_message),
//                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IncomingCallControlButton(
                    iconRes = R.drawable.ic_close,
                    label = stringResource(R.string.call_incoming_decline),
                    containerColor = Color(0xFFE64C4C),
                    onClick = onDeclineCall,
                )
                Spacer(modifier = Modifier.weight(1f))

                IncomingCallControlButton(
                    iconRes = R.drawable.ic_phone,
                    label = stringResource(R.string.call_incoming_accept),
                    containerColor = Color(0xFF2ED06D),
                    onClick = onAcceptCall,
                )
            }
        }
    }
}

@Composable
private fun IncomingCallSecondaryAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() },
    ) {
        Surface(
            color = Color(0xFF1D2239),
            contentColor = Color.White,
            shape = CircleShape,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = label)
            }
        }

        Text(
            text = label,
            color = Color(0xFFB0B3C5),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun IncomingCallControlButton(
    iconRes: Int,
    label: String,
    containerColor: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier
                .size(74.dp)
                .clip(CircleShape),
            color = containerColor,
            contentColor = Color.White,
            onClick = onClick,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = Color.White,
                )
            }
        }

        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun CallTopBar(
    containerColor: Color,
    callDurationSeconds: Int,
    onOpenChat: () -> Unit,
    onShowParticipants: () -> Unit,
    isRecording: Boolean,
    onRecordClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onMinimize: () -> Unit,
) {
    val recordingScale = if (isRecording) {
        val infiniteTransition = rememberInfiniteTransition(label = "record_scale_transition")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "record_scale",
        ).value
    } else {
        1f
    }

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
                        painter = painterResource(id = R.drawable.ic_call_dialog),
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

                IconButton(
                    onClick = onRecordClick,
                    modifier = Modifier.scale(recordingScale),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_call_record),
                        contentDescription = stringResource(R.string.call_record),
                        tint = Color(0xFFFF5656),
                    )
                }

                IconButton(onClick = onOpenSettings) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Настройки",
                        tint = Color.White,
                    )
                }
            }

            IconButton(onClick = onMinimize) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_show_preview),
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
    isVideoCall: Boolean,
    onRemoteRendererReady: (SurfaceViewRenderer) -> Unit,
    onRemoteRendererReleased: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isVideoCall) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF121732)),
            contentAlignment = Alignment.Center,
        ) {
            CallVideoSurface(
                modifier = Modifier.fillMaxSize(),
                onRemoteRendererReady = onRemoteRendererReady,
                onRemoteRendererReleased = onRemoteRendererReleased,
            )
            if (status == CallStatus.DIALING || status == CallStatus.CONNECTING) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    } else {
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
                fontWeight = FontWeight.SemiBold,
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
}

@Composable
private fun CallVideoSurface(
    modifier: Modifier = Modifier,
    onRemoteRendererReady: (SurfaceViewRenderer) -> Unit,
    onRemoteRendererReleased: () -> Unit,
) {
    val eglBase = remember { EglBase.create() }
    var renderer by remember { mutableStateOf<FPSurfaceViewRenderer?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            FPSurfaceViewRenderer(context).apply {
                init(eglBase.eglBaseContext, null)
                setEnableHardwareScaler(true)
                renderer = this
                onRemoteRendererReady(this)
            }
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            onRemoteRendererReleased()
            renderer?.release()
            eglBase.release()
        }
    }
}

@Composable
private fun SingleParticipantVideo(
    participant: CallParticipant?,
    status: CallStatus,
    isLocalVideoEnabled: Boolean,
    modifier: Modifier = Modifier,
    onBindLocalRenderer: (FPSurfaceViewRenderer) -> Unit,
    onBindRemoteRenderer: (FPSurfaceViewRenderer) -> Unit,
    onReleaseLocalRenderer: () -> Unit,
    onReleaseRemoteRenderer: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF121732)),
        contentAlignment = Alignment.Center,
    ) {
        FlashphonerVideoView(
            modifier = Modifier.fillMaxSize(),
            isMirror = false,
            isOverlay = false,
            onRendererReady = onBindRemoteRenderer,
            onRendererReleased = onReleaseRemoteRenderer,
        )
        if (isLocalVideoEnabled) {
            FlashphonerVideoView(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(width = 120.dp, height = 160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                isMirror = true,
                isOverlay = true,
                onRendererReady = onBindLocalRenderer,
                onRendererReleased = onReleaseLocalRenderer,
            )
        }
        if (status == CallStatus.DIALING || status == CallStatus.CONNECTING) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Avatar(
                    url = participant?.avatarUrl,
                    name = participant?.name,
                    size = 100.dp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun FlashphonerVideoView(
    modifier: Modifier,
    isMirror: Boolean,
    isOverlay: Boolean,
    onRendererReady: (FPSurfaceViewRenderer) -> Unit,
    onRendererReleased: () -> Unit,
) {
    val context = LocalContext.current
    val readyCallback by rememberUpdatedState(onRendererReady)
    val releaseCallback by rememberUpdatedState(onRendererReleased)
    val renderer = remember(context, isMirror, isOverlay) {
        FPSurfaceViewRenderer(context).apply {
            init(Flashphoner.context, null)
            setEnableHardwareScaler(true)
            setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
            setMirror(isMirror)
            if (isOverlay) {
                setZOrderMediaOverlay(true)
            }
        }
    }

    LaunchedEffect(renderer) {
        readyCallback(renderer)
    }

    AndroidView(
        factory = { renderer },
        modifier = modifier,
        update = {},
    )

    DisposableEffect(renderer) {
        onDispose {
            releaseCallback()
            renderer.release()
        }
    }
}

@Composable
private fun GroupVideoCallGrid(
    participants: List<CallParticipant>,
    currentUserId: String?,
    selfAvatarUrl: String?,
    sessionState: CallSessionState,
    status: CallStatus,
    speakingIds: Set<String>,
    selfSpeaking: Boolean,
    modifier: Modifier = Modifier,
    onBindLocalRenderer: (FPSurfaceViewRenderer) -> Unit,
    onReleaseLocalRenderer: () -> Unit,
    onBindParticipantRenderer: (String, FPSurfaceViewRenderer) -> Unit,
    onReleaseParticipantRenderer: (String) -> Unit,
) {
    val remoteParticipants = remember(participants, currentUserId) {
        if (currentUserId != null) participants.filter { it.id != currentUserId }
        else participants
    }
    val tileCount = remoteParticipants.size
    val columns = if (tileCount <= 1) 1 else 2
    val rows = maxOf((tileCount + columns - 1) / columns, 1)

    // Movable-тайлы: сохраняем состояние тайла участника (и его WebRTC-рендерер)
    // при переезде между строками/колонками сетки, когда участники входят/выходят.
    // Обычный key() этого не даёт — при смене родительской Row тайл пересоздаётся,
    // рвётся подписка на стрим и у соседа "отваливается" камера. movableContentOf
    // переносит тайл без dispose/recreate.
    val participantTiles = remember { mutableMapOf<String, @Composable (CallParticipant, Boolean, Modifier) -> Unit>() }
    participantTiles.keys.retainAll(remoteParticipants.mapTo(HashSet()) { it.id })
    remoteParticipants.forEach { participant ->
        participantTiles.getOrPut(participant.id) {
            movableContentOf { p, speaking, tileModifier ->
                VideoParticipantTile(
                    label = p.name ?: p.id,
                    avatarUrl = p.avatarUrl,
                    isMuted = p.isMuted,
                    camOn = p.camOn,
                    handUp = p.handUp,
                    speaking = speaking,
                    modifier = tileModifier,
                    onRendererReady = { renderer -> onBindParticipantRenderer(p.id, renderer) },
                    onRendererReleased = { onReleaseParticipantRenderer(p.id) },
                )
            }
        }
    }

    Box(modifier = modifier) {
        if (tileCount == 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            // При 5+ участниках плитки на weight(1f) сжимались в нечитаемую кашу.
            // Свыше 4 — делаем колонку вертикально-скроллящейся с фиксированной
            // высотой строки, чтобы плитки оставались нормального размера.
            // Column (не Lazy) сохраняет WebRTC-рендереры (movableContentOf).
            val scrollable = tileCount > 4
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (scrollable) Modifier.height(200.dp) else Modifier.weight(1f)),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (colIndex in 0 until columns) {
                            val tileIndex = rowIndex * columns + colIndex
                            if (tileIndex < tileCount) {
                                val participant = remoteParticipants[tileIndex]
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                ) {
                                    participantTiles.getValue(participant.id)
                                        .invoke(
                                            participant,
                                            participant.id in speakingIds,
                                            Modifier.fillMaxSize(),
                                        )
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Local PiP overlay — bottom-end
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .size(width = 100.dp, height = 140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF121732))
                .then(
                    if (selfSpeaking && sessionState.camOn) {
                        Modifier.border(3.dp, Color(0xFF4DA6FF), RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (sessionState.camOn) {
                FlashphonerVideoView(
                    modifier = Modifier.fillMaxSize(),
                    isMirror = true,
                    isOverlay = true,
                    onRendererReady = onBindLocalRenderer,
                    onRendererReleased = onReleaseLocalRenderer,
                )
            } else {
                NoVideoAvatar(
                    avatarUrl = selfAvatarUrl,
                    name = "Вы",
                    modifier = Modifier.fillMaxSize(),
                    speaking = selfSpeaking,
                )
            }
            CallTileBadge(
                label = "Вы",
                isMuted = !sessionState.micOn,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp),
            )
        }

        if (status == CallStatus.DIALING || status == CallStatus.CONNECTING) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@Composable
private fun VideoParticipantTile(
    label: String,
    avatarUrl: String?,
    isMuted: Boolean,
    camOn: Boolean,
    handUp: Boolean,
    speaking: Boolean,
    modifier: Modifier = Modifier,
    onRendererReady: (FPSurfaceViewRenderer) -> Unit,
    onRendererReleased: () -> Unit,
) {
    // При включённой камере обводим говорящего синей рамкой (у выключенной —
    // пульсирует кольцо вокруг аватара в NoVideoAvatar).
    val speakingBorder = if (speaking && camOn) {
        Modifier.border(3.dp, Color(0xFF4DA6FF), RoundedCornerShape(16.dp))
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121732))
            .then(speakingBorder),
    ) {
        // Видео-слой (рендерер биндится всегда, чтобы вернуть видео при включении
        // камеры). SurfaceView непрозрачен, поэтому когда камера выключена —
        // накрываем его аватар-визуалом ниже.
        FlashphonerVideoView(
            modifier = Modifier.fillMaxSize(),
            isMirror = false,
            isOverlay = false,
            onRendererReady = onRendererReady,
            onRendererReleased = onRendererReleased,
        )

        // Камера выключена — вместо видео/спиннера показываем аватар участника
        // с размытым/затемнённым фоном (нет фото → инициалы + градиент).
        if (!camOn) {
            NoVideoAvatar(
                avatarUrl = avatarUrl,
                name = label,
                modifier = Modifier.fillMaxSize(),
                speaking = speaking,
            )
        }

        // Name + mic badge (top-start)
        CallTileBadge(
            label = label,
            isMuted = isMuted,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        )

        // Поднятая рука — пульсирующий бейдж (bottom-start), как в дизайне.
        if (handUp) {
            RaisedHandBadge(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
            )
        }
    }
}

/** Бейдж «поднята рука» с волновой пульсацией. */
@Composable
private fun RaisedHandBadge(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "hand")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hand_scale",
    )
    Box(
        modifier = modifier
            .scale(scale)
            .size(32.dp)
            .clip(CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_call_hand),
            contentDescription = null,
            tint = Color(0xFF2E83D9),
            modifier = Modifier.size(18.dp),
        )
    }
}

/**
 * Пульсирующее синее кольцо-индикатор «говорит» (VU-метр). Оборачивает аватар
 * заданного размера: кольцо чуть больше аватара, его прозрачность/масштаб
 * анимируются в такт речи. Когда [speaking] == false — рисуем только аватар.
 */
@Composable
private fun SpeakingAvatar(
    avatarUrl: String?,
    name: String?,
    size: Dp,
    speaking: Boolean,
) {
    Box(contentAlignment = Alignment.Center) {
        if (speaking) {
            val transition = rememberInfiniteTransition(label = "vu")
            val ringScale by transition.animateFloat(
                initialValue = 1f,
                targetValue = 1.14f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "vu_scale",
            )
            val ringAlpha by transition.animateFloat(
                initialValue = 0.35f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "vu_alpha",
            )
            Box(
                modifier = Modifier
                    .size(size + 10.dp)
                    .scale(ringScale)
                    .border(3.dp, Color(0xFF4DA6FF).copy(alpha = ringAlpha), CircleShape),
            )
        }
        Avatar(url = avatarUrl, name = name, size = size)
    }
}

/**
 * Плитка участника без трансляции: фон — размытая/затемнённая аватарка (или
 * градиент, если фото нет), по центру круглый аватар (фото либо инициалы с
 * градиентом). Blur работает на API 31+, ниже — тёмный scrim (дизайн допускает
 * «блюр или тёмная alpha»).
 */
@Composable
private fun NoVideoAvatar(
    avatarUrl: String?,
    name: String?,
    modifier: Modifier = Modifier,
    speaking: Boolean = false,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (!avatarUrl.isNullOrEmpty()) {
            AsyncImage(
                model = BuildConfig.IMAGE_SERVER_URL + avatarUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(24.dp),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xCC0B1020)),
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(getGradientForName(name.orEmpty())),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x660B1020)),
            )
        }
        SpeakingAvatar(avatarUrl = avatarUrl, name = name, size = 96.dp, speaking = speaking)
    }
}

@Composable
private fun CallTileBadge(
    label: String,
    isMuted: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(Color(0x99000000), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        Icon(
            painter = painterResource(
                id = if (isMuted) R.drawable.ic_rec_mic_inactive
                else R.drawable.ic_rec_mic_active
            ),
            contentDescription = null,
            tint = if (isMuted) Color(0xFFFF5656) else Color(0xFF4DA6FF),
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun CallParticipantsGrid(
    participants: List<CallParticipant>,
    currentUserId: String?,
    sessionState: CallSessionState,
    speakingIds: Set<String>,
    selfSpeaking: Boolean,
    modifier: Modifier = Modifier,
) {
    val remoteParticipants = remember(participants, currentUserId) {
        if (currentUserId != null) participants.filter { it.id != currentUserId }
        else participants
    }
    val totalTiles = remoteParticipants.size + 1
    val columns = if (totalTiles <= 2) 1 else 2
    val rows = (totalTiles + columns - 1) / columns
    // Свыше 4 плиток — скроллим с фиксированной высотой строки (см. видео-сетку).
    val scrollable = totalTiles > 4

    Column(
        modifier = modifier
            .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (scrollable) Modifier.height(200.dp) else Modifier.weight(1f)),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (colIndex in 0 until columns) {
                    val tileIndex = rowIndex * columns + colIndex
                    if (tileIndex < totalTiles) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            if (tileIndex == 0) {
                                key("local_audio") {
                                    ParticipantCard(
                                        name = "Вы",
                                        avatarUrl = null,
                                        isMuted = !sessionState.micOn,
                                        speaking = selfSpeaking,
                                    )
                                }
                            } else {
                                val participant = remoteParticipants[tileIndex - 1]
                                key(participant.id) {
                                    ParticipantCard(
                                        name = participant.name,
                                        avatarUrl = participant.avatarUrl,
                                        isMuted = participant.isMuted,
                                        speaking = participant.id in speakingIds,
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantCard(
    name: String?,
    avatarUrl: String?,
    isMuted: Boolean,
    speaking: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF121732)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF2A3A6A), Color(0xFF1A2550))
                        )
                    )
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF121732))
                    .padding(3.dp),
                contentAlignment = Alignment.Center,
            ) {
                SpeakingAvatar(
                    avatarUrl = avatarUrl,
                    name = name,
                    size = 72.dp,
                    speaking = speaking,
                )
            }
        }

        CallTileBadge(
            label = name.orEmpty(),
            isMuted = isMuted,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        )
    }
}

/**
 * Баннер «X хочет войти» в звонке (для админа/организатора). Аватар + имя +
 * «Разрешить» (впустить из лобби) / «Нет» (отклонить). Дизайн — карточка внизу
 * над панелью управления.
 */
@Composable
private fun JoinRequestBanner(
    participant: CallParticipant,
    onAllow: () -> Unit,
    onDeny: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF1B2036),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Avatar(url = participant.avatarUrl, name = participant.name, size = 36.dp)
            Text(
                text = stringResource(R.string.call_wants_to_join, participant.name ?: ""),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.call_allow_join),
                color = Color(0xFF4DA6FF),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable { onAllow() }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            )
            Text(
                text = stringResource(R.string.call_deny_join),
                color = Color(0xFFEB5757),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable { onDeny() }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun CallControls(
    sessionState: CallSessionState,
    canUseCamera: Boolean,
    onToggleMicrophone: () -> Unit,
    onToggleCamera: () -> Unit,
    onEndCall: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF0B1020),
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
                iconRes = if (sessionState.micOn) R.drawable.ic_mic_on else R.drawable.ic_mic_off,
                label = stringResource(R.string.call_microphone),
                containerColor = Color(0xFF50515c),
                contentColor = if (sessionState.micOn) Color.White else Color(0xFF8083A0),
                onClick = onToggleMicrophone,
            )
            if (canUseCamera) {
                CallActionButton(
                    iconRes = if (sessionState.camOn) R.drawable.ic_camera_on else R.drawable.ic_camera_off,
                    label = stringResource(R.string.call_camera),
                    containerColor = Color(0xFF50515c),
                    contentColor = if (sessionState.camOn) Color.White else Color(0xFF8083A0),
                    onClick = onToggleCamera,
                )
            }
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantsScreen(
    roster: List<CallParticipant>,
    lobby: List<CallParticipant>,
    isAdmin: Boolean,
    selfId: String?,
    onBackClick: () -> Unit,
    onAllow: (CallParticipant) -> Unit,
    onAllowAll: () -> Unit,
    onMuteAll: () -> Unit,
    onDisableAllCameras: () -> Unit,
    onForbidAllRaiseHand: () -> Unit,
    onParticipantClick: (CallParticipant) -> Unit,
) {
    val backgroundColor = Color(0xFF0B1020)
    val cardColor = Color(0xFF121732)
    val accentBlue = Color(0xFF3B82F6)
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val query = searchQuery.trim().lowercase()
    val filteredRoster = remember(roster, query) {
        if (query.isEmpty()) roster
        else roster.filter { it.name?.lowercase()?.contains(query) == true }
    }
    val filteredLobby = remember(lobby, query) {
        if (query.isEmpty()) lobby
        else lobby.filter { it.name?.lowercase()?.contains(query) == true }
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        BulkActionButton(
                            iconRes = R.drawable.ic_mic_off,
                            label = stringResource(R.string.call_bulk_mute_mics),
                            modifier = Modifier.weight(1f),
                            onClick = onMuteAll,
                        )
                        BulkActionButton(
                            iconRes = R.drawable.ic_camera_off,
                            label = stringResource(R.string.call_bulk_disable_cameras),
                            modifier = Modifier.weight(1f),
                            onClick = onDisableAllCameras,
                        )
                        BulkActionButton(
                            iconRes = R.drawable.ic_call_hand,
                            label = stringResource(R.string.call_bulk_forbid_hand),
                            modifier = Modifier.weight(1f),
                            onClick = onForbidAllRaiseHand,
                        )
                    }
                }

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
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor,
                        disabledContainerColor = cardColor,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    if (isAdmin && filteredLobby.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.call_waiting_room_count,
                                        filteredLobby.size,
                                    ),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = stringResource(R.string.call_allow_all),
                                    color = accentBlue,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.clickable { onAllowAll() },
                                )
                            }
                        }
                        items(filteredLobby) { participant ->
                            LobbyParticipantRow(
                                participant = participant,
                                accent = accentBlue,
                                cardColor = cardColor,
                                onAllow = { onAllow(participant) },
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(
                                R.string.call_participants_count,
                                filteredRoster.size,
                            ),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    items(filteredRoster) { participant ->
                        ParticipantRosterRow(
                            participant = participant,
                            // Меню действий только над другими участниками —
                            // над собой нельзя (нельзя выгнать/назначить себя).
                            isAdmin = isAdmin && participant.id != selfId,
                            cardColor = cardColor,
                            onMenuClick = { onParticipantClick(participant) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BulkActionButton(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF121732),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = label,
                color = Color(0xFFB0B3C5),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
            )
        }
    }
}

@Composable
private fun LobbyParticipantRow(
    participant: CallParticipant,
    accent: Color,
    cardColor: Color,
    onAllow: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardColor,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(url = participant.avatarUrl, name = participant.name, size = 40.dp)
            Text(
                text = participant.name.orEmpty(),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.call_allow),
                color = accent,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onAllow() },
            )
        }
    }
}

@Composable
private fun ParticipantRosterRow(
    participant: CallParticipant,
    isAdmin: Boolean,
    cardColor: Color,
    onMenuClick: () -> Unit,
) {
    val isOrganizer = participant.roles.contains("37:301")
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardColor,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(url = participant.avatarUrl, name = participant.name, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.name.orEmpty(),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (isOrganizer) {
                    Text(
                        text = stringResource(R.string.call_role_organizer),
                        color = Color(0xFFF2C94C),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Icon(
                painter = painterResource(
                    id = if (participant.isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on,
                ),
                contentDescription = null,
                tint = Color(0xFFB0B3C5),
                modifier = Modifier.size(18.dp),
            )
            Icon(
                painter = painterResource(
                    id = if (participant.camOn) R.drawable.ic_camera_on else R.drawable.ic_camera_off,
                ),
                contentDescription = null,
                tint = Color(0xFFB0B3C5),
                modifier = Modifier.size(18.dp),
            )
            if (isAdmin) {
                IconButton(onClick = onMenuClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile_more),
                        contentDescription = null,
                        tint = Color(0xFFB0B3C5),
                        modifier = Modifier.size(18.dp),
                    )
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
    isAdmin: Boolean,
    canAssignAdmin: Boolean,
    onMuteMic: () -> Unit,
    onDisableCamera: () -> Unit,
    onForbidRaiseHand: () -> Unit,
    onWritePrivate: () -> Unit,
    onAssignAdmin: () -> Unit,
    onKick: () -> Unit,
    onOpenProfile: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardColor = Color(0xFF121732)
    val kickRed = Color(0xFFEB5757)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B1020),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.call_settings_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardColor,
                shape = RoundedCornerShape(12.dp),
                onClick = onOpenProfile,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Avatar(url = participant.avatarUrl, name = participant.name, size = 40.dp)
                    Text(
                        text = participant.name.orEmpty(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF8083A0),
                    )
                }
            }

            if (isAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BulkActionButton(
                        iconRes = R.drawable.ic_mic_off,
                        label = stringResource(R.string.call_action_mute_mic),
                        modifier = Modifier.weight(1f),
                        onClick = onMuteMic,
                    )
                    BulkActionButton(
                        iconRes = R.drawable.ic_camera_off,
                        label = stringResource(R.string.call_action_disable_camera),
                        modifier = Modifier.weight(1f),
                        onClick = onDisableCamera,
                    )
                    BulkActionButton(
                        iconRes = R.drawable.ic_call_hand,
                        label = stringResource(R.string.call_bulk_forbid_hand),
                        modifier = Modifier.weight(1f),
                        onClick = onForbidRaiseHand,
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = cardColor,
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SheetActionItem(
                        iconRes = R.drawable.ic_chat,
                        label = stringResource(R.string.call_action_write_private),
                        onClick = onWritePrivate,
                    )
                    if (canAssignAdmin) {
                        SheetActionItem(
                            iconRes = R.drawable.ic_user_profile,
                            label = stringResource(R.string.call_action_assign_admin),
                            onClick = onAssignAdmin,
                        )
                    }
                    if (isAdmin) {
                        SheetActionItem(
                            iconRes = R.drawable.ic_close,
                            label = stringResource(R.string.call_action_kick),
                            onClick = onKick,
                            tint = kickRed,
                        )
                    }
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
    tint: Color = Color.White,
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
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            color = tint,
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

/**
 * Имя файла записи по умолчанию — название звонка и текущая дата
 * (например, «Групповой звонок 2024-03-05»). Подставляется как подсказка
 * в поле названия на [CallRecordingSetupScreen].
 */
private fun defaultRecordingFileName(callTitle: String): String {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    return "$callTitle $date".trim()
}
