package uddug.com.data.services.models.request.call

/**
 * Тело запроса на старт записи звонка
 * (PUT chat/v1/calls/record/start/dialog/{dialogId}).
 *
 * @property name название файла записи, заданное пользователем на экране
 *           настройки записи.
 */
data class StartRecordingRequestDto(
    val name: String,
)
