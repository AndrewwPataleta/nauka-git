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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbarCreateMultiComponent(
    modifier: Modifier = Modifier,
    onApplyClick: () -> Unit,
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Создать чат", fontSize = 20.sp, color = Color.Black)
            }
        },
        actions = {
            IconButton(onClick = { onApplyClick() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chat_create_apply),
                    contentDescription = "Apply Icon",
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { onBackPressed() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chat_back),
                    contentDescription = "Back Icon",
                )
            }
        },
        backgroundColor = Color.White,
        elevation = 0.dp,
    )
}
