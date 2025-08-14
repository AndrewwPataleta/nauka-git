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
import uddug.com.naukoteka.BuildConfig
import java.time.format.DateTimeFormatter

@Composable
fun Avatar(url: String?) {
    if (url != null) {
        AsyncImage(
            model = BuildConfig.IMAGE_SERVER_URL.plus(url),
            contentDescription = "avatar",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Green)
        )
    }
}
