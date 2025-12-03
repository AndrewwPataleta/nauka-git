package uddug.com.naukoteka.socket

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import uddug.com.naukoteka.ui.chat.di.SocketService

@Singleton
class SocketEnvironment @Inject constructor(
    private val socketService: SocketService,
) {

    fun ensureConnected() {
        try {
            socketService.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to establish socket connection", e)
        }
    }

    fun disconnect() {
        try {
            socketService.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect socket", e)
        }
    }

    fun registerListener(event: String, callback: (data: String) -> Unit) {
        socketService.setOnEvent(event, callback)
    }

    companion object {
        private const val TAG = "SocketEnvironment"
    }
}
