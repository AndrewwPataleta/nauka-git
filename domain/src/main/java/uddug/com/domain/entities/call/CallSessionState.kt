package uddug.com.domain.entities.call

data class CallSessionState(
    val micOn: Boolean = false,
    val camOn: Boolean = false,
    val handUp: Boolean = false,
    val sharingScreen: Boolean = false,
)
