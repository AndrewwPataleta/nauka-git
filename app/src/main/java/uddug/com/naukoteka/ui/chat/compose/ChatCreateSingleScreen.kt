package uddug.com.naukoteka.ui.chat.compose


import CreateChatMemberCard
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ChatCreateSingleUiState
import uddug.com.naukoteka.mvvm.chat.ChatCreateSingleViewModel
import uddug.com.naukoteka.ui.chat.compose.components.ChatToolbarCreateSingleComponent
import uddug.com.naukoteka.ui.chat.compose.components.SearchField


@Composable
fun ChatCreateSingleScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatCreateSingleViewModel,
    onGroupCreateClick: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
    ) {
        ChatToolbarCreateSingleComponent(
            onBackPressed = onBackPressed
        )

        when (uiState) {
            is ChatCreateSingleUiState.Error -> Unit
            ChatCreateSingleUiState.Loading -> Unit
            is ChatCreateSingleUiState.Success -> {
                val state = uiState as ChatCreateSingleUiState.Success
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
                                color = Color.Black,
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
                                showCheckbox = false,
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .clickable (
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onGroupCreateClick() },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add_users_chat),
                                contentDescription = "Edit Icon",
                            )
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = stringResource(R.string.create_group_chat),
                                fontSize = 18.sp,
                                color = Color.Black,
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.subs),
                            fontSize = 18.sp,
                            color = Color.Black,
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
                            showCheckbox = false,
                        )
                    }
                }
            }
        }


    }
}
