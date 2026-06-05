package uddug.com.domain.entities.call

data class CallParticipant(
    val id: Long,
    val callId: Long,
    val userId: String,
    val fullName: String? = null,
    val imageUrl: String? = null,
    val status: Int,
    val states: List<CallParticipantState>,
    val roles: List<String>,
    val permits: List<String>,
)
