package uddug.com.naukoteka.ui.chat.di

interface SocketService {
    fun connect()
    fun disconnect()
    fun sendMessage(event: String, data: Any)
    fun setOnEvent(event: String, callback: (data: String) -> Unit)
    fun setOnEvent(event: String, tag: String, callback: (data: String) -> Unit)
    fun removeEvent(event: String)
    fun removeEvent(event: String, tag: String)
}
