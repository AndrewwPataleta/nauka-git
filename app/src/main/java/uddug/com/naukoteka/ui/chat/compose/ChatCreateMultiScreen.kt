package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiUiState
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiViewModel
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarCreateMultiComponent
import uddug.com.naukoteka.ui.chat.compose.components.SearchField
import uddug.com.naukoteka.ui.chat.compose.components.UserSelectableItem

@Composable
fun ChatCreateMultiScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatCreateMultiViewModel,
    onCreateClick: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        ChatToolbarCreateMultiComponent(
            onApplyClick = onCreateClick,
            onBackPressed = onBackPressed,
        )

        when (uiState) {
            is ChatCreateMultiUiState.Error -> Unit
            ChatCreateMultiUiState.Loading -> Unit
            is ChatCreateMultiUiState.Success -> {
                val state = uiState as ChatCreateMultiUiState.Success
                SearchField(
                    title = stringResource(R.string.find_chat_message),
                    query = state.query,
                    onSearchChanged = { viewModel.onCurrentSearchChange(it) }
                )
                val users = state.users
                if (users.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(users) { user ->
                            val selected = state.selectedIds.contains(user.userId?.toLongOrNull())
                            UserSelectableItem(
                                user = user,
                                selected = selected,
                                onCheckedChange = { checked ->
                                    viewModel.onUserChecked(user, checked)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
