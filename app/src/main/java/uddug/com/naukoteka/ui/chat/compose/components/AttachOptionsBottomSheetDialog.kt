package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R

enum class AttachOption {
    MEDIA,
    FILE,
    POLL,
    CONTACT,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachOptionsBottomSheetDialog(
    onDismissRequest: () -> Unit,
    onMediaClick: () -> Unit,
    onFileClick: () -> Unit,
    onPollClick: () -> Unit,
    onContactClick: () -> Unit,
    selected: AttachOption? = null,
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
                icon = painterResource(R.drawable.ic_bottom_image),
                text = stringResource(R.string.chat_attach_photo_video),
                isSelected = selected == AttachOption.MEDIA,
                onClick = onMediaClick
            )
            BottomSheetItem(
                icon = painterResource(R.drawable.ic_cloud),
                text = stringResource(R.string.chat_attach_file),
                isSelected = selected == AttachOption.FILE,
                onClick = onFileClick
            )
            BottomSheetItem(
                icon = painterResource(R.drawable.ic_bottom_poll),
                text = stringResource(R.string.chat_attach_poll),
                isSelected = selected == AttachOption.POLL,
                onClick = onPollClick
            )
            BottomSheetItem(
                icon = painterResource(R.drawable.ic_bottom_contacts),
                text = stringResource(R.string.chat_attach_contact),
                isSelected = selected == AttachOption.CONTACT,
                onClick = onContactClick
            )
        }
    }
}

@Composable
private fun BottomSheetItem(
    icon: Painter,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (isSelected) {
                        Color(0xFF2E83D9)
                    } else {
                        Color(0xFFEAEAF2)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = icon,
                contentDescription = text,
//                tint = if (isSelected) {
//                    MaterialTheme.colorScheme.onPrimary
//                } else {
//                    Color(0xFF8083A0)
//                },
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

