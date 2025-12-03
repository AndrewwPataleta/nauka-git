package uddug.com.data.services.models.request.call

import uddug.com.data.services.models.response.call.CallSessionStateDto

data class UpdateCallStateRequestDto(
    val user: String,
    val mediaSessionId: String,
    val state: CallSessionStateDto,
)
