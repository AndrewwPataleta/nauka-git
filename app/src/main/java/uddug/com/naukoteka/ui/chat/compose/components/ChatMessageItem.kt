package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.naukoteka.BuildConfig
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalTime // если toLocalTime() возвращает LocalTime


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: MessageChat,
    isMine: Boolean,
    onLongPress: (MessageChat) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .defaultMinSize(minWidth = 150.dp)
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = { onLongPress(message) }
            ),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isMine) {
            // Аватарка
            Avatar(message.ownerAvatarUrl)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .background(
                    color = if (isMine) Color(0xFF2E83D9) else Color(0xFFF5F5F9),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)
                .widthIn(max = 300.dp)
        ) {
            if (!isMine && message.ownerName?.isNotEmpty() == true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.ownerName.orEmpty(),
                        color = Color(0xFF2E83D9),
                        fontSize = 14.sp
                    )
                    if (message.ownerIsAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "админ",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            if (message.replyTo != null) {
                ReplyBlock(message.replyTo!!)
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (!message.text.isNullOrBlank()) {
                Text(
                    text = message.text.orEmpty(),
                    color = if (isMine) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }

            message.files.firstOrNull()?.let { file ->
                Spacer(modifier = Modifier.height(6.dp))
                if (file.contentType.startsWith("image")) {
                    Column {
                        AsyncImage(
                            model = BuildConfig.IMAGE_SERVER_URL.plus(file.path),
                            contentDescription = "image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .fillMaxWidth()
                                .height(140.dp)
                        )
                        if (file.fileName != null) {
                            Text(
                                modifier = Modifier.padding(top = 4.dp),
                                text = file.fileName.orEmpty(),
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Menu, contentDescription = "PDF", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Медиа Сообщение", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
            if (message.ownerName?.isNotEmpty() == true) {
                Text(
                    text = LocalDateTime.parse(
                        message.createdAt.toString().substringBeforeLast('.'),
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    ).format(DateTimeFormatter.ofPattern("HH:mm")),
                    fontSize = 10.sp,
                    color = if (isMine) Color.White.copy(alpha = 0.8f) else Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }

        }
    }
}
