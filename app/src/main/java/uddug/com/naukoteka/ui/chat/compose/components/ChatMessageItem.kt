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
import androidx.compose.foundation.border
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
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import coil.request.videoFrameMillis
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.domain.entities.chat.MessageType
import uddug.com.domain.entities.chat.Poll
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.theme.NauTheme
import uddug.com.naukoteka.ui.chat.compose.util.formatVoiceDuration
import uddug.com.naukoteka.ui.chat.compose.util.MessageText
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
    onPollResults: (pollId: String, poll: uddug.com.domain.entities.chat.Poll?) -> Unit = { _, _ -> },
    pollRevoteTrigger: Int = 0,
    onImageClick: (url: String) -> Unit = {},
    onVideoClick: (url: String) -> Unit = {},
    onVoiceMessageClick: (MessageChat, ChatFile) -> Unit = { _, _ -> },
    isPollAuthor: Boolean = false,
    mentionUsers: List<Pair<String, String>> = emptyList(),
    onMentionClick: (String) -> Unit = {},
) {
    val isSystem = message.type == MessageType.SYSTEM && message.files.isEmpty()
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
        // Checkbox is hidden for system messages — they cannot be selected.
        AnimatedVisibility(
            visible = selectionMode && !isSystem,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircleCheckbox(
                    checked = isSelected,
                    onClick = { onSelectChange() },
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
                        if (selectionMode && !isSystem) onSelectChange()
                    },
                    onLongClick = {
                        if (isSystem) return@combinedClickable
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
                    color = NauTheme.extendedColors.chatTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                val chatColors = NauTheme.extendedColors
                Column(
                    modifier = Modifier
                        .background(
                            color = if (isMine) chatColors.accent else chatColors.chatBubbleOther,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                        .widthIn(max = 300.dp)
                ) {
                    if (!isMine && message.ownerName?.isNotEmpty() == true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.ownerName.orEmpty(),
                                color = chatColors.accent,
                                fontSize = 14.sp
                            )
                            if (message.ownerIsAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.chat_admin_label),
                                    fontSize = 10.sp,
                                    color = chatColors.chatTextSecondary
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

                    val isForwarded = message.forwardedFromName != null
                    if (isForwarded) {
                        val forwardText = message.text?.takeIf { it.isNotBlank() }
                            ?: describeMessageContent(message)
                        ForwardedBlock(
                            authorName = message.forwardedFromName!!,
                            text = forwardText,
                            isMine = isMine,
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
                            isPollAuthor = isPollAuthor,
                            onVote = { selected -> onPollVote(message.poll!!.id, selected) },
                            onShowResults = { onPollResults(message.poll!!.id, message.poll) },

                            revoteTrigger = pollRevoteTrigger
                        )
                    } else {
                        if (!isForwarded && !message.text.isNullOrBlank()) {
                            MessageText(
                                text = message.text.orEmpty(),
                                baseColor = if (isMine) Color.White else chatColors.chatTextOther,
                                fontSize = 14.sp,
                                mentionUsers = mentionUsers,
                                onMentionClick = onMentionClick,
                                // На своём (синем) баббле синий тег сливается —
                                // делаем его белым, у собеседников оставляем синим.
                                mentionColor = if (isMine) Color.White else Color(0xFF4DA6FF),
                            )
                        }

                        // A voice message (cType 4) is always rendered as a
                        // voice bubble, even when the echoed file metadata is
                        // sparse — so it never collapses into an empty bubble.
                        if (message.type == MessageType.VOICE) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val voiceFile = message.files.firstOrNull()
                            VoiceMessageContent(
                                file = voiceFile,
                                isMine = isMine,
                                onClick = {
                                    when {
                                        selectionMode -> onSelectChange()
                                        voiceFile != null -> onVoiceMessageClick(message, voiceFile)
                                    }
                                },
                            )
                        }

                        message.files.firstOrNull()
                            ?.takeIf { message.type != MessageType.VOICE }
                            ?.let { file ->
                            Spacer(modifier = Modifier.height(6.dp))
                            val fileUrl = BuildConfig.IMAGE_SERVER_URL.plus(file.path)
                            val isImage = file.contentType?.startsWith("image") == true
                            val isVideo = file.contentType?.startsWith("video") == true
                            val isVoice = file.contentType?.startsWith("audio") == true
                            when {
                                isVoice -> {
                                    VoiceMessageContent(
                                        file = file,
                                        isMine = isMine,
                                        onClick = {
                                            if (selectionMode) onSelectChange()
                                            else onVoiceMessageClick(message, file)
                                        },
                                    )
                                }
                                isImage -> {
                                    Column {
                                        AsyncImage(
                                            model = fileUrl,
                                            contentDescription = "image",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clickable {
                                                    if (selectionMode) onSelectChange()
                                                    else onImageClick(fileUrl)
                                                }
                                        )
                                        file.fileName?.let { name ->
                                            Text(
                                                modifier = Modifier.padding(top = 4.dp),
                                                text = name,
                                                fontSize = 12.sp,
                                                color = if (isMine) Color.White else chatColors.chatTextOther
                                            )
                                        }
                                    }
                                }
                                isVideo -> {
                                    Column {
                                        val videoCtx = LocalContext.current
                                        // Превью видео: сначала пробуем серверное превью
                                        // (?preview — первый ключевой кадр, лёгкое), с фоллбэком
                                        // на кадр из видео на ~2с (coil-video), если превью нет.
                                        val videoThumb = remember(fileUrl) {
                                            ImageRequest.Builder(videoCtx)
                                                .data("$fileUrl?preview")
                                                .videoFrameMillis(2000)
                                                .decoderFactory(VideoFrameDecoder.Factory())
                                                .crossfade(true)
                                                .build()
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .background(chatColors.chatBubbleOther)
                                                .clickable {
                                                    if (selectionMode) onSelectChange()
                                                    else onVideoClick(fileUrl)
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            AsyncImage(
                                                model = videoThumb,
                                                contentDescription = "video preview",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxWidth().height(140.dp),
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0x99000000)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    // Material play-triangle: ic_play_voice — это
                                                    // композит (круг + треугольник), и сплошной белый
                                                    // tint схлопывал его в белый круг без «play».
                                                    imageVector = Icons.Filled.PlayArrow,
                                                    contentDescription = stringResource(id = R.string.chat_play_video),
                                                    tint = Color.White,
                                                    modifier = Modifier.size(30.dp),
                                                )
                                            }
                                        }
                                        file.fileName?.let { name ->
                                            Text(
                                                modifier = Modifier.padding(top = 4.dp),
                                                text = name,
                                                fontSize = 12.sp,
                                                color = if (isMine) Color.White else chatColors.chatTextOther,
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    FileAttachmentCard(
                                        file = file,
                                        isMine = isMine,
                                        selectionMode = selectionMode,
                                        onSelectChange = onSelectChange
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = DateTimeFormatter
                                .ofPattern("HH:mm")
                                .withZone(ZoneId.systemDefault())
                                .format(message.createdAt),
                            fontSize = 10.sp,
                            color = if (isMine) Color.White.copy(alpha = 0.8f) else chatColors.chatTextSecondary,
                        )
                        // Статус доставки — только для своих сообщений
                        // (readCount: 1 отправлено, 2 доставлено, 3 прочитано).
                        if (isMine) {
                            MessageStatusTick(status = message.readCount)
                        }
                    }
                }
            }

        }
    }
}

/**
 * Индикатор статуса своего сообщения: 1 — отправлено (одна галочка), 2 —
 * доставлено (двойная серая), 3 — прочитано (двойная синяя). Пусто, пока статус
 * не пришёл (null/0).
 */
@Composable
private fun MessageStatusTick(status: Int?) {
    val readColor = Color(0xFF9FE7FF)
    val dimColor = Color.White.copy(alpha = 0.8f)
    val (iconRes, tint) = when (status) {
        3 -> R.drawable.ic_readed to readColor
        2 -> R.drawable.ic_readed to dimColor
        1 -> R.drawable.ic_done to dimColor
        else -> return
    }
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(14.dp),
    )
}

@Composable
private fun PollMessageContent(
    poll: Poll,
    question: String?,
    isMine: Boolean,
    isPollAuthor: Boolean,
    onVote: (List<String>) -> Unit,
    onShowResults: () -> Unit,
    revoteTrigger: Int = 0,
) {
    val chatColors = NauTheme.extendedColors
    val primaryTextColor = if (isMine) Color.White else chatColors.chatTextOther
    val secondaryTextColor = if (isMine) Color.White.copy(alpha = 0.75f) else chatColors.chatTextSecondary
    val optionBackground = if (isMine) Color.White.copy(alpha = 0.12f) else chatColors.chatBubbleOther
    val accentColor = if (isMine) Color.White else chatColors.accent
    val buttonContentColor = Color(0xFF9CCDFF)
    val questionText = poll.subject.takeIf { it.isNotBlank() } ?: question
    val isMultiple = poll.multipleAnswers
    val isStopped = poll.isStopped
    val totalVotes = poll.options.sumOf { it.voteCount }

    val hasVoted = poll.options.any { it.isVoted }
    var isRevoting by remember(poll.id) { mutableStateOf(false) }
    val showResults = (hasVoted && !isRevoting) || isStopped

    val selectedOptions = remember(poll.id) { mutableStateListOf<String>() }

    LaunchedEffect(poll.id, poll.options) {
        selectedOptions.clear()
        selectedOptions.addAll(poll.options.filter { it.isVoted }.map { it.id })
    }

    LaunchedEffect(revoteTrigger) {
        if (revoteTrigger > 0) {
            selectedOptions.clear()
            isRevoting = true
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

        if (showResults) {
            val metaParts = buildList {
                add(stringResource(R.string.chat_poll_label))
                val votes = totalVotes.coerceAtLeast(0)
                if (votes > 0) {
                    add("$votes ${pluralizeVotes(votes)}")
                }
                if (poll.isAnonymous) add(stringResource(R.string.chat_poll_results_anonymous))
            }
            Text(
                text = metaParts.joinToString(separator = "  •  "),
                color = secondaryTextColor,
                fontSize = 12.sp,
            )

            poll.options.forEach { option ->
                PollOptionResultItem(
                    option = option,
                    totalVotes = totalVotes,
                    textColor = primaryTextColor,
                    secondaryTextColor = secondaryTextColor,
                    accentColor = accentColor,
                )
            }

            if (!isStopped) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isRevoting = true; selectedOptions.clear() },
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.chat_poll_revote),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = buttonContentColor,
                )
            }

            val resultsButtonColors = ButtonDefaults.textButtonColors(
                contentColor = if (isMine) Color.White else accentColor,
            )
            TextButton(
                onClick = onShowResults,
                colors = resultsButtonColors,
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
        } else {
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

            Text(
                modifier = Modifier.fillMaxWidth().clickable {
                    if (selectedOptions.isNotEmpty()) {
                        onVote(selectedOptions.toList())
                        isRevoting = false
                    }
                },
                textAlign = TextAlign.Center,
                text = stringResource(R.string.chat_poll_vote),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = buttonContentColor,
            )

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
}

@Composable
private fun PollOptionResultItem(
    option: uddug.com.domain.entities.chat.PollOption,
    totalVotes: Int,
    textColor: Color,
    secondaryTextColor: Color,
    accentColor: Color,
) {
    val percent = option.percent
        ?: if (totalVotes > 0) {
            ((option.voteCount.toDouble() / totalVotes) * 100).toInt()
        } else {
            0
        }
    val progress = (percent.coerceIn(0, 100)) / 100f
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "$percent%",
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(40.dp),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = option.value,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = if (option.isVoted) FontWeight.SemiBold else FontWeight.Normal,
                )
                option.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        color = secondaryTextColor,
                        fontSize = 12.sp,
                    )
                }
            }
            if (option.isVoted) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = accentColor,
            backgroundColor = accentColor.copy(alpha = 0.2f),
        )
    }
}

