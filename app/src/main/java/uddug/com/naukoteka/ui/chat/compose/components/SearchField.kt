package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R

@Composable
fun SearchField(
    title: String,
    query: String,
    onSearchChanged: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = Color(0xFFEAEAF2),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            if (query.isEmpty()) {
                Text(text = title, color = Color.Gray)
            }
            BasicTextField(
                value = query,
                onValueChange = onSearchChanged,
                textStyle = LocalTextStyle.current.copy(color = Color.Black),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
