package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import uddug.com.naukoteka.BuildConfig

@Composable
fun Avatar(url: String?, name: String?, size: Dp = 36.dp) {
    if (!url.isNullOrEmpty()) {
        AsyncImage(
            model = BuildConfig.IMAGE_SERVER_URL.plus(url),
            contentDescription = "avatar",
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        val initials = name.orEmpty()
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
        val gradient = getGradientForName(name.orEmpty())
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp
            )
        }
    }
}

fun getGradientForName(name: String): Brush {
    return when (getHashCodeToString(name.hashCode(), 5)) {
        0 -> Brush.linearGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
        1 -> Brush.linearGradient(listOf(Color(0xFFFFA17F), Color(0xFFFF6F91)))
        2 -> Brush.linearGradient(listOf(Color(0xFFB5FFFC), Color(0xFF6DD5ED)))
        3 -> Brush.linearGradient(listOf(Color(0xFFC3CFE2), Color(0xFFA5A5A5)))
        else -> Brush.linearGradient(listOf(Color(0xFFF857A6), Color(0xFFFF5858)))
    }
}

fun getHashCodeToString(value: Int, mod: Int): Int {
    return kotlin.math.abs(value) % mod
}
