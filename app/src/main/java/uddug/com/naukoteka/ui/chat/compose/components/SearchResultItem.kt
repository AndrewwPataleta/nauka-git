package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.domain.entities.chat.SearchDialog
import uddug.com.domain.entities.chat.SearchMessage
import uddug.com.naukoteka.R
import uddug.com.naukoteka.core.deeplink.formatMessageTime
import uddug.com.naukoteka.mvvm.chat.SearchResult
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SearchResultItem(
    result: SearchResult,
    query: String,
    onClick: (Long) -> Unit,
) {
    when (result) {
        is SearchResult.Dialog -> SearchDialogResultCard(dialog = result.data, onClick = onClick)
        is SearchResult.Message -> SearchMessageResultCard(result = result.data, query = query, onClick = onClick)
    }
}

@Composable
private fun SearchDialogResultCard(
    dialog: SearchDialog,
    onClick: (Long) -> Unit,
) {
    val context = LocalContext.current
    val statusText = remember(dialog.createdAt) {
        formatDialogStatus(context.resources, dialog.createdAt)
    }
    val createdAtIso = remember(dialog.createdAt) { dialog.createdAt.toString() }

    ChatCard(
        dialogId = dialog.dialogId,
        avatarUrl = dialog.image,
        name = dialog.fullName,
        message = statusText,
        time = createdAtIso,
        onChatClick = onClick,
        onChatLongClick = {},
    )
}

@Composable
private fun SearchMessageResultCard(
    result: SearchMessage,
    query: String,
    onClick: (Long) -> Unit,
) {
    val highlightColor = Color(0xFF2E83D9)
    val createdAtIso = remember(result.createdAt) { result.createdAt.toString() }
    val messageText = result.text.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(result.dialogId) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Avatar(url = result.image, name = result.fullName, size = 40.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HighlightedText(
                        text = result.fullName,
                        query = query,
                        style = TextStyle(fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.SemiBold),
                        highlightColor = highlightColor,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatMessageTime(createdAtIso),
                        color = Color(0xFF8083A0),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (messageText.isNotBlank()) {
                    Text(
                        text = buildHighlightedString(messageText, query, highlightColor),
                        color = Color(0xFF4E5068),
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 68.dp),
            color = Color(0xFFEAEAF2),
            thickness = 1.dp,
        )
    }
}

@Composable
private fun HighlightedText(
    text: String,
    query: String,
    style: TextStyle,
    highlightColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildHighlightedString(text, query, highlightColor),
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

private fun buildHighlightedString(
    text: String,
    query: String,
    highlightColor: Color,
): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text)
    }

    val lowerText = text.lowercase(Locale.getDefault())
    val lowerQuery = query.lowercase(Locale.getDefault())

    if (!lowerText.contains(lowerQuery)) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        var currentIndex = 0
        while (currentIndex < text.length) {
            val index = lowerText.indexOf(lowerQuery, currentIndex)
            if (index == -1) {
                append(text.substring(currentIndex))
                break
            }
            if (index > currentIndex) {
                append(text.substring(currentIndex, index))
            }
            val end = index + lowerQuery.length
            withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.SemiBold)) {
                append(text.substring(index, end))
            }
            currentIndex = end
        }
    }
}

private fun formatDialogStatus(resources: android.content.res.Resources, instant: Instant): String {
    val zoneId = ZoneId.systemDefault()
    val dialogTime = instant.atZone(zoneId)
    val now = ZonedDateTime.now(zoneId)
    val rawDuration = Duration.between(dialogTime, now)
    val duration = if (rawDuration.isNegative) Duration.ZERO else rawDuration

    return when {
        duration <= Duration.ofMinutes(5) -> resources.getString(R.string.search_status_online)
        duration < Duration.ofHours(1) -> resources.getString(
            R.string.search_status_minutes_ago,
            duration.toMinutes().toInt().coerceAtLeast(1)
        )
        duration < Duration.ofDays(1) -> resources.getString(
            R.string.search_status_hours_ago,
            duration.toHours().toInt().coerceAtLeast(1)
        )
        else -> {
            val dateText = dialogTime.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
            resources.getString(R.string.search_status_date, dateText)
        }
    }
}
