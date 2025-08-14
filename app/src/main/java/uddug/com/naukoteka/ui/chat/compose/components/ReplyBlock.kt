package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.domain.entities.chat.MessageChat
import java.time.format.DateTimeFormatter

@Composable
fun ReplyBlock(reply: MessageChat) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Text(
            text = reply.ownerName ?: "Ответ",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
            text = reply.text.orEmpty().take(50) + "...",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}
