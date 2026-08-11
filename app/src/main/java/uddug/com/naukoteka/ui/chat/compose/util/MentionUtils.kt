package uddug.com.naukoteka.ui.chat.compose.util

import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

/**
 * @упоминания в групповых чатах. Формат в тексте — обычный `@Имя Фамилия`
 * (кросс-платформенно: iOS/web видят простой текст). Подсветка и клик строятся
 * на клиенте по ростеру участников диалога — сопоставляем самое длинное имя.
 */
object MentionUtils {

    // Активное упоминание в конце строки: @ (в начале строки или после пробела),
    // затем непробельные символы. Используется для подсказок при вводе.
    private val ACTIVE_QUERY = Regex("(?:^|\\s)@([^\\s@]*)$")

    /** Текущий вводимый @query (может быть пустым) или null, если упоминания нет. */
    fun activeQuery(text: String): String? = ACTIVE_QUERY.find(text)?.groupValues?.getOrNull(1)

    /** Заменяет активный @query на выбранное `@fullName ` (с пробелом в конце). */
    fun applyMention(text: String, fullName: String): String {
        val match = ACTIVE_QUERY.find(text) ?: return text
        val atIndex = text.lastIndexOf('@', match.range.last)
        if (atIndex < 0) return text
        return text.substring(0, atIndex) + "@" + fullName + " "
    }

    data class MentionSpan(val start: Int, val end: Int, val userId: String)

    /**
     * Находит в тексте диапазоны `@<fullName>` для известных участников.
     * [participants] — пары (fullName, userId). Сопоставление жадное по самому
     * длинному имени, чтобы «@Анна Иванова» не схлопнулась в «@Анна».
     */
    fun findMentions(
        text: String,
        participants: List<Pair<String, String>>,
    ): List<MentionSpan> {
        if (text.isEmpty()) return emptyList()
        val sorted = participants
            .filter { it.first.isNotBlank() }
            .sortedByDescending { it.first.length }
        if (sorted.isEmpty()) return emptyList()

        val spans = mutableListOf<MentionSpan>()
        var i = 0
        while (i < text.length) {
            if (text[i] == '@') {
                val rest = text.substring(i + 1)
                val match = sorted.firstOrNull { rest.startsWith(it.first) }
                if (match != null) {
                    val end = i + 1 + match.first.length
                    spans += MentionSpan(i, end, match.second)
                    i = end
                    continue
                }
            }
            i++
        }
        return spans
    }
}

private const val MENTION_TAG = "mention"

/**
 * Рендер текста сообщения с кликабельными @упоминаниями (синим). Если совпадений
 * нет — обычный текст. Тап по тегу вызывает [onMentionClick] с userId.
 */
@Composable
fun MessageText(
    text: String,
    baseColor: Color,
    fontSize: TextUnit,
    mentionUsers: List<Pair<String, String>>,
    onMentionClick: (String) -> Unit,
    mentionColor: Color = Color(0xFF4DA6FF),
) {
    val spans = remember(text, mentionUsers) {
        MentionUtils.findMentions(text, mentionUsers)
    }

    val annotated: AnnotatedString = remember(text, spans, baseColor) {
        if (spans.isEmpty()) {
            AnnotatedString(text)
        } else {
            buildAnnotatedString {
                var cursor = 0
                spans.forEach { span ->
                    if (span.start > cursor) append(text.substring(cursor, span.start))
                    pushStringAnnotation(MENTION_TAG, span.userId)
                    withStyle(SpanStyle(color = mentionColor, fontWeight = FontWeight.Medium)) {
                        append(text.substring(span.start, span.end))
                    }
                    pop()
                    cursor = span.end
                }
                if (cursor < text.length) append(text.substring(cursor))
            }
        }
    }

    ClickableText(
        text = annotated,
        style = TextStyle(color = baseColor, fontSize = fontSize),
        onClick = { offset ->
            annotated.getStringAnnotations(MENTION_TAG, offset, offset)
                .firstOrNull()
                ?.let { onMentionClick(it.item) }
        },
    )
}
