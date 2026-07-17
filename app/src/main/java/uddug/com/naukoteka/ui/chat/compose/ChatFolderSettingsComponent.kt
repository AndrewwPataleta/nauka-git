package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatFolderSettingsComponent(
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
    onCreateFolderClick: () -> Unit,
) {
    val folders by viewModel.folders.collectAsState()
    val isFolderOrderChanged by viewModel.isFolderOrderChanged.collectAsState()
    val mainFolderId = folders.firstOrNull()?.id
    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        viewModel.reorderFolders(from.index, to.index)
    })

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 8.dp),
                title = {
                    Text(
                        text = stringResource(R.string.chat_folder_settings_title),
                        color = MaterialTheme.colors.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_back),
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
                            painter = painterResource(id = R.drawable.ic_chat_create_apply),
                            contentDescription = null,
                            tint = if (isFolderOrderChanged) MaterialTheme.colors.primary else MaterialTheme.colors.primary.copy(alpha = 0.3f)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = NauTheme.extendedColors.inactive
            )

            Text(
                text = stringResource(R.string.chat_folder_settings_selected_chats),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.onBackground
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .background(NauTheme.extendedColors.backgroundMoreInfo, shape = RoundedCornerShape(16.dp))
            ) {
                Text(
                    text = stringResource(R.string.chat_folder_settings_add_folder),
                    color = MaterialTheme.colors.primary,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable { onCreateFolderClick() }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                )

                LazyColumn(
                    state = reorderState.listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .reorderable(reorderState)
                ) {
                    itemsIndexed(folders, key = { _, folder -> folder.id }) { index, folder ->
                        val isSystemFolder = (folder.id == mainFolderId) ||
                                folder.name.equals("Заблокированные", ignoreCase = true)
                        ReorderableItem(reorderState, key = folder.id) { _ ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                                    .detectReorderAfterLongPress(reorderState),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isSystemFolder) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_folder_delete_radio),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clickable { viewModel.deleteFolder(folder.id) }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                } else {
                                    Spacer(modifier = Modifier.width(34.dp))
                                }

                                Text(
                                    text = folder.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colors.onBackground
                                )

                                if (folder.unreadCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Badge(
                                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = folder.unreadCount.toString(),
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))
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
