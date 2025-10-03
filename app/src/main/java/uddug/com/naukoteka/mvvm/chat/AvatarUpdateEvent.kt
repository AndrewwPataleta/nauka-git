package uddug.com.naukoteka.mvvm.chat

sealed class AvatarUpdateEvent {
    data object Success : AvatarUpdateEvent()
    data class Error(val message: String?) : AvatarUpdateEvent()
}
