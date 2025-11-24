package uddug.com.naukoteka.ui.call.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
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

import uddug.com.naukoteka.mvvm.call.CallStatus
import uddug.com.naukoteka.mvvm.call.CallUiState
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallScreen(
    state: CallUiState,
    onBackPressed: () -> Unit,
    onEndCall: () -> Unit,
) {
//    val backgroundColor = Color(0xFF0B1020)
//    val statusText = when (state.status) {
//        CallStatus.DIALING -> stringResource(R.string.call_status_dialing)
//        CallStatus.CONNECTING -> stringResource(R.string.call_status_connecting)
//        CallStatus.IN_CALL -> stringResource(R.string.call_status_in_call)
//        CallStatus.FINISHED -> stringResource(R.string.call_status_finished)
//    }
//
//    Scaffold(
//        containerColor = backgroundColor,
//        topBar = {
//            TopAppBar(
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = backgroundColor,
//                    navigationIconContentColor = Color.White
//                ),
//                title = {},
//                navigationIcon = {
//                    IconButton(onClick = onBackPressed) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_close),
//                            tint = Color.White,
//                            contentDescription = null,
//                        )
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(horizontal = 24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.SpaceBetween,
//        ) {
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Avatar(
//                    url = state.avatarUrl,
//                    name = state.contactName,
//                    size = 120.dp,
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(
//                    text = state.contactName.orEmpty(),
//                    color = Color.White,
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 24.sp,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth(),
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Text(
//                    text = statusText,
//                    color = Color(0xFFB0B3C5),
//                    fontSize = 16.sp,
//                )
//                Spacer(modifier = Modifier.height(32.dp))
//                if (state.status == CallStatus.DIALING || state.status == CallStatus.CONNECTING) {
//                    CircularProgressIndicator(color = Color.White)
//                }
//            }
//
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly,
//                ) {
//                    CallActionButton(
//                        iconRes = R.drawable.ic_rec_mic_active,
//                        label = stringResource(R.string.call_microphone),
//                        containerColor = Color(0xFF121732),
//                        contentColor = Color.White,
//                    ) {}
//                    CallActionButton(
//                        iconRes = R.drawable.ic_profile_call,
//                        label = stringResource(R.string.call_primary_action),
//                        containerColor = Color(0xFF121732),
//                        contentColor = Color.White,
//                    ) {}
//                    CallActionButton(
//                        iconRes = R.drawable.ic_profile_send,
//                        label = stringResource(R.string.call_secondary_action),
//                        containerColor = Color(0xFF121732),
//                        contentColor = Color.White,
//                    ) {}
//                }
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(12.dp)),
//                    color = Color(0xFF1D2239),
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 12.dp),
//                        horizontalArrangement = Arrangement.SpaceEvenly,
//                        verticalAlignment = Alignment.CenterVertically,
//                    ) {
//                        CallActionButton(
//                            iconRes = R.drawable.ic_rec_mic_inactive,
//                            label = stringResource(R.string.call_microphone),
//                            containerColor = Color.Transparent,
//                            contentColor = Color.White,
//                        ) {}
//                        CallActionButton(
//                            iconRes = R.drawable.ic_mute_sound_folder,
//                            label = stringResource(R.string.call_speaker),
//                            containerColor = Color.Transparent,
//                            contentColor = Color.White,
//                        ) {}
//                        CallActionButton(
//                            iconRes = R.drawable.ic_close,
//                            label = stringResource(R.string.call_terminate_action),
//                            containerColor = Color(0xFFE64C4C),
//                            contentColor = Color.White,
//                            onClick = onEndCall,
//                        )
//                    }
//                }
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//        }
//    }
//}
//
//@Composable
//private fun CallActionButton(
//    iconRes: Int,
//    label: String,
//    containerColor: Color,
//    contentColor: Color,
//    onClick: () -> Unit,
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Surface(
//            modifier = Modifier
//                .size(56.dp)
//                .clip(CircleShape),
//            color = containerColor,
//            contentColor = contentColor,
//            onClick = onClick,
//        ) {
//            Box(contentAlignment = Alignment.Center) {
//                Icon(
//                    painter = painterResource(id = iconRes),
//                    contentDescription = label,
//                    tint = contentColor,
//                )
//            }
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodySmall,
//            color = contentColor,
//            textAlign = TextAlign.Center,
//        )
//    }
}
