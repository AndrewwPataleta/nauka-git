package uddug.com.data.services.models.response.call

data class CallParticipantDto(
    val id: Long,
    val call: Long,
    val user: String,
    val status: Int,
    val states: List<CallParticipantStateDto> = emptyList(),
    val roles: List<String> = emptyList(),
    val permits: List<String> = emptyList(),
)
