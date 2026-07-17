package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.ForwardMessageItem
import uddug.com.naukoteka.mvvm.chat.ForwardMessageViewModel
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@Composable
fun ForwardMessageComponent(
    viewModel: ForwardMessageViewModel,
    forwardAuthor: String? = null,
    forwardText: String? = null,
    onBack: () -> Unit,
    onSelect: (ForwardMessageItem) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val scaffoldState = rememberScaffoldState()

    val dialogs = remember(state.query, state.dialogs) {
        val query = state.query.trim().lowercase()
        if (query.isEmpty()) {
            state.dialogs
        } else {
            state.dialogs.filter { item ->
                val title = item.title.lowercase()
                val subtitle = item.subtitle?.lowercase().orEmpty()
                title.contains(query) || subtitle.contains(query)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.forward_message_title),
                        color = MaterialTheme.colors.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_chat_back),
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
                elevation = 0.dp,
            )
        },
        backgroundColor = MaterialTheme.colors.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!forwardAuthor.isNullOrBlank() || !forwardText.isNullOrBlank()) {
                ForwardPreviewBlock(
                    author = forwardAuthor,
                    text = forwardText,
                )
            }

            TextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text(text = stringResource(R.string.search_dialogs_hint)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colorResource(R.color.main_background_input),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = colorResource(R.color.object_main),
                    textColor = MaterialTheme.colors.onBackground,
                    placeholderColor = Color(0xFF8F8FA0),
                    leadingIconColor = Color(0xFF8F8FA0),
                ),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(
                    items = dialogs,
                    key = { item -> item.dialogId }
                ) { item ->
                    ForwardMessageListItem(
                        item = item,
                        onClick = { onSelect(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ForwardPreviewBlock(
    author: String?,
    text: String?,
) {
    val accentColor = colorResource(id = R.color.object_main)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_forward),
            contentDescription = null,
            tint = accentColor
        )
        Image(
            painter = painterResource(id = R.drawable.ic_chat_separator),
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .width(2.dp)
                .height(24.dp),
            colorFilter = ColorFilter.tint(accentColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            if (!author.isNullOrBlank()) {
                Text(
                    text = author,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!text.isNullOrBlank()) {
                Text(
                    text = text,
                    color = Color(0xFF8083A0),
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
    Divider(
        color = colorResource(R.color.main_background_input_stroke),
        thickness = 1.dp,
    )
}

@Composable
private fun ForwardMessageListItem(
    item: ForwardMessageItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                url = item.avatarUrl,
                name = item.title,
                size = 48.dp,
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = MaterialTheme.colors.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                item.subtitle?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        color = Color(0xFF6F6F7B),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Divider(
            color = colorResource(R.color.main_background_input_stroke),
            thickness = 1.dp,
            modifier = Modifier.padding(start = 76.dp)
        )
    }
}
