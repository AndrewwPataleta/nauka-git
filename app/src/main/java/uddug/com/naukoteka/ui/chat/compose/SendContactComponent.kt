package uddug.com.naukoteka.ui.chat.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.mvvm.chat.SendContactViewModel

@Composable
fun SendContactComponent(
    viewModel: SendContactViewModel,
    onBack: () -> Unit,
    onSelect: (UserProfileFullInfo) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringResource(R.string.send_contact_title)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = null
                    )
                }
            }
        )
        TextField(
            value = state.query,
            onValueChange = { viewModel.onQueryChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(text = stringResource(R.string.search_contacts_hint)) }
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(state.contacts) { user ->
                Text(
                    text = user.fullName ?: "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(user) }
                        .padding(16.dp)
                )
            }
        }
    }
}

