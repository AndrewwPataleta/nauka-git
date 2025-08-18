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
import uddug.com.naukoteka.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFunctionsBottomSheetDialog(
    onDismissRequest: () -> Unit,
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
                R.drawable.ic_mail to R.string.chat_mark_unread,
                R.drawable.ic_attach to R.string.chat_show_attachments,
                R.drawable.ic_save_post to R.string.chat_pin,
                R.drawable.ic_mute to R.string.chat_disable_notifications,
                R.drawable.ic_checkbox_unchecked to R.string.chat_select_messages,
                R.drawable.ic_lock to R.string.chat_block_chat,
                R.drawable.ic_trash to R.string.chat_delete_chat
            )
            items.forEach { (iconRes, textRes) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
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
