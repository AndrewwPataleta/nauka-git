package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatFunctionsEvent
import uddug.com.naukoteka.mvvm.chat.ChatFunctionsViewModel
import kotlin.collections.buildList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailMoreSheetDialog(
    dialogId: Long,
    isGroup: Boolean,
    onNavigateToProfile: () -> Unit,
    onDismissRequest: () -> Unit,
    onChatDeleted: () -> Unit,
    onEditGroup: () -> Unit,
    isCurrentUserAdmin: Boolean,
    notificationsDisabled: Boolean = false,
    onLeaveGroup: () -> Unit = {},
    onNotificationsChanged: (Boolean) -> Unit = {},
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
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            val items = buildList {
                if (isGroup) {
                    if (isCurrentUserAdmin) {
                        add(R.string.chat_edit_group to {
                            onEditGroup()
                            onDismissRequest()
                        })
                        add(
                            if (notificationsDisabled) R.string.chat_enable_notifications else R.string.chat_disable_notifications
                                to {
                                    if (notificationsDisabled) {
                                        viewModel.enableNotifications(dialogId)
                                        onNotificationsChanged(false)
                                    } else {
                                        viewModel.disableNotifications(dialogId)
                                        onNotificationsChanged(true)
                                    }
                                    onDismissRequest()
                                }
                        )
                        add(R.string.chat_delete_group to {
                            viewModel.deleteGroupChat(dialogId)
                            onDismissRequest()
                        })
                    } else {
                        add(
                            if (notificationsDisabled) R.string.chat_enable_notifications else R.string.chat_disable_notifications
                                to {
                                    if (notificationsDisabled) {
                                        viewModel.enableNotifications(dialogId)
                                        onNotificationsChanged(false)
                                    } else {
                                        viewModel.disableNotifications(dialogId)
                                        onNotificationsChanged(true)
                                    }
                                    onDismissRequest()
                                }
                        )
                        add(R.string.chat_leave_group to {
                            viewModel.leaveGroup(dialogId)
                            onLeaveGroup()
                            onDismissRequest()
                        })
                    }
                } else {
                    add(R.string.chat_go_to_profile to {
                        onNavigateToProfile()
                        onDismissRequest()
                    })
                    add(
                        if (notificationsDisabled) R.string.chat_enable_notifications else R.string.chat_disable_notifications
                            to {
                                if (notificationsDisabled) {
                                    viewModel.enableNotifications(dialogId)
                                    onNotificationsChanged(false)
                                } else {
                                    viewModel.disableNotifications(dialogId)
                                    onNotificationsChanged(true)
                                }
                                onDismissRequest()
                            }
                    )
                    add(R.string.chat_delete_chat to {
                        viewModel.deleteChat(dialogId)
                    })
                }
            }
            items.forEach { (textRes, action) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            action()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = textRes),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

