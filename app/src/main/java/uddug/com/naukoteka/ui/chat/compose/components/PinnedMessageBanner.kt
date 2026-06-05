package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.domain.entities.chat.PinnedMessagePreview
import uddug.com.naukoteka.R

@Composable
fun PinnedMessageBanner(
    message: PinnedMessagePreview,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val accentColor = Color(0xFF2E83D9)
    val previewText: String = when {
        !message.text.isNullOrBlank() -> message.text!!
        message.hasAttachment -> "📎 Вложение"
        else -> "Сообщение"
    }
    val ownerLabel = message.ownerName?.takeIf { it.isNotBlank() } ?: "Закрепленное сообщение"

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.main_background))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Vertical accent strip on the left.
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(34.dp)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ownerLabel,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = previewText,
                    color = colorResource(id = R.color.main_text),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter = painterResource(id = R.drawable.ic_chat_pin),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x14000000))
        )
    }
}
