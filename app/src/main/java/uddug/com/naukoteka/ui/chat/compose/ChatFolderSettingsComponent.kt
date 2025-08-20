package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatFolderSettingsComponent(
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
) {
    val folders by viewModel.folders.collectAsState()
    val reorderState = rememberReorderableLazyListState(onMove = { from, to ->
        viewModel.reorderFolders(from.index, to.index)
    })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Настроить папки", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF2E83D9))
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
                .background(Color.White)
                .padding(padding)
        ) {
            Text(
                text = "Вы можете создать папки с нужными чатами и переключаться между ними.\nЧтобы удалить папку или изменить порядок сортировки, нажмите кнопку «Изменить».",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = Color(0xFF8083A0)
            )
            Text(
                text = "Выбранные чаты или группы",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 16.sp,
                color = Color.Black
            )
            androidx.compose.material.TextButton(
                onClick = { },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(text = "Добавить новую папку", color = Color(0xFF2E83D9))
            }
            LazyColumn(
                state = reorderState.listState,
                modifier = Modifier
                    .fillMaxSize()
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
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                            Text(
                                text = folder.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = null,
                                tint = Color(0xFF8083A0)
                            )
                        }
                    }
                }
            }
        }
    }
}
