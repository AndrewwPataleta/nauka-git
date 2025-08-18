package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatFunctionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFunctionsBottomSheetDialog(
    dialogId: Long,
    onDismissRequest: () -> Unit,
    viewModel: ChatFunctionsViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_functions_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            val items = listOf(
                Triple(R.drawable.ic_mail, R.string.chat_mark_unread) { viewModel.markUnread(dialogId) },
                Triple(R.drawable.ic_attach, R.string.chat_show_attachments) { viewModel.showAttachments(dialogId) },
                Triple(R.drawable.ic_save_post, R.string.chat_pin) { viewModel.pinChat(dialogId) },
                Triple(R.drawable.ic_mute, R.string.chat_disable_notifications) { viewModel.disableNotifications(dialogId) },
                Triple(R.drawable.ic_checkbox_unchecked, R.string.chat_select_messages) { viewModel.selectMessages(dialogId) },
                Triple(R.drawable.ic_lock, R.string.chat_block_chat) { viewModel.blockChat(dialogId) },
                Triple(R.drawable.ic_trash, R.string.chat_delete_chat) { viewModel.deleteChat(dialogId) }
            )
            items.forEach { (iconRes, textRes, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            action()
                            onDismissRequest()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = stringResource(id = textRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
