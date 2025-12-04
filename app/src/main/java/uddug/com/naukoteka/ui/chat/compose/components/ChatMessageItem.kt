package uddug.com.naukoteka.ui.chat.compose.components

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.chat.Poll
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R

import uddug.com.naukoteka.ui.chat.compose.util.formatVoiceDuration
import uddug.com.domain.entities.chat.File as ChatFile
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageItem(
    message: MessageChat,
    isMine: Boolean,
    selectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectChange: () -> Unit = {},
    onLongPress: (MessageChat) -> Unit,
    onReplyReferenceClick: (Long) -> Unit = {},
    onPollVote: (pollId: String, optionIds: List<String>) -> Unit = { _, _ -> },
    onPollResults: (pollId: String) -> Unit = {},
    pollRevoteTrigger: Int = 0,
) {
    val isSystem = message.type == MessageType.SYSTEM
    Row(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxWidth()
            .defaultMinSize(minWidth = 150.dp)
            .padding(vertical = 4.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        AnimatedVisibility(
            visible = selectionMode,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    modifier = Modifier.clip(CircleShape),
                    checked = isSelected,
                    onCheckedChange = { onSelectChange() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF2E83D9),
                        uncheckedColor = Color(0xFF2E83D9),
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (selectionMode) onSelectChange()
                    },
                    onLongClick = {
                        if (selectionMode) onSelectChange() else onLongPress(message)
                    }
                ),
            horizontalArrangement = when {
                isSystem -> Arrangement.Center
                isMine -> Arrangement.End
                else -> Arrangement.Start
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isMine && !isSystem) {

                Avatar(url = message.ownerAvatarUrl, name = message.ownerName)
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (isSystem) {
                Text(
                    text = message.text.orEmpty(),
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            } else {
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
                                    text = stringResource(R.string.chat_admin_label),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    if (message.replyTo != null) {
                        ReplyBlock(
                            reply = message.replyTo!!,
                            isMine = isMine,
                            onReplyClick = onReplyReferenceClick
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    val isPollMessage = message.type == MessageType.POLL && message.poll != null

                    if (isPollMessage) {
                        PollMessageContent(
                            poll = message.poll!!,
                            question = message.poll?.subject.takeIf { !it.isNullOrBlank() }
                                ?: message.text,
                            isMine = isMine,
                            onVote = { selected -> onPollVote(message.poll!!.id, selected) },
                            onShowResults = { onPollResults(message.poll!!.id) },

                            revoteTrigger = pollRevoteTrigger
                        )
                    } else {
                        if (!message.text.isNullOrBlank()) {
                            Text(
                                text = message.text.orEmpty(),
                                color = if (isMine) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                        }

                        message.files.firstOrNull()?.let { file ->
                            Spacer(modifier = Modifier.height(6.dp))
                            if (file.contentType?.startsWith("image") == true) {
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
                                    file.fileName?.let { name ->
                                        Text(
                                            modifier = Modifier.padding(top = 4.dp),
                                            text = name,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                FileAttachmentCard(
                                    file = file,
                                    isMine = isMine,
                                    selectionMode = selectionMode,
                                    onSelectChange = onSelectChange
                                )
                            }
                        }
                    }

                    Text(
                        text = DateTimeFormatter
                            .ofPattern("HH:mm")
                            .withZone(ZoneId.systemDefault())
                            .format(message.createdAt),
                        fontSize = 10.sp,
                        color = if (isMine) Color.White.copy(alpha = 0.8f) else Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

        }
    }
}

@Composable
private fun PollMessageContent(
    poll: Poll,
    question: String?,
    isMine: Boolean,
    onVote: (List<String>) -> Unit,
    onShowResults: () -> Unit,
    revoteTrigger: Int = 0,
) {
    val headlineColor = if (isMine) Color.White else Color(0xFF2E83D9)
    val primaryTextColor = if (isMine) Color.White else Color(0xFF111827)
    val secondaryTextColor = if (isMine) Color.White.copy(alpha = 0.75f) else Color(0xFF6F7A90)
    val optionBackground = if (isMine) Color.White.copy(alpha = 0.12f) else Color.White
    val accentColor = if (isMine) Color.White else Color(0xFF2E83D9)
    val buttonContentColor = Color(0xFF9CCDFF)
    val questionText = poll.subject.takeIf { it.isNotBlank() } ?: question
    val isMultiple = poll.multipleAnswers
    val isStopped = poll.isStopped
    val selectedOptions = remember(poll.id) { mutableStateListOf<String>() }

    LaunchedEffect(poll.id, poll.options) {
        selectedOptions.clear()
        selectedOptions.addAll(poll.options.filter { it.isVoted }.map { it.id })
    }

    LaunchedEffect(revoteTrigger) {
        if (revoteTrigger > 0) {
            selectedOptions.clear()
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        if (!questionText.isNullOrBlank()) {
            Text(
                text = questionText,
                color = primaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        val descriptionRes = if (isMultiple) {
            R.string.chat_poll_description_multiple
        } else {
            R.string.chat_poll_description_single
        }
        Text(
            text = stringResource(descriptionRes),
            color = secondaryTextColor,
            fontSize = 12.sp
        )

        poll.options.forEach { option ->
            val isSelected = selectedOptions.contains(option.id)
            PollOptionItem(
                text = option.value,
                isSelected = isSelected,
                isEnabled = !isStopped,
                backgroundColor = optionBackground,
                accentColor = accentColor,
                textColor = primaryTextColor,
            ) {
                if (isMultiple) {
                    if (isSelected) {
                        selectedOptions.remove(option.id)
                    } else {
                        selectedOptions.add(option.id)
                    }
                } else {
                    selectedOptions.clear()
                    selectedOptions.add(option.id)
                }
            }
        }

        Button(
            onClick = { onVote(selectedOptions.toList()) },
            enabled = selectedOptions.isNotEmpty() && !isStopped,

            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_poll_vote),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = buttonContentColor,
            )
        }

        val textButtonColors = ButtonDefaults.textButtonColors(
            contentColor = if (isMine) Color.White else accentColor,
            disabledContentColor = if (isMine) {
                Color.White.copy(alpha = 0.4f)
            } else {
                accentColor.copy(alpha = 0.4f)
            }
        )
        TextButton(
            onClick = onShowResults,
            enabled = poll.options.isNotEmpty(),
            colors = textButtonColors,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.chat_poll_view_results),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PollOptionItem(
    text: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    backgroundColor: Color,
    accentColor: Color,
    textColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled) { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { if (isEnabled) onClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = accentColor,
                unselectedColor = accentColor.copy(alpha = 0.6f),
//                disabledSelectedColor = accentColor.copy(alpha = 0.6f),
//                disabledUnselectedColor = accentColor.copy(alpha = 0.4f),
            ),
            enabled = isEnabled
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FileAttachmentCard(
    file: ChatFile,
    isMine: Boolean,
    selectionMode: Boolean,
    onSelectChange: () -> Unit,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor = if (isMine) Color.White.copy(alpha = 0.1f) else Color.White
    val accentColor = if (isMine) Color.White else Color(0xFF2E83D9)
    val supportingColor = if (isMine) Color.White.copy(alpha = 0.75f) else Color(0xFF6F7A90)
    val fileTypeLabel = file.fileName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() }
        ?.uppercase(Locale.getDefault())
        ?: file.contentType?.substringAfterLast('/', "")?.takeIf { it.isNotBlank() }
            ?.uppercase(Locale.getDefault())
        ?: stringResource(id = R.string.chat_file_attachment_unknown_type)
    val fileSizeText = formatFileSize(file.fileSize)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (selectionMode) {
                    onSelectChange()
                } else {
                    downloadChatFile(context, file)
                }
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isMine) Color.White.copy(alpha = 0.12f) else Color(0xFFE4E8F1)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_file_placeholder),
                contentDescription = null,
                colorFilter = ColorFilter.tint(accentColor),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = fileTypeLabel,
                color = supportingColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = file.fileName.orEmpty(),
                color = if (isMine) Color.White else Color(0xFF111827),
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = supportingColor,
                    modifier = Modifier.size(16.dp)
                )
                if (!fileSizeText.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = fileSizeText,
                        color = supportingColor,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

private fun formatFileSize(sizeInBytes: Int?): String? {
    if (sizeInBytes == null || sizeInBytes <= 0) return null
    val kiloBytes = sizeInBytes / 1024.0
    if (kiloBytes < 1) {
        return String.format(Locale.getDefault(), "%d B", sizeInBytes)
    }
    val megaBytes = kiloBytes / 1024.0
    if (megaBytes < 1) {
        return String.format(Locale.getDefault(), "%.1f KB", kiloBytes)
    }
    val gigaBytes = megaBytes / 1024.0
    if (gigaBytes < 1) {
        return String.format(Locale.getDefault(), "%.1f MB", megaBytes)
    }
    return String.format(Locale.getDefault(), "%.2f GB", gigaBytes)
}

private fun downloadChatFile(context: Context, file: ChatFile) {
    val downloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return
    val fileName = file.fileName?.takeIf { it.isNotBlank() } ?: "chat_file_${file.id}"
    val request = DownloadManager.Request(Uri.parse(BuildConfig.IMAGE_SERVER_URL + file.path))
        .setTitle(fileName)
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
    file.contentType?.let { request.setMimeType(it) }
    downloadManager.enqueue(request)
    Toast.makeText(
        context,
        context.getString(R.string.chat_file_download_started),
        Toast.LENGTH_SHORT
    ).show()
}


