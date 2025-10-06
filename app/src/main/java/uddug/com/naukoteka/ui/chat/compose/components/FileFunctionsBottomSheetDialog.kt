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
import uddug.com.naukoteka.R

enum class FileFunctionAction {
    COPY_LINK,
    DOWNLOAD,
    SHARE,
    EDIT_INFO,
    SELECT,
    DELETE,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileFunctionsBottomSheetDialog(
    fileName: String,
    onDismissRequest: () -> Unit,
    onActionSelected: (FileFunctionAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.chat_file_functions_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.3f
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (fileName.isNotBlank()) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Spacer(modifier = Modifier.height(10.dp))
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }

            val items = listOf(
                FileFunctionAction.COPY_LINK to R.string.chat_file_action_copy_link,
                FileFunctionAction.DOWNLOAD to R.string.chat_file_action_download,
                FileFunctionAction.SHARE to R.string.chat_file_action_share,
                FileFunctionAction.EDIT_INFO to R.string.chat_file_action_edit_info,
                FileFunctionAction.SELECT to R.string.chat_file_action_select,
                FileFunctionAction.DELETE to R.string.chat_file_action_delete,
            )

            items.forEach { (action, titleRes) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            onActionSelected(action)
                            onDismissRequest()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = titleRes),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
