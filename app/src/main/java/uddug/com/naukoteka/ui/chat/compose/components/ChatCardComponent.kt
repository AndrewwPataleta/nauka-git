package uddug.com.naukoteka.ui.chat.compose.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uddug.com.naukoteka.mvvm.chat.ChatListViewModel


@Composable
fun ChatCardComponent(
    modifier: Modifier = Modifier,
    viewModel: ChatListViewModel,
    onBackPressed: () -> Unit,
) {
    Column(
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

        }
    }
}
