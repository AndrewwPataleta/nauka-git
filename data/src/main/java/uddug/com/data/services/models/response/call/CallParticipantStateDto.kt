package uddug.com.data.services.models.response.call

data class CallParticipantStateDto(
    val id: Long,
    val call: Long,
    val user: String,
    val mediaSessionId: String,
    val state: CallSessionStateDto,
)
