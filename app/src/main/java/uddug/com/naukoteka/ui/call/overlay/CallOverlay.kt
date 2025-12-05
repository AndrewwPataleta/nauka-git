package uddug.com.naukoteka.ui.call.overlay

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onClose: () -> Unit,
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
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(32.dp)
                        .clip(CircleShape),
                    color = Color(0xFFE64C4C),
                    contentColor = Color.White,
                    onClick = onEndCall,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                        )
                    }
                }

                IconButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clip(CircleShape),
                    onClick = onClose,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Avatar(
                    modifier = Modifier.align(Alignment.Center),
                    url = state.participants.firstOrNull()?.avatarUrl,
                    name = state.participants.firstOrNull()?.name,
                    size = 74.dp,
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    callTitle?.let { title ->
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Text(
                        text = statusText,
                        color = Color(0xFFB0B3C5),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp)
                        .clip(CircleShape),
                    color = Color(0xFF121732),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = {},
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_rec_mic_inactive),
                            tint = Color.White,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

private fun formatCallDuration(callDurationSeconds: Int): String {
    val safeSeconds = callDurationSeconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
