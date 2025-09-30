import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.core.deeplink.formatMessageTime
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatCard(
    dialogId: Long,
    avatarUrl: String?,
    name: String,
    message: String,
    time: String,
    newMessagesCount: Int? = null,
    attachment: String? = null,
    isRepost: Boolean = false,
    isMedia: Boolean = false,
    isFromMe: Boolean = false,
    notificationsDisable: Boolean = false,
    isPinned: Boolean = false,
    isMuted: Boolean = false,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectChange: (Boolean) -> Unit = {},
    onChatClick: (Long) -> Unit,
    onChatLongClick: (Long) -> Unit
) {
    // Определяем форматирование даты
    val formattedTime = formatMessageTime(time)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .combinedClickable(
                onClick = {
                    if (selectionMode) {
                        onSelectChange(!isSelected)
                    } else {
                        onChatClick(dialogId)
                    }
                },
                onLongClick = {
                    if (selectionMode) {
                        onSelectChange(!isSelected)
                    } else {
                        onChatLongClick(dialogId)
                    }
                }
            ),  // Убираем отступы
        colors = CardDefaults.cardColors(containerColor = Color.White)  // Белый фон
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Avatar(avatarUrl, name, size = 40.dp)
                    if (selectionMode && isSelected) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color(0xFF2EB66D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))

                // Основной контент
                Column(modifier = Modifier.weight(1f)) {
                    // Имя
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (name.isNullOrEmpty()) {
                                stringResource(R.string.group_chat)
                            } else {
                                name
                            }, style = TextStyle(fontSize = 16.sp, color = Color.Black)
                        )
                        if (isMuted) {
                            Icon(
                                modifier = Modifier
                                    .padding(start = 2.dp)
                                    .size(18.dp),
                                painter = painterResource(id = R.drawable.ic_mute),
                                contentDescription = "Muted",
                                tint = Color.Gray
                            )
                        }
                    }


                    // Последнее сообщение
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when {
                                isRepost -> "Репост: $message"
                                isMedia -> "Медиа-вложение"
                                isFromMe -> "Вы: $message"
                                else -> message
                            },
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = if (isFromMe) Color.Blue else Color.Gray,
                                fontWeight = if (isRepost) FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )

                        // Иконка медиа вложения, если оно есть
                        if (isMedia && attachment?.isNotEmpty() == true) {
                            AsyncImage(
                                model = BuildConfig.IMAGE_SERVER_URL + attachment,
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RectangleShape)
                            )
                        }
                    }
                }
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPinned) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chat_pin),
                                contentDescription = "Pinned",
                                tint = Color(0xFF2E83D9),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                        Text(
                            text = formattedTime,
                            style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                        )
                    }

                    // Число новых сообщений
                    newMessagesCount?.let {
                        if (it > 0) {
                            val circleColor = if (notificationsDisable) Color.Gray else Color(0xFF2E83D9)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(24.dp)
                                    .background(circleColor, CircleShape)
                            ) {
                                Text(
                                    text = it.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                // Время

            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFEAEAF2))
            )
        }
    }
}

fun formatMessageTime(time: String): String {
    // Ваш код для форматирования времени
    return time
}
