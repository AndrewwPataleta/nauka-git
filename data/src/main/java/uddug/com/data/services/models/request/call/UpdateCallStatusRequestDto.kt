package uddug.com.data.services.models.request.call

data class UpdateCallStatusRequestDto(
    val user: String,
    val status: Int,
)
