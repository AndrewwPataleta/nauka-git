import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    // Определяем форматирование даты
  //  val formattedTime = formatMessageTime(time)
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
            },  // Убираем отступы
        colors = CardDefaults.cardColors(containerColor = Color.White)  // Белый фон
    ) {
        Column {
              Row(
                  modifier = Modifier
                      .fillMaxWidth()
                      .padding(16.dp),
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
                          colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E83D9))
                      )
                      Spacer(modifier = Modifier.width(16.dp))
                  }
                  Avatar(avatarUrl.takeIf { it.isNotEmpty() }, name, size = 40.dp)
                  Spacer(modifier = Modifier.width(16.dp))

                // Основной контент
                Column(modifier = Modifier.weight(1f)) {
                    // Имя
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (name.isEmpty()) {
                                stringResource(R.string.group_chat)
                            } else {
                                name
                            }, style = TextStyle(fontSize = 16.sp, color = Color.Black)
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
                          colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E83D9))
                      )
                  }
            }
//            Text(
//                text = formattedTime,
//                style = TextStyle(fontSize = 12.sp, color = Color.Gray),
//                modifier = Modifier
//            )
        }
    }
}

