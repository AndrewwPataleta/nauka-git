package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R

@Composable
fun SearchField(
    title: String,
    query: String,
    onSearchChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEAEAF2)
            )
            .padding(start = 16.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = Color.Gray,
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.CenterVertically)
        )

        BasicTextField(
            value = query,
            onValueChange = { onSearchChanged(it) },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .padding(10.dp),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(text = title, color = Color.Gray)
                }
                innerTextField()
            }
        )
    }
}
