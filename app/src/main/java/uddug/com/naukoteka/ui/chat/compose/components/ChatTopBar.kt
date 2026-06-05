package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R

@Composable
fun ChatTopBar(
    name: String,
    image: String,
    isGroup: Boolean,
    status: String?,
    firstParticipantName: String? = null,
    onDetailClick: () -> Unit,
    onCallClick: () -> Unit = {},
    onBackPressed: () -> Unit,
    onMoreClick: () -> Unit,
) {
    Surface(elevation = 0.dp, color = MaterialTheme.colors.background) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onBackPressed() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.primary)
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(36.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDetailClick()
                        }
                ) {
                    Avatar(
                        url = image.takeIf { it.isNotEmpty() },
                        name = if (isGroup) null else name,
                        size = 36.dp,
                        overrideInitials = if (isGroup) stringResource(R.string.chat_group_initial) else null,
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onDetailClick()
                        }
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.subtitle1.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colors.onBackground
                        )
                    )
                    status?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.caption.copy(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Кнопка звонка одинаковая в личных и групповых чатах:
                // в группе она запускает групповой звонок.
                IconButton(onClick = { onCallClick() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile_call),
                        contentDescription = "Call",
                        tint = MaterialTheme.colors.primary
                    )
                }

                IconButton(onClick = onMoreClick) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colors.primary)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
            )
        }
    }
}

