package uddug.com.naukoteka.ui.chat.compose.components


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    val backgroundColor = MaterialTheme.colors.background
    val contentColor = MaterialTheme.colors.onBackground

    if (isSelectionMode) {
        TopAppBar(
            title = {
                Text(
                    text = selectedCount.toString(),
                    style = MaterialTheme.typography.h6.copy(color = contentColor)
                )
            },
            navigationIcon = {
                IconButton(onClick = onCloseSelection) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = contentColor
                    )
                }
            },
            actions = {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Delete",
                        tint = contentColor
                    )
                }
                IconButton(onClick = onMoreClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_chat),
                        contentDescription = "More",
                        tint = contentColor
                    )
                }
            },
            backgroundColor = backgroundColor,
            elevation = 0.dp
        )
    } else {
        val currentUser by viewModel.currentUser.collectAsState()
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    currentUser?.image?.path?.let {
                        Avatar(
                            url = currentUser?.image?.path,
                            name = currentUser?.fullName ?: currentUser?.nickname,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = stringResource(R.string.nau_chat),
                        style = MaterialTheme.typography.h6.copy(color = contentColor)
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
                        tint = contentColor
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_chat),
                        contentDescription = "Edit Icon",
                        tint = contentColor
                    )
                }
            },
            navigationIcon = null,
            backgroundColor = backgroundColor,
            elevation = 0.dp
        )
    }
}
