package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.MessageFunctionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageFunctionsBottomSheetDialog(
    message: MessageChat,
    onDismissRequest: () -> Unit,
    viewModel: MessageFunctionsViewModel = hiltViewModel(),
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
                text = stringResource(R.string.message_functions_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(20.dp))
            val items = listOf(
                Triple(R.drawable.ic_send, R.string.chat_message_reply) { viewModel.reply(message.id) },
                Triple(R.drawable.ic_share, R.string.chat_message_forward) { viewModel.forward(message.id) },
                Triple(R.drawable.ic_copy, R.string.chat_message_copy) { viewModel.copy(message.id) },
                Triple(R.drawable.ic_checkbox_unchecked, R.string.chat_message_select) { viewModel.select(message.id) },
                Triple(R.drawable.ic_more_info, R.string.chat_message_show_original) { viewModel.showOriginal(message.id) },
                Triple(R.drawable.ic_trash, R.string.chat_message_delete) { viewModel.delete(message.id) }
            )
            items.forEach { (iconRes, textRes, action) ->
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
