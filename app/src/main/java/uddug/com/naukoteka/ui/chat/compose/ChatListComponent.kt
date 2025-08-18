package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.ui.chat.compose.components.ChatFunctionsBottomSheetDialog
import uddug.com.naukoteka.ui.chat.compose.components.ChatTabBar
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarComponent
import uddug.com.naukoteka.ui.chat.compose.components.SearchField

@Composable
fun ChatListComponent(
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
    onCreateChatClick: () -> Unit,
    onShowAttachments: (Long) -> Unit,
) {
    var selectedDialogId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(color = Color.White)
    ) {
        ChatToolbarComponent(
            viewModel = viewModel,
            onCreateChatClick = {
                onCreateChatClick()
            },
            onBackPressed = {
                onBackPressed()
            }
        )
        SearchField(
            title = stringResource(R.string.find_chat_message),
            query = "",
            onSearchChanged = {

            }
        )
        ChatTabBar(
            viewModel = viewModel,
            onChatLongClick = { id ->
                selectedDialogId = id
            }
        )
    }

    selectedDialogId?.let { id ->
        ChatFunctionsBottomSheetDialog(
            dialogId = id,
            onDismissRequest = { selectedDialogId = null },
            onShowAttachments = onShowAttachments
        )
    }
}
