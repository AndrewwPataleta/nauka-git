package uddug.com.data.services.models.response.call

data class CallParticipantDto(
    val id: Long,
    val call: Long,
    val user: String,
    val fullName: String? = null,
    val imageUrl: String? = null,
    val status: Int,
    // Gson НЕ применяет Kotlin-дефолты: если поле null/отсутствует в JSON, оно
    // приходит как null. Поэтому списки nullable, а подстановку emptyList и .map
    // делаем в маппере — иначе NPE (collectionSizeOrDefault) и весь список
    // участников теряется, из-за чего пропадают роли/админ/лобби.
    val states: List<CallParticipantStateDto>? = null,
    val roles: List<String>? = null,
    val permits: List<String>? = null,
)
