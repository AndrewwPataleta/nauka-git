package uddug.com.naukoteka.ui.call.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.AudioRoute
import uddug.com.naukoteka.mvvm.call.AudioRouteType
import uddug.com.naukoteka.mvvm.call.CallParticipant
import uddug.com.naukoteka.mvvm.call.CallUiState
import uddug.com.naukoteka.mvvm.call.CameraDevice
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

private val SheetBackground = Color(0xFF0B1020)
private val CardBackground = Color(0xFF121732)
private val Accent = Color(0xFF4DA6FF)
private val Muted = Color(0xFFB0B3C5)

// Разрешения участника (см. docs/calls.md, справочник #82).
private const val PERMIT_USE_CAMERA = "82:602"
private const val PERMIT_USE_MIC = "82:603"
private const val PERMIT_SHARE_SCREEN = "82:605"
private const val PERMIT_JOIN_CALL = "82:606"

/** Лист «Настройки» — корневая шторка звонка. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallSettingsSheet(
    state: CallUiState,
    onDismiss: () -> Unit,
    onOpenAudioDevices: () -> Unit,
    onOpenCameraDevices: () -> Unit,
    onToggleHand: () -> Unit,
    onRecordClick: () -> Unit,
    onOpenParticipants: () -> Unit,
    onOpenParticipantPermits: () -> Unit,
    onShareLink: () -> Unit,
    onToggleScreenShare: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isSharingScreen = state.sessionState.sharingScreen

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SheetHeader(title = "Настройки", onClose = onDismiss)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                QuickActionButton(
                    iconRes = R.drawable.ic_call_screen,
                    label = if (isSharingScreen) "Остановить\nпоказ" else "Расшарить\nэкран",
                    active = isSharingScreen,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleScreenShare,
                )
                QuickActionButton(
                    iconRes = R.drawable.ic_call_hand,
                    label = "Поднять\nруку",
                    active = state.sessionState.handUp,
                    modifier = Modifier.weight(1f),
                    onClick = onToggleHand,
                )
                QuickActionButton(
                    iconRes = R.drawable.ic_call_record,
                    label = "Запись\nзвонка",
                    iconTint = Color(0xFFFF5656),
                    active = state.isRecording,
                    modifier = Modifier.weight(1f),
                    onClick = onRecordClick,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SettingsNavRow(
                    iconRes = R.drawable.ic_call_headset,
                    title = "Микрофон и динамик",
                    subtitle = state.currentAudioRouteName ?: "Динамик телефона",
                    onClick = onOpenAudioDevices,
                )
                if (state.isGroupCall || state.isVideoCall) {
                    SettingsNavRow(
                        iconRes = R.drawable.ic_camera,
                        title = "Камера",
                        subtitle = state.currentCameraName ?: "Основная камера",
                        onClick = onOpenCameraDevices,
                    )
                }
                // «Участники звонка» и управление участниками — только в групповом
                // звонке. В 1-на-1 и так понятно, кто в звонке.
                if (state.isGroupCall) {
                    SettingsNavRow(
                        iconRes = R.drawable.ic_call_people,
                        title = "Участники звонка",
                        onClick = onOpenParticipants,
                    )
                    if (state.isCurrentUserAdmin) {
                        SettingsNavRow(
                            iconRes = R.drawable.ic_settings,
                            title = "Настройки для участников",
                            onClick = onOpenParticipantPermits,
                        )
                    }
                }
                SettingsNavRow(
                    iconRes = R.drawable.ic_share,
                    title = "Отправить ссылку",
                    onClick = onShareLink,
                )
            }
        }
    }
}

/** Лист «Выбор устройства» — переключение вывода звука (гарнитура/динамик/BT). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioDeviceSheet(
    routes: List<AudioRoute>,
    currentRouteId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeader(title = "Выбор устройства", onClose = onDismiss)

            if (routes.isEmpty()) {
                Text(
                    text = "Нет доступных устройств",
                    color = Muted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    routes.forEach { route ->
                        AudioDeviceRow(
                            route = route,
                            selected = route.id == currentRouteId,
                            onClick = {
                                onSelect(route.id)
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

/** Лист «Выбор камеры» — фронтальная/основная/внешние (BT). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDeviceSheet(
    cameras: List<CameraDevice>,
    currentCameraId: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeader(title = "Выбор камеры", onClose = onDismiss)

            if (cameras.isEmpty()) {
                Text(
                    text = "Нет доступных камер",
                    color = Muted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    cameras.forEach { camera ->
                        CameraDeviceRow(
                            camera = camera,
                            selected = camera.id == currentCameraId,
                            onClick = {
                                onSelect(camera.id)
                                onDismiss()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraDeviceRow(
    camera: CameraDevice,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = camera.name,
            color = if (selected) Accent else Color.White,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** Лист «Что может участник» — редактор разрешений (только для админов). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantPermitsSheet(
    participant: CallParticipant,
    initialVolume: Float,
    onSetPermit: (permit: String, grant: Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var canSpeak by remember { mutableStateOf(participant.permits.contains(PERMIT_USE_MIC)) }
    var canCamera by remember { mutableStateOf(participant.permits.contains(PERMIT_USE_CAMERA)) }
    var canShareScreen by remember { mutableStateOf(participant.permits.contains(PERMIT_SHARE_SCREEN)) }
    // «Комната ожидания» включена, когда у участника НЕТ права свободно
    // присоединяться (он ждёт одобрения).
    var waitingRoom by remember { mutableStateOf(!participant.permits.contains(PERMIT_JOIN_CALL)) }
    var volume by remember { mutableFloatStateOf(initialVolume) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetHeader(title = "Что может участник", onClose = onDismiss)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(url = participant.avatarUrl, name = participant.name, size = 40.dp)
                Text(
                    text = participant.name.orEmpty(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            PermitToggleRow(
                iconRes = R.drawable.ic_mic_on,
                title = "Говорить в микрофон",
                checked = canSpeak,
                onCheckedChange = {
                    canSpeak = it
                    onSetPermit(PERMIT_USE_MIC, it)
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_call_volume),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        onVolumeChange(it)
                    },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent,
                        inactiveTrackColor = CardBackground,
                    ),
                )
            }

            PermitToggleRow(
                iconRes = R.drawable.ic_camera,
                title = "Включать камеру",
                checked = canCamera,
                onCheckedChange = {
                    canCamera = it
                    onSetPermit(PERMIT_USE_CAMERA, it)
                },
            )
            PermitToggleRow(
                iconRes = R.drawable.ic_call_screen,
                title = "Показывать экран",
                checked = canShareScreen,
                onCheckedChange = {
                    canShareScreen = it
                    onSetPermit(PERMIT_SHARE_SCREEN, it)
                },
            )
            PermitToggleRow(
                iconRes = R.drawable.ic_call_hand,
                title = "Комната ожидания",
                subtitle = "Подключение к звонку только после вашего разрешения",
                checked = waitingRoom,
                onCheckedChange = {
                    waitingRoom = it
                    // Включили комнату ожидания → забрали право свободно входить.
                    onSetPermit(PERMIT_JOIN_CALL, !it)
                },
            )
        }
    }
}

@Composable
private fun SheetHeader(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Закрыть",
                tint = Accent,
            )
        }
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun QuickActionButton(
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    active: Boolean = false,
    iconTint: Color = Color.White,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .alpha(if (enabled) 1f else 0.4f),
        color = if (active) Accent.copy(alpha = 0.18f) else CardBackground,
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = if (active) Accent else iconTint,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                color = Muted,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun SettingsNavRow(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Muted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AudioDeviceRow(
    route: AudioRoute,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconRes = when (route.type) {
        AudioRouteType.BLUETOOTH -> R.drawable.ic_call_bluetooth
        AudioRouteType.WIRED -> R.drawable.ic_call_headset
        AudioRouteType.SPEAKER -> R.drawable.ic_call_volume
        AudioRouteType.EARPIECE -> R.drawable.ic_call_headset
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = if (selected) Accent else Color.White,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = route.name,
            color = if (selected) Accent else Color.White,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check),
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PermitToggleRow(
    iconRes: Int,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CardBackground,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}
