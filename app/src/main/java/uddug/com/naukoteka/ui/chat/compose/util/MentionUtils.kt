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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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

    /** Курсор-версия: ищем @query в тексте ДО позиции курсора. */
    fun activeQuery(text: String, cursor: Int): String? {
        val upto = text.substring(0, cursor.coerceIn(0, text.length))
        return ACTIVE_QUERY.find(upto)?.groupValues?.getOrNull(1)
    }

    /** Заменяет активный @query на выбранное `@fullName ` (с пробелом в конце). */
    fun applyMention(text: String, fullName: String): String {
        val match = ACTIVE_QUERY.find(text) ?: return text
        val atIndex = text.lastIndexOf('@', match.range.last)
        if (atIndex < 0) return text
        return text.substring(0, atIndex) + "@" + fullName + " "
    }

    /**
     * Курсор-версия вставки: заменяет @query перед курсором на `@fullName ` и
     * возвращает (новый текст, новую позицию курсора — сразу после пробела).
     */
    fun applyMention(text: String, cursor: Int, fullName: String): Pair<String, Int> {
        val c = cursor.coerceIn(0, text.length)
        val upto = text.substring(0, c)
        ACTIVE_QUERY.find(upto) ?: return text to c
        val at = upto.lastIndexOf('@')
        if (at < 0) return text to c
        val insert = "@$fullName "
        val newText = text.substring(0, at) + insert + text.substring(c)
        return newText to (at + insert.length)
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

/**
 * Подсвечивает синим `@Имя` прямо в поле ввода, не меняя сам текст (маппинг
 * позиций 1:1). [names] — полные имена участников для сопоставления.
 */
class MentionVisualTransformation(
    private val names: List<String>,
    private val color: Color,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val spans = MentionUtils.findMentions(raw, names.map { it to "" })
        val annotated = buildAnnotatedString {
            append(raw)
            spans.forEach { span ->
                addStyle(
                    SpanStyle(color = color, fontWeight = FontWeight.Medium),
                    span.start,
                    span.end,
                )
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
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
                    withStyle(
                        SpanStyle(
                            color = mentionColor,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                        )
                    ) {
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
