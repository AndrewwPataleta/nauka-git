package uddug.com.naukoteka.ui.chat.compose.components


import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbarCreateSingleComponent(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    onActionClick: (() -> Unit)? = null,
    isActionEnabled: Boolean = true,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.chat_create_single_title), fontSize = 20.sp, color = Color.Black)
            }
        },
        actions = {
            if (onActionClick != null) {
                IconButton(onClick = { onActionClick() }, enabled = isActionEnabled) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chat_create_apply),
                        contentDescription = "Edit Icon",
                        tint = if (isActionEnabled) Color(0xFF2E83D9) else Color(0x4D2E83D9)
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chat_back),
                    contentDescription = "Edit Icon",
                    tint = Color(0xFF2E83D9)
                )
            }
        },
        backgroundColor = Color.White,
        elevation = 0.dp
    )
}
