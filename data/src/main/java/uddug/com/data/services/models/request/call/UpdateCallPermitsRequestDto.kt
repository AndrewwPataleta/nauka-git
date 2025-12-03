package uddug.com.data.services.models.request.call

data class UpdateCallPermitsRequestDto(
    val user: String,
    val role: String? = null,
    val addPermits: List<String>? = null,
    val delPermits: List<String>? = null,
)
