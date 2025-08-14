package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.domain.entities.chat.User

@Composable
fun UserSelectableItem(
    user: User,
    selected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!selected) }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onCheckedChange(it) }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Avatar(url = user.image)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = user.fullName ?: user.nickname.orEmpty(),
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}
