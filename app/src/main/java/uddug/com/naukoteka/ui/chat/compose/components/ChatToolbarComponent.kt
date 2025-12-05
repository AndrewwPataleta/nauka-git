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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
    onCreateChatClick: () -> Unit,
    isSelectionMode: Boolean,
    selectedCount: Int,
    onCloseSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onMoreClick: () -> Unit
) {
    if (isSelectionMode) {
        TopAppBar(
            title = {
                Text(
                    text = selectedCount.toString(),
                    fontSize = 20.sp,
                    color = Color(0xFF10101C)
                )
            },
            navigationIcon = {
                IconButton(onClick = onCloseSelection) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = Color(0xFF10101C)
                    )
                }
            },
            actions = {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Delete",
                        tint = Color(0xFF10101C)
                    )
                }
                IconButton(onClick = onMoreClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_chat),
                        contentDescription = "More",
                        tint = Color(0xFF10101C)
                    )
                }
            },
            backgroundColor = Color.White,
            elevation = 0.dp
        )
    } else {
        val currentUser by viewModel.currentUser.collectAsState()
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(
                        url = currentUser?.image?.path,
                        name = currentUser?.fullName ?: currentUser?.nickname,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.nau_chat),
                        fontSize = 20.sp,
                        color = Color(0xFF10101C)
                    )
                }
            },
            actions = {
                IconButton(onClick = {
                    onCreateChatClick()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_create_chat),
                        contentDescription = "Edit Icon",
                        tint = Color(0xFF10101C)
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_chat),
                        contentDescription = "Edit Icon",
                        tint = Color(0xFF10101C)
                    )
                }
            },
            navigationIcon = null,
            backgroundColor = Color.White,
            elevation = 0.dp
        )
    }
}
