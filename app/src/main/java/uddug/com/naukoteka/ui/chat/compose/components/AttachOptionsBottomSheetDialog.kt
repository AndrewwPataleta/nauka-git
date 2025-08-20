package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachOptionsBottomSheetDialog(
    onDismissRequest: () -> Unit,
    onMediaClick: () -> Unit,
    onFileClick: () -> Unit,
    onPollClick: () -> Unit,
    onContactClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomSheetItem(
                icon = Icons.Filled.Person,
                text = stringResource(R.string.chat_attach_photo_video),
                onClick = onMediaClick
            )
            BottomSheetItem(
                icon = Icons.Filled.Person,
                text = stringResource(R.string.chat_attach_file),
                onClick = onFileClick
            )
            BottomSheetItem(
                icon = Icons.Filled.Person,
                text = stringResource(R.string.chat_attach_poll),
                onClick = onPollClick
            )
            BottomSheetItem(
                icon = Icons.Filled.Person,
                text = stringResource(R.string.chat_attach_contact),
                onClick = onContactClick
            )
        }
    }
}

@Composable
private fun BottomSheetItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.height(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text)
    }
}

