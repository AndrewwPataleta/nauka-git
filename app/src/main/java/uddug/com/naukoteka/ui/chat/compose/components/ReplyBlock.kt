package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Reply
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.domain.entities.chat.MessageChat

@Composable
fun ReplyBlock(
    reply: MessageChat,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
) {
    val resolvedBackgroundColor = backgroundColor ?: if (isMine) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(resolvedBackgroundColor)
            .height(IntrinsicSize.Min)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Reply,
            contentDescription = null,
            tint = Color(0xFF2E83D9),
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(Color(0xFF2E83D9))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = reply.ownerName?.takeIf { it.isNotBlank() } ?: "Ответ",
                color = Color(0xFF2E83D9),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val previewText = when {
                !reply.text.isNullOrBlank() -> reply.text
                else -> reply.files.firstOrNull()?.fileName
            }?.takeIf { it.isNotBlank() }

            if (previewText != null) {
                Text(
                    text = previewText,
                    color = if (isMine) Color.White else Color.Black,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
