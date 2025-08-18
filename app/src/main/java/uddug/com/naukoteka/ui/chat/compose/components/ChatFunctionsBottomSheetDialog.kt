package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onSelectMessages: () -> Unit,
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
                R.string.chat_mark_unread to { viewModel.markUnread(dialogId) },
                R.string.chat_show_attachments to { viewModel.showAttachments(dialogId) },
                R.string.chat_pin to { viewModel.pinChat(dialogId) },
                R.string.chat_disable_notifications to { viewModel.disableNotifications(dialogId) },
                R.string.chat_select_messages to {
                    viewModel.selectMessages(dialogId)
                    onSelectMessages()
                },
                R.string.chat_block_chat to { viewModel.blockChat(dialogId) },
                R.string.chat_delete_chat to { viewModel.deleteChat(dialogId) }
            )
            items.forEach { (textRes, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            action()
                            onDismissRequest()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = textRes),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