private fun pluralizeVotes(count: Int): String {
    val mod100 = count % 100
    val mod10 = count % 10
    return when {
        mod100 in 11..14 -> "голосов"
        mod10 == 1 -> "голос"
        mod10 in 2..4 -> "голоса"
        else -> "голосов"
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
private fun VoiceMessageContent(
    file: ChatFile?,
    isMine: Boolean,
    onClick: () -> Unit,
) {
    val chatColors = NauTheme.extendedColors
    val durationColor = if (isMine) Color.White.copy(alpha = 0.75f) else chatColors.chatTextSecondary
    val discColor = if (isMine) Color.White else chatColors.accent
    val triangleColor = if (isMine) chatColors.accent else Color.White
    val waveformTint = if (isMine) Color.White.copy(alpha = 0.6f) else chatColors.accent.copy(alpha = 0.5f)
    val durationText = formatVoiceDuration(file?.duration)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(discColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(id = R.string.chat_voice_message),
                tint = triangleColor,
                modifier = Modifier.size(26.dp),
            )
        }
        Image(
            painter = painterResource(id = R.drawable.background_voice_wave),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.tint(waveformTint),
        )
        if (!durationText.isNullOrBlank()) {
            Text(
                text = durationText,
                color = durationColor,
                fontSize = 12.sp,
            )
        }
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
    val chatColors = NauTheme.extendedColors
    val backgroundColor = if (isMine) Color.White.copy(alpha = 0.1f) else chatColors.chatBubbleOther
    val accentColor = if (isMine) Color.White else chatColors.accent
    val supportingColor = if (isMine) Color.White.copy(alpha = 0.75f) else chatColors.chatTextSecondary
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
                    if (isMine) Color.White.copy(alpha = 0.12f) else chatColors.chatFileIconBg
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
                color = if (isMine) Color.White else chatColors.chatTextOther,
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

/**
 * Custom circular checkbox without the 48dp minimum touch target that Material
 * [androidx.compose.material.Checkbox] enforces — keeps the chat row height
 * identical to non-selection mode.
 */
@Composable
private fun CircleCheckbox(
    checked: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = NauTheme.extendedColors.accent
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (checked) activeColor else Color.Transparent)
            .border(
                width = 2.dp,
                color = activeColor,
                shape = CircleShape,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun describeMessageContent(message: MessageChat): String? {
    if (message.type == MessageType.POLL) return stringResource(R.string.chat_poll_label)
    if (message.type == MessageType.VOICE) return stringResource(R.string.chat_voice_message)
    val file = message.files.firstOrNull() ?: return null
    val ct = file.contentType?.lowercase()
    return when {
        ct?.startsWith("image") == true -> stringResource(R.string.chat_last_message_image)
        ct?.startsWith("video") == true -> stringResource(R.string.chat_last_message_video)
        ct?.startsWith("audio") == true -> stringResource(R.string.chat_voice_message)
        file.fileType == 1 -> stringResource(R.string.chat_last_message_image)
        file.fileType == 30 -> stringResource(R.string.chat_last_message_video)
        file.fileType == 21 -> stringResource(R.string.chat_voice_message)
        else -> stringResource(R.string.chat_last_message_file)
    }
}

