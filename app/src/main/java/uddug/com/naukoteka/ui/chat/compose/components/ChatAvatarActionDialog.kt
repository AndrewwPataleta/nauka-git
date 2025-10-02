package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import uddug.com.naukoteka.R

@Composable
fun ChatAvatarActionDialog(
    onDismissRequest: () -> Unit,
    onViewClick: () -> Unit,
    onReplaceClick: () -> Unit,
    onDeleteClick: () -> Unit,
    canView: Boolean,
    canDelete: Boolean,
) {
    val actions = buildList {
        if (canView) {
            add(
                AvatarAction(
                    icon = AvatarActionIcon.View,
                    textRes = R.string.chat_avatar_action_view,
                    onClick = onViewClick,
                    tint = Color(0xFF2E83D9),
                )
            )
        }
        add(
            AvatarAction(
                icon = AvatarActionIcon.Replace,
                textRes = R.string.chat_avatar_action_replace,
                onClick = onReplaceClick,
                tint = Color(0xFF2E83D9),
            )
        )
        if (canDelete) {
            add(
                AvatarAction(
                    icon = AvatarActionIcon.Delete,
                    textRes = R.string.chat_avatar_action_delete,
                    onClick = onDeleteClick,
                    tint = Color(0xFFE53935),
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                actions.forEach { action ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDismissRequest()
                                action.onClick()
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = action.icon.icon,
                                contentDescription = null,
                                tint = action.tint,
                            )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(id = action.textRes),
                            style = MaterialTheme.typography.body1.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1C1F2E)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Immutable
private data class AvatarAction(
    val icon: AvatarActionIcon,
    val textRes: Int,
    val onClick: () -> Unit,
    val tint: Color,
)

private enum class AvatarActionIcon(val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    View(Icons.Outlined.Visibility),
    Replace(Icons.Outlined.Image),
    Delete(Icons.Outlined.DeleteForever)
}
