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
fun MessageFunctionsBottomSheetDialog(
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(R.string.message_functions_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            val items = listOf(
                R.string.chat_message_reply,
                R.string.chat_message_forward,
                R.string.chat_message_copy,
                R.string.chat_message_select,
                R.string.chat_message_show_original,
                R.string.chat_message_delete
            )
            items.forEach { itemRes ->
                Text(
                    text = stringResource(id = itemRes),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

