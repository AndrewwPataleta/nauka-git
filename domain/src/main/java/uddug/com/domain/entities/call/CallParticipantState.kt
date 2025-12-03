package uddug.com.domain.entities.call

data class CallParticipantState(
    val id: Long,
    val callId: Long,
    val userId: String,
    val mediaSessionId: String,
    val state: CallSessionState,
)
