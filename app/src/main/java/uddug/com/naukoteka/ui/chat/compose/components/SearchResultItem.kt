package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.mvvm.chat.SearchResult

@Composable
fun SearchResultItem(
    result: SearchResult,
    onClick: (Long) -> Unit,
) {
    val name = when (result) {
        is SearchResult.Dialog -> result.data.fullName
        is SearchResult.Message -> result.data.fullName
    }
    val message = when (result) {
        is SearchResult.Dialog -> ""
        is SearchResult.Message -> result.data.text ?: ""
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(result.dialogId) }
            .padding(16.dp)
    ) {
        Text(text = name)
        if (message.isNotEmpty()) {
            Text(text = message, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
