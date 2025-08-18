package uddug.com.naukoteka.ui.chat.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import uddug.com.naukoteka.R


@Composable
fun ChatInputBar(
    modifier: Modifier = Modifier,
    currentMessage: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAttachClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFEAEAF2))
                .height(1.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAttachClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_attach),
                    contentDescription = "Attach",
                    tint = Color(0XFF8083A0)
                )
            }

            TextField(
                value = currentMessage,
                onValueChange = onMessageChange,
                placeholder = { Text("Напишите сообщение") },
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .weight(1f)
                    .height(54.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFEAEAF2),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(onClick = onSendClick) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0XFF8083A0))
            }
        }
    }
}