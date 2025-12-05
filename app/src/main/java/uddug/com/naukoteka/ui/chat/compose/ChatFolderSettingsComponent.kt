package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel
import uddug.com.naukoteka.ui.theme.NauTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatFolderSettingsComponent(
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
    onCreateFolderClick: () -> Unit,
) {
    val folders by viewModel.folders.collectAsState()
    val isFolderOrderChanged by viewModel.isFolderOrderChanged.collectAsState()
    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        viewModel.reorderFolders(from.index, to.index)
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.chat_folder_settings_title),
                        color = Color(0xFF10101C)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.persistFolderOrder() },
                        enabled = isFolderOrderChanged
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            tint = if (isFolderOrderChanged) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface.copy(alpha = 0.3f)
                        )
                    }
                },
                backgroundColor = Color.White,
                elevation = 0.dp
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background)
                .padding(padding)
        ) {
            Text(
                text = stringResource(R.string.chat_folder_settings_description),
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = NauTheme.extendedColors.inactive
            )
            Text(
                text = stringResource(R.string.chat_folder_settings_selected_chats),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colors.onBackground
            )
            androidx.compose.material.TextButton(
                onClick = onCreateFolderClick,
            ) {
                Text(
                    text = stringResource(R.string.chat_folder_settings_add_folder),
                    color = MaterialTheme.colors.primary,
                    style = TextStyle.Default
                )
            }
            Column(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp)
                    .wrapContentHeight()
                    .background(NauTheme.extendedColors.backgroundMoreInfo, shape = RoundedCornerShape(16.dp))
            ) {

                LazyColumn(
                    state = reorderState.listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .reorderable(reorderState)
                ) {
                    items(folders, key = { it.id }) { folder ->
                        ReorderableItem(reorderState, key = folder.id) { _ ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .detectReorderAfterLongPress(reorderState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_remove_folder),
                                    contentDescription = null,

                                    )
                                Text(
                                    text = folder.name,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colors.onBackground
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.ic_move_folders),
                                    contentDescription = null,

                                    )
                            }
                        }
                    }
                }
            }

        }
    }
}
