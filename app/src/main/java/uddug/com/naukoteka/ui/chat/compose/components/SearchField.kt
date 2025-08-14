package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import java.lang.reflect.Modifier

@Composable
fun SearchField(
     title: String,
     query: String,
     onSearchChanged: (String) -> Unit
) {
    var searchText = TextFieldValue(title)

    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFEAEAF2)
            )
            .padding(start = 16.dp)
    ) {
        // Иконка слева
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = Color.Gray,
            modifier = androidx.compose.ui.Modifier
                .size(18.dp)
                .align(Alignment.CenterVertically)
        )

        // BasicTextField
        BasicTextField(
            value = query,
            onValueChange = { onSearchChanged(it) },
            textStyle = LocalTextStyle.current.copy(color = Color.Gray),
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEAEAF2),
                )

        )
    }
}
