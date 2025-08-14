package uddug.com.naukoteka.ui.chat.di

interface SocketService {
    fun connect()
    fun disconnect()
    fun sendMessage(event: String, data: Any)
    fun setOnEvent(event: String, callback: (data: String) -> Unit)
}
