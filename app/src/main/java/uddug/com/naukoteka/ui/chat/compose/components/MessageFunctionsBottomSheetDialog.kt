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
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.MessageFunctionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageFunctionsBottomSheetDialog(
    message: MessageChat,
    onDismissRequest: () -> Unit,
    onSelectMessage: () -> Unit,
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
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.message_functions_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.3f
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            val items = listOf(
                R.string.chat_message_reply to { viewModel.reply(message.id) },
                R.string.chat_message_forward to { viewModel.forward(message.id) },
                R.string.chat_message_copy to { viewModel.copy(message.id) },
                R.string.chat_message_select to {
                    viewModel.select(message.id)
                    onSelectMessage()
                },
                R.string.chat_message_show_original to { viewModel.showOriginal(message.id) },
                R.string.chat_message_delete to { viewModel.delete(message.id) }
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
