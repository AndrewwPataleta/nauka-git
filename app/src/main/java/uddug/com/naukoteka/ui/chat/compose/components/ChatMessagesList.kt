package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.naukoteka.ui.chat.compose.formatMessageDate
import uddug.com.naukoteka.ui.chat.compose.messageDate
import uddug.com.naukoteka.ui.chat.compose.shouldShowDateBadge
import java.time.ZoneId

@Composable
fun ChatMessagesList(messages: List<MessageChat>) {
    val context = LocalContext.current
    val zoneId = remember { ZoneId.systemDefault() }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        itemsIndexed(messages) { index, message ->
            val previousMessage = messages.getOrNull(index - 1)
            if (shouldShowDateBadge(previousMessage, message, zoneId)) {
                ChatMessageDateBadge(
                    text = formatMessageDate(
                        context = context,
                        date = message.messageDate(zoneId),
                        zoneId = zoneId
                    )
                )
            }
            Text(
                text = message.text.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
    }
}
