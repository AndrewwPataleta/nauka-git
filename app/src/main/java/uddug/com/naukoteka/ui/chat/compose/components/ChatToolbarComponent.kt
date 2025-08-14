package uddug.com.naukoteka.ui.chat.compose.components


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatToolbarComponent(
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
    onCreateChatClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.mock_avatar),
                    contentDescription = "User Icon",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "НауЧат", fontSize = 20.sp, color = Color.Black)
            }
        },
        actions = {
            IconButton(onClick = {
                onCreateChatClick()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_create_chat),
                    contentDescription = "Edit Icon",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_more_chat),
                    contentDescription = "Edit Icon",
                    tint = Color.Black
                )
            }
        },
        navigationIcon = null,
        backgroundColor = Color.White,
        elevation = 0.dp
    )
}
