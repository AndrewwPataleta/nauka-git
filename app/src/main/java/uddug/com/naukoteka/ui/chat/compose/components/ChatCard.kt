import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.core.deeplink.formatMessageTime
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

enum class ChatAttachmentType {
    IMAGE,
    VIDEO,
    FILE,
}

data class ChatAttachmentPreview(
    val path: String?,
    val type: ChatAttachmentType,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatCard(
    dialogId: Long,
    avatarUrl: String?,
    name: String,
    message: String,
    time: String,
    newMessagesCount: Int? = null,
    attachmentPreview: ChatAttachmentPreview? = null,
    isRepost: Boolean = false,
    isGroupChat: Boolean = false,
    isFromMe: Boolean = false,
    authorName: String? = null,
    notificationsDisable: Boolean = false,
    isPinned: Boolean = false,
    isMuted: Boolean = false,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectChange: (Boolean) -> Unit = {},
    onChatClick: (Long) -> Unit,
    onChatLongClick: (Long) -> Unit
) {
    
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
            ),  
        colors = CardDefaults.cardColors(containerColor = Color.White)  
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
                    Avatar(url =avatarUrl, name = name, size = 40.dp)
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

                
                Column(modifier = Modifier.weight(1f)) {
                    
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


                    
                    val messageText = when {
                        isRepost -> stringResource(R.string.chat_last_message_repost, message)
                        attachmentPreview != null -> when (attachmentPreview.type) {
                            ChatAttachmentType.IMAGE -> stringResource(R.string.chat_last_message_image)
                            ChatAttachmentType.VIDEO -> stringResource(R.string.chat_last_message_video)
                            ChatAttachmentType.FILE -> message.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.chat_last_message_file)
                        }
                        !isGroupChat && isFromMe -> stringResource(R.string.chat_last_message_from_me, message)
                        else -> message
                    }.ifBlank { stringResource(R.string.chat_no_messages) }

                    if (isGroupChat) {
                        val authorLabel = if (isFromMe) {
                            stringResource(R.string.chat_last_message_author_you)
                        } else {
                            authorName.orEmpty()
                        }
                        if (authorLabel.isNotBlank()) {
                            Text(
                                text = authorLabel,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    color = Color(0xFF2E83D9),
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = if (isGroupChat) 4.dp else 0.dp)
                    ) {
                        attachmentPreview
                            ?.takeUnless { it.type == ChatAttachmentType.FILE }
                            ?.let { preview ->
                                AttachmentPreview(
                                    preview = preview,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        Text(
                            text = messageText,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = if (!isGroupChat && isFromMe) Color(0xFF2E83D9) else Color(0xFF4E5068),
                                fontWeight = if (isRepost) FontWeight.Bold else FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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

@Composable
private fun AttachmentPreview(
    preview: ChatAttachmentPreview,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFFF2F5FA)),
        contentAlignment = Alignment.Center
    ) {
        when (preview.type) {
            ChatAttachmentType.IMAGE -> {
                AsyncImage(
                    model = BuildConfig.IMAGE_SERVER_URL + (preview.path ?: ""),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            ChatAttachmentType.VIDEO -> {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF2E83D9),
                    modifier = Modifier.size(16.dp)
                )
            }

            ChatAttachmentType.FILE -> {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = null,
                    tint = Color(0xFF2E83D9),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

fun formatMessageTime(time: String): String {

    return time
}
