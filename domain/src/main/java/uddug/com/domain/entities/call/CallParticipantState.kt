package uddug.com.domain.entities.call

data class CallParticipantState(
    val id: Long,
    val callId: Long,
    val userId: String,
    val mediaSessionId: String,
    // null, пока участник не передал своё медиа-состояние на бэк.
    val state: CallSessionState?,
)
