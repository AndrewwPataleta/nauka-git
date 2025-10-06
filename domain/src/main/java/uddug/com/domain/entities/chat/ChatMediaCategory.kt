package uddug.com.domain.entities.chat

enum class ChatMediaCategory(val apiValue: Int) {
    MEDIA(1),
    FILES(3),
    VOICE_MESSAGES(6),
    CALL_RECORDINGS(7),
}
