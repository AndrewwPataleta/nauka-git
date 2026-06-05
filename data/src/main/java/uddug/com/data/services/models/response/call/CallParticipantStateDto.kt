package uddug.com.data.services.models.response.call

data class CallParticipantStateDto(
    val id: Long,
    val call: Long,
    val user: String,
    val mediaSessionId: String,
    // Бэк отдаёт state = null, пока участник не передал медиа-состояние
    // (см. ответ GET calls/dialog/:id/participants). Тип обязан быть nullable —
    // иначе маппер падает с NPE и весь список участников не загружается.
    val state: CallSessionStateDto?,
)
