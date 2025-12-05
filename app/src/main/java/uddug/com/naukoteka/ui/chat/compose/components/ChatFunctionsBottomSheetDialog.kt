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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatFunctionsEvent
import uddug.com.naukoteka.mvvm.chat.ChatFunctionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFunctionsBottomSheetDialog(
    dialogId: Long,
    isBlocked: Boolean = false,
    isPinned: Boolean = false,
    notificationsDisabled: Boolean = false,
    onDismissRequest: () -> Unit,
    onShowAttachments: (Long) -> Unit,
    onSelectMessages: () -> Unit,
    onPinChange: (Long, Boolean) -> Unit = { _, _ -> },
    onNotificationsChange: (Long, Boolean) -> Unit = { _, _ -> },
    onChatDeleted: () -> Unit = {},
    viewModel: ChatFunctionsViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                ChatFunctionsEvent.ChatDeleted -> {
                    onDismissRequest()
                    onChatDeleted()
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.chat_functions_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.3f,
                    color = colorResource(id = R.color.main_text)
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            val blockItem = if (isBlocked) {
                R.string.chat_unblock_chat to { viewModel.unblockChat(dialogId) }
            } else {
                R.string.chat_block_chat to { viewModel.blockChat(dialogId) }
            }
            val pinItem = if (isPinned) {
                R.string.chat_unpin to {
                    viewModel.unpinChat(dialogId)
                    onPinChange(dialogId, false)
                }
            } else {
                R.string.chat_pin to {
                    viewModel.pinChat(dialogId)
                    onPinChange(dialogId, true)
                }
            }
            val notificationsItem = if (notificationsDisabled) {
                R.string.chat_enable_notifications to {
                    viewModel.enableNotifications(dialogId)
                    onNotificationsChange(dialogId, false)
                }
            } else {
                R.string.chat_disable_notifications to {
                    viewModel.disableNotifications(dialogId)
                    onNotificationsChange(dialogId, true)
                }
            }
            val items = listOf(
                R.string.chat_mark_unread to { viewModel.markUnread(dialogId) },
                R.string.chat_show_attachments to { onShowAttachments(dialogId) },
                pinItem,
                notificationsItem,
                R.string.chat_select_messages to {
                    viewModel.selectMessages(dialogId)
                    onSelectMessages()
                },
                blockItem,
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
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colorResource(id = R.color.main_text)
                        ),
                    )
                }
            }
        }
    }
}
