import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.chat.compose.components.Avatar

@Composable
fun CreateChatMemberCard(
    name: String,
    avatarUrl: String,
    time: String,
    onMemberClick: () -> Unit,
    showCheckbox: Boolean = true,
    checkboxOnLeft: Boolean = false,
) {


    var isChecked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (showCheckbox) {
                    isChecked = !isChecked
                }
                onMemberClick()
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colors.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showCheckbox && checkboxOnLeft) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            isChecked = it
                            onMemberClick()
                        },
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colors.primary,
                            uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            checkmarkColor = MaterialTheme.colors.onPrimary,
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Avatar(url = avatarUrl.takeIf { it.isNotEmpty() }, name = name, size = 40.dp)
                Spacer(modifier = Modifier.width(16.dp))


                Column(modifier = Modifier.weight(1f)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (name.isEmpty()) {
                                stringResource(R.string.group_chat)
                            } else {
                                name
                            }, style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colors.onSurface)
                        )
                    }

                }

                if (showCheckbox && !checkboxOnLeft) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            isChecked = it
                            onMemberClick()
                        },
                        modifier = Modifier.size(24.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colors.primary,
                            uncheckedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                            checkmarkColor = MaterialTheme.colors.onPrimary,
                        )
                    )
                }
            }


        }
    }
}

