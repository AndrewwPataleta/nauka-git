package uddug.com.naukoteka.ui.call.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uddug.com.naukoteka.R

/**
 * Экран настройки записи звонка. Открывается по кнопке записи в шапке звонка
 * (когда запись ещё не идёт): пользователь задаёт название файла и подтверждает
 * старт. Сама запись запускается уже после нажатия «Начать запись звонка».
 *
 * Поведение экрана и палитра повторяют [CallScreen]/ParticipantsScreen, чтобы
 * запись открывалась как ещё один state-overlay поверх экрана звонка.
 *
 * @param defaultFileName подставляется как подсказка в поле названия и
 *        используется, если пользователь оставил поле пустым.
 * @param onBack закрыть экран без старта записи.
 * @param onStartRecording старт записи с итоговым названием файла.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallRecordingSetupScreen(
    defaultFileName: String,
    onBack: () -> Unit,
    onStartRecording: (String) -> Unit,
) {
    val backgroundColor = Color(0xFF0B1020)
    val accentColor = Color(0xFF2E83D9)
    var fileName by rememberSaveable { mutableStateOf("") }

    BackHandler(onBack = onBack)

    Surface(modifier = Modifier.fillMaxSize(), color = backgroundColor) {
        Scaffold(
            containerColor = backgroundColor,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
                        navigationIconContentColor = accentColor,
                        titleContentColor = Color.White,
                    ),
                    title = {
                        Text(
                            text = stringResource(R.string.call_record_setup_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = null,
                                tint = accentColor,
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text(
                    text = stringResource(R.string.call_record_setup_description),
                    color = Color(0xFFB0B3C5),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.call_record_file_name_label),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    placeholder = {
                        Text(
                            text = defaultFileName,
                            color = Color(0xFF8083A0),
                            fontSize = 16.sp,
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF121732),
                        unfocusedContainerColor = Color(0xFF121732),
                        disabledContainerColor = Color(0xFF121732),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                val warningText = buildAnnotatedString {
                    append(stringResource(R.string.call_record_warning))
                    append(" ")
                    withStyle(SpanStyle(color = Color(0xFF4DA6FF))) {
                        append(stringResource(R.string.call_record_warning_link))
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF1D2239),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = warningText,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    color = accentColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        onStartRecording(fileName.trim().ifBlank { defaultFileName })
                    },
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.call_record_start_action),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
