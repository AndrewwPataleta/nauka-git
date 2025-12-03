package uddug.com.data.services.models.response.call

data class CallSessionStateDto(
    val micOn: Boolean = false,
    val camOn: Boolean = false,
    val handUp: Boolean = false,
    val sharingScreen: Boolean = false,
)
