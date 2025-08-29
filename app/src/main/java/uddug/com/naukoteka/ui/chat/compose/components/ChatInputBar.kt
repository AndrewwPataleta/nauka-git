package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import coil.compose.AsyncImage
import uddug.com.naukoteka.R
import uddug.com.domain.entities.chat.MessageChat
import java.io.File

@Composable
fun ChatInputBar(
    modifier: Modifier = Modifier,
    currentMessage: String,
    attachedFiles: List<File>,
    replyMessage: MessageChat?,
    isRecording: Boolean,
    recordedAudio: File?,
    recordingTime: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onAttachClick: () -> Unit,
    onRemoveFile: (File) -> Unit,
    onCancelReply: () -> Unit,
    onDeleteRecording: () -> Unit,
    onSendRecording: () -> Unit,
    onPlayRecording: () -> Unit,
    isRecordingPlaying: Boolean,
) {
    Column(
        modifier = modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAEAF2))
                .height(1.dp)
        )

        replyMessage?.let { reply ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reply.ownerName ?: "",
                        color = Color(0XFF8083A0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = reply.text.orEmpty(),
                        color = Color(0XFF8083A0),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                IconButton(onClick = onCancelReply) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Cancel reply",
                        tint = Color(0XFF8083A0)
                    )
                }
            }
        }

        if (attachedFiles.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(attachedFiles) { file ->
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onRemoveFile(file) }
                    ) {
                        AsyncImage(
                            model = file,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(16.dp)
                                .background(Color.Red, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }

        when {
            recordedAudio != null -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDeleteRecording) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0XFF8083A0)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        RecordedAudioPreview(duration = recordingTime, isPlaying = isRecordingPlaying, onPlay = onPlayRecording)
                    }
                    IconButton(onClick = onSendRecording) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0XFF8083A0))
                    }
                }
            }
            isRecording -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Text(
                            text = recordingTime,
                            color = Color(0XFF8083A0),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    Text(
                        text = "Ведите влево для отмены",
                        color = Color(0XFF8083A0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onVoiceClick) {
                        val transition = rememberInfiniteTransition()
                        val scale by transition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_rec_mic_active),
                            contentDescription = "Stop",
                            modifier = Modifier.scale(scale)
                        )
                    }
                }
            }
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onAttachClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_attach),
                            contentDescription = "Attach",
                            tint = Color(0XFF8083A0)
                        )
                    }

                    TextField(
                        value = currentMessage,
                        onValueChange = onMessageChange,
                        placeholder = { Text("Напишите сообщение") },
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .weight(1f)
                            .height(54.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFEAEAF2),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                    )

                    if (currentMessage.isBlank() && attachedFiles.isEmpty()) {
                        IconButton(onClick = onVoiceClick) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_rec_mic_inactive),
                                contentDescription = "Record",
                            )
                        }
                    } else {
                        IconButton(onClick = onSendClick) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0XFF8083A0))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecordedAudioPreview(duration: String, isPlaying: Boolean, onPlay: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .fillMaxWidth()
            .clickable { onPlay() },
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_voice_wave),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color(0xFF3F7AFF)
                )
            }
            Text(text = duration, color = Color.White)
        }
    }
}