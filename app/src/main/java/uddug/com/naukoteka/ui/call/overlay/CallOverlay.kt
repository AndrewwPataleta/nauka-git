package uddug.com.naukoteka.ui.call.overlay

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallUiState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallOverlay(
    state: CallUiState,
    onExpand: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMic: () -> Unit,
    onFinished: () -> Unit,
) {
    val backgroundColor = Color(0xFF0B1020)
    val overlayWidth = 240.dp
    val overlayHeight = 160.dp
    val margin = 16.dp
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val widthPx = with(density) { overlayWidth.toPx() }
    val heightPx = with(density) { overlayHeight.toPx() }
    val marginPx = with(density) { margin.toPx() }

    var offset by remember {
        mutableStateOf(
            Offset(
                x = screenWidth - widthPx - marginPx,
                y = screenHeight - heightPx - marginPx,
            )
        )
    }

    LaunchedEffect(state.status) {
        if (state.status == CallStatus.FINISHED) {
            onFinished()
        }
    }

    val callTitle = state.callTitle ?: state.participants.firstOrNull()?.name
    val statusText = when (state.status) {
        CallStatus.IN_CALL -> formatCallDuration(state.callDurationSeconds)
        CallStatus.INCOMING -> stringResource(R.string.call_status_incoming)
        CallStatus.DIALING -> stringResource(R.string.call_status_dialing)
        CallStatus.CONNECTING -> stringResource(R.string.call_status_connecting)
        CallStatus.FINISHED -> stringResource(R.string.call_status_finished)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(overlayWidth, overlayHeight)
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .combinedClickable(onClick = onExpand)
                .pointerInput(screenWidth, screenHeight) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newOffset = offset + dragAmount
                        offset = Offset(
                            x = newOffset.x.coerceIn(marginPx, screenWidth - widthPx - marginPx),
                            y = newOffset.y.coerceIn(marginPx, screenHeight - heightPx - marginPx),
                        )
                    }
                },
            shape = RoundedCornerShape(20.dp),
            color = backgroundColor,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Avatar(
                    url = state.participants.firstOrNull()?.avatarUrl,
                    name = state.participants.firstOrNull()?.name ?: state.callTitle,
                    size = 52.dp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                callTitle?.let { title ->
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Text(
                    text = statusText,
                    color = Color(0xFFB0B3C5),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OverlayCircleButton(
                        iconRes = if (state.sessionState.micOn) {
                            R.drawable.ic_mic_on
                        } else {
                            R.drawable.ic_mic_off
                        },
                        backgroundColor = Color(0xFF121732),
                        iconTint = if (state.sessionState.micOn) Color.White else Color(0xFF8083A0),
                        onClick = onToggleMic,
                    )
                    OverlayCircleButton(
                        iconRes = R.drawable.ic_close,
                        backgroundColor = Color(0xFFE64C4C),
                        iconTint = Color.White,
                        onClick = onEndCall,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayCircleButton(
    iconRes: Int,
    backgroundColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = backgroundColor,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = iconRes),
                tint = iconTint,
                contentDescription = null,
            )
        }
    }
}

private fun formatCallDuration(callDurationSeconds: Int): String {
    val safeSeconds = callDurationSeconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
