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
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.theme.NauTheme

@Composable
fun SearchField(
    title: String,
    query: String,
    onSearchChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
    placeholderCentered: Boolean = false,
    showClearIcon: Boolean = false,
    onClearClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = NauTheme.extendedColors.inputBackground
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )

        BasicTextField(
            value = query,
            onValueChange = { onSearchChanged(it) },
            textStyle = LocalTextStyle.current.copy(color = MaterialTheme.colors.onBackground),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { onFocusChanged(it.isFocused) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (placeholderCentered) Alignment.Center else Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = title,
                            color = NauTheme.extendedColors.inactive,
                            textAlign = if (placeholderCentered) TextAlign.Center else TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        innerTextField()
                    }
                }
            }
        )

        if (showClearIcon) {
            IconButton(
                onClick = onClearClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
