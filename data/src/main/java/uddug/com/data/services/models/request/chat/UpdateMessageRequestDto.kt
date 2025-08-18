package uddug.com.data.services.models.request.chat

data class UpdateMessageRequestDto(
    val messageId: Long,
    val text: String?,
)
