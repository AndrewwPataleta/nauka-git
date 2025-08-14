import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.core.deeplink.formatMessageTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun CreateChatMemberCard(
    name: String,
    avatarUrl: String,
    time: String,
    onMemberClick: () -> Unit,
) {
    // Определяем форматирование даты
    val formattedTime = formatMessageTime(time)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .clickable {
                onMemberClick()
            },  // Убираем отступы
        colors = CardDefaults.cardColors(containerColor = Color.White)  // Белый фон
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (avatarUrl.isNullOrEmpty()) {

                    val initials = name.split(" ").let {
                        (it.firstOrNull()?.firstOrNull()?.toString() ?: "") +
                                (it.getOrNull(1)?.firstOrNull()?.toString() ?: "")
                    }
                    val gradientRes = getGradientForName(name)

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color(0xFF93DDFF), // Используем XML drawable
                                shape = CircleShape
                            )
                    ) {

                        Text(
                            text = "Ч",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    // Если URL не пустой, загружаем изображение
                    AsyncImage(
                        model = BuildConfig.IMAGE_SERVER_URL + avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                // Основной контент
                Column(modifier = Modifier.weight(1f)) {
                    // Имя
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (name.isEmpty()) {
                                stringResource(R.string.group_chat)
                            } else {
                                name
                            }, style = TextStyle(fontSize = 16.sp, color = Color.Black)
                        )
                    }

                }
            }
            Text(
                text = formattedTime,
                style = TextStyle(fontSize = 12.sp, color = Color.Gray),
                modifier = Modifier
            )
        }
    }
}

