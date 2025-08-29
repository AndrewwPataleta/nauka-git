package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R

@Composable
fun ChatInputBar(
    modifier: Modifier = Modifier,
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isVoiceMessage: Boolean = false,
    voiceDuration: String = "0:00",
    onDeleteVoiceClick: () -> Unit = {},
    onPlayVoiceClick: () -> Unit = {}
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

        if (isVoiceMessage) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteVoiceClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Delete voice",
                        tint = Color(0xFFEB5545)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .background(Color(0xFF2E83D9), RoundedCornerShape(40.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPlayVoiceClick,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color(0xFF2E83D9))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.background_voice_wave),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = voiceDuration, color = Color.White)
                }

                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF2E83D9), CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_up),
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* вложения */ }) {
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
                    shape = RoundedCornerShape(12.dp)
                )

                IconButton(onClick = onSendClick) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0XFF8083A0))
                }
            }
        }
    }
}
