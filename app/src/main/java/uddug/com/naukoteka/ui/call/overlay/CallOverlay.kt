package uddug.com.naukoteka.ui.call.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallUiState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar
import kotlin.math.roundToInt

@Composable
fun CallOverlay(
    state: CallUiState,
    onExpand: () -> Unit,
    onEndCall: () -> Unit,
    onClose: () -> Unit,
    onFinished: () -> Unit,
) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(overlayWidth, overlayHeight)
                .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
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
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            onClick = onExpand,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.05f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        onClick = onClose,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = null,
                        )
                    }
                    IconButton(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        onClick = onEndCall,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close),
                            contentDescription = null,
                        )
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Avatar(
                        url = state.participants.firstOrNull()?.avatarUrl,
                        name = state.participants.firstOrNull()?.name,
                        size = 64.dp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.callTitle ?: state.participants.firstOrNull()?.name
                        ?: stringResource(id = R.string.call_status_in_call),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (state.status) {
                            CallStatus.DIALING -> stringResource(id = R.string.call_status_dialing)
                            CallStatus.CONNECTING -> stringResource(id = R.string.call_status_connecting)
                            CallStatus.IN_CALL -> stringResource(id = R.string.call_status_in_call)
                            CallStatus.FINISHED -> stringResource(id = R.string.call_status_finished)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
