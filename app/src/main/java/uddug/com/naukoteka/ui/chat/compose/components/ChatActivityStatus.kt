package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatActivity

/**
 * Строка активности в шапке чата: иконка (точки «печатает» / стрелка «отправляет»
 * / волны «голосовое») + акцентный текст. Замещает серый подзаголовок статуса,
 * пока собеседник(и) что-то делают. Данные — из канала `social` (справочник #189).
 * Иконка и текст берут акцент из темы, поэтому одинаково смотрятся в light и dark.
 */
@Composable
fun ChatActivityStatus(
    activity: List<ChatActivity>,
    isGroup: Boolean,
    modifier: Modifier = Modifier,
) {
    val line = rememberChatActivityLine(activity, isGroup) ?: return
    val accent = MaterialTheme.colors.primary
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        when (line.icon) {
            ChatActivityIcon.TYPING -> TypingDots(accent)
            ChatActivityIcon.SENDING -> SendingArrow(accent)
            ChatActivityIcon.VOICE -> VoiceWaves(accent)
        }
        Text(
            text = line.text,
            style = MaterialTheme.typography.caption.copy(color = accent),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

enum class ChatActivityIcon { TYPING, SENDING, VOICE }

data class ChatActivityLine(val icon: ChatActivityIcon, val text: String)

/**
 * Формирует иконку и текст статуса по списку активных участников.
 * - 1:1 — только глагол («пишет», «отправляет файл»).
 * - группа, 1 — «Имя глагол».
 * - группа, 2 — «Имя1, Имя2 пишут».
 * - группа, 3+ — «Имя1 и еще N пишут».
 * Для нескольких участников всегда «пишут» + точки (как в макете).
 */
@Composable
fun rememberChatActivityLine(activity: List<ChatActivity>, isGroup: Boolean): ChatActivityLine? {
    if (activity.isEmpty()) return null

    if (!isGroup || activity.size == 1) {
        val a = activity.first()
        val verb = activityVerb(a.action)
        val text = if (isGroup) {
            stringResource(R.string.chat_activity_named, firstName(a.user), verb)
        } else {
            verb
        }
        return ChatActivityLine(activityIcon(a.action), text)
    }

    val plural = stringResource(R.string.chat_activity_typing_plural)
    val text = if (activity.size == 2) {
        stringResource(
            R.string.chat_activity_two,
            firstName(activity[0].user),
            firstName(activity[1].user),
            plural,
        )
    } else {
        stringResource(
            R.string.chat_activity_many,
            firstName(activity[0].user),
            activity.size - 1,
            plural,
        )
    }
    return ChatActivityLine(ChatActivityIcon.TYPING, text)
}

@Composable
private fun activityVerb(action: Int): String = when (action) {
    ACTIVITY_PHOTO -> stringResource(R.string.chat_activity_photo)
    ACTIVITY_VIDEO -> stringResource(R.string.chat_activity_video)
    ACTIVITY_FILE -> stringResource(R.string.chat_activity_file)
    ACTIVITY_AUDIO -> stringResource(R.string.chat_activity_audio)
    ACTIVITY_VOICE -> stringResource(R.string.chat_activity_voice)
    else -> stringResource(R.string.chat_activity_typing)
}

private fun activityIcon(action: Int): ChatActivityIcon = when (action) {
    ACTIVITY_TYPING -> ChatActivityIcon.TYPING
    ACTIVITY_VOICE -> ChatActivityIcon.VOICE
    else -> ChatActivityIcon.SENDING
}

private fun firstName(user: uddug.com.domain.entities.chat.User): String =
    user.fullName?.trim()?.substringBefore(' ')?.takeIf { it.isNotBlank() }
        ?: user.nickname?.takeIf { it.isNotBlank() }
        ?: ""

// --- Иконки статуса -------------------------------------------------------

@Composable
private fun TypingDots(color: Color) {
    val transition = rememberInfiniteTransition(label = "typing_header")
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 900
                        0.3f at 0
                        1f at 300
                        0.3f at 600
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(index * 150),
                ),
                label = "dot_$index",
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .alpha(alpha)
                    .background(color, CircleShape),
            )
        }
    }
}

@Composable
private fun SendingArrow(color: Color) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val w = size.width
        val h = size.height
        val cy = h / 2f
        val stroke = Stroke(width = w * 0.11f)
        // Горизонтальная линия
        drawLine(
            color = color,
            start = Offset(w * 0.08f, cy),
            end = Offset(w * 0.86f, cy),
            strokeWidth = stroke.width,
        )
        // Наконечник
        drawLine(
            color = color,
            start = Offset(w * 0.58f, h * 0.24f),
            end = Offset(w * 0.9f, cy),
            strokeWidth = stroke.width,
        )
        drawLine(
            color = color,
            start = Offset(w * 0.58f, h * 0.76f),
            end = Offset(w * 0.9f, cy),
            strokeWidth = stroke.width,
        )
    }
}

@Composable
private fun VoiceWaves(color: Color) {
    Canvas(modifier = Modifier.size(14.dp)) {
        val w = size.width
        val h = size.height
        val cy = h / 2f
        // Точка слева
        drawCircle(
            color = color,
            radius = w * 0.12f,
            center = Offset(w * 0.2f, cy),
        )
        // Две дуги, раскрытые вправо
        val stroke = Stroke(width = w * 0.1f)
        drawArc(
            color = color,
            startAngle = -55f,
            sweepAngle = 110f,
            useCenter = false,
            topLeft = Offset(w * 0.05f, h * 0.2f),
            size = Size(w * 0.55f, h * 0.6f),
            style = stroke,
        )
        drawArc(
            color = color,
            startAngle = -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.08f),
            size = Size(w * 0.7f, h * 0.84f),
            style = stroke,
        )
    }
}

// Справочник #189 (cSubType) — дублируем коды локально для маппинга иконок/текста.
private const val ACTIVITY_VIDEO = 1
private const val ACTIVITY_VOICE = 2
private const val ACTIVITY_TYPING = 3
private const val ACTIVITY_FILE = 4
private const val ACTIVITY_PHOTO = 5
private const val ACTIVITY_AUDIO = 6
