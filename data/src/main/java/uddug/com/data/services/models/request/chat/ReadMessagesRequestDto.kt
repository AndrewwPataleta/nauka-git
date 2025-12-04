package uddug.com.data.services.models.request.chat

data class ReadMessagesRequestDto(
    val dialogId: Long,
    val messages: List<Long>,
    val readStatus: Int,
)
