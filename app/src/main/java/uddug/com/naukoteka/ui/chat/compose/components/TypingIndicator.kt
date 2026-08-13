package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import uddug.com.domain.entities.chat.User
import uddug.com.naukoteka.ui.theme.NauTheme

/**
 * Индикатор «печатает…»: аватар автора + бабл с анимацией трёх точек. Ставится
 * футером под лентой сообщений (над полем ввода), поэтому визуально «поднимает»
 * сообщения, пока собеседник печатает. Для группового чата показываем аватар
 * первого печатающего (несколько — их аватарки стопкой).
 *
 * Данные о том, кто печатает, приходят по socket-событию typing (см.
 * ChatDialogViewModel.typingUsers). Компонент чисто визуальный.
 */
@Composable
fun TypingIndicator(
    users: List<User>,
    modifier: Modifier = Modifier,
) {
    if (users.isEmpty()) return
    Row(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(
            url = users.first().image,
            name = users.first().fullName,
            size = 34.dp,
        )
        Surface(
            // Тот же светлый фон, что у входящих сообщений — не «серый».
            color = NauTheme.extendedColors.chatBubbleOther,
            shape = RoundedCornerShape(18.dp),
        ) {
            ThreeDots(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            )
        }
    }
}

@Composable
private fun ThreeDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    val dotColor = NauTheme.extendedColors.accent
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 900
                        0.3f at 0
                        1f at 300
                        0.3f at 600
                    },
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset =
                        androidx.compose.animation.core.StartOffset(index * 150),
                ),
                label = "dot_$index",
            )
            Box(
                modifier = Modifier
                    .size(4.5.dp)
                    .clip(CircleShape)
                    .alpha(alpha)
                    .background(dotColor, CircleShape),
            )
        }
    }
}
