package uddug.com.naukoteka.ui.chat.compose

import CreateChatMemberCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiUiState
import uddug.com.naukoteka.mvvm.chat.ChatCreateMultiViewModel
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarCreateSingleComponent
import uddug.com.naukoteka.ui.chat.compose.components.SearchField

@Composable
fun ChatCreateMultiScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatCreateMultiViewModel,
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val successState = uiState as? ChatCreateMultiUiState.Success
    val isActionEnabled = (successState?.selected?.size ?: 0) >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colors.background)
    ) {
        ChatToolbarCreateSingleComponent(
            onBackPressed = onBackPressed,
            onActionClick = { viewModel.onCreateGroupClick() },
            isActionEnabled = isActionEnabled
        )

        when (uiState) {
            is ChatCreateMultiUiState.Error -> Unit
            ChatCreateMultiUiState.Loading -> Unit
            is ChatCreateMultiUiState.Success -> {
                val state = successState ?: return
                SearchField(
                    title = stringResource(R.string.find_chat_message),
                    query = state.query,
                    onSearchChanged = {
                        viewModel.onCurrentSearchChange(it)
                    },
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    if (state.searchResults.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.search_country),
                                fontSize = 18.sp,
                                color = MaterialTheme.colors.onBackground,
                            )
                        }
                        items(state.searchResults) { user ->
                            CreateChatMemberCard(
                                name = user.fullName.orEmpty(),
                                avatarUrl = user.image?.path.orEmpty(),
                                time = "",
                                onMemberClick = {
                                    user.id?.let { viewModel.onUserClick(it) }
                                },
                                showCheckbox = true,
                                checkboxOnLeft = true,
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.subs),
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.onBackground,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                    items(state.users) { user ->
                        CreateChatMemberCard(
                            name = user.fullName.orEmpty(),
                            avatarUrl = user.image?.path.orEmpty(),
                            time = "",
                            onMemberClick = {
                                user.id?.let { viewModel.onUserClick(it) }
                            },
                            showCheckbox = true,
                            checkboxOnLeft = true,
                        )
                    }
                }
            }
        }
    }
}

