package uddug.com.naukoteka.ui.chat.di

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject
import uddug.com.data.cache.cookies.CookiesCache

class SocketServiceImpl(private val cookiesCache: CookiesCache) : SocketService {

    companion object {
        private const val TAG = "SocketService"
        private const val MAX_RECONNECTION_ATTEMPTS = 5
        private const val RECONNECTION_DELAY_MS = 1000

        // Строковые константы для событий, так как их нет в версии 2.1.2
        private const val EVENT_RECONNECT_ATTEMPT = "reconnect_attempt"
        private const val EVENT_RECONNECT_ERROR = "reconnect_error"
        private const val EVENT_RECONNECT_FAILED = "reconnect_failed"
    }

    private val gson = Gson()
    private var isConnected = false

    private val socket: Socket by lazy {
        val authToken = cookiesCache.getAuthCookies().takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Auth token is empty")

        IO.socket("https://stage.naukotheka.ru", IO.Options().apply {
            path = "/api/chat/socket.io"

            transports = arrayOf(WebSocket.NAME, "polling")
            extraHeaders = mapOf(
                "Authorization" to listOf("${authToken.replace("\"", "")}"),
                "Origin" to listOf("https://stage.naukotheka.ru")
            )
            reconnection = true
            reconnectionAttempts = MAX_RECONNECTION_ATTEMPTS
            reconnectionDelay = RECONNECTION_DELAY_MS.toLong()
        }).also {
            setupEventListeners(it)
            logConnectionParameters(authToken)
        }
    }

    override fun connect() {
        if (isConnected) {
            Log.w(TAG, "Already connected, skipping connect request")
            return
        }

        Log.d(TAG, "Initiating connection...")
        try {
            socket.connect()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate connection", e)
            throw SocketConnectionException("Connection failed", e)
        }
    }

    override fun disconnect() {
        if (!isConnected) {
            Log.w(TAG, "Not connected, skipping disconnect request")
            return
        }

        Log.d(TAG, "Disconnecting...")
        socket.disconnect()
        isConnected = false
    }

    override fun sendMessage(event: String, data: Any) {
        if (!isConnected) {
            Log.w(TAG, "Cannot send message - not connected")
            throw IllegalStateException("Socket is not connected")
        }

        try {
            val json = gson.toJson(data)
            Log.d(TAG, "Emitting event: '$event' with payload: $json")

            socket.emit(event, JSONObject(gson.toJson(data)))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message for event: $event", e)
            throw SocketMessageException("Failed to send message", e)
        }
    }

    override fun setOnEvent(event: String, callback: (data: String) -> Unit) {
        socket.on(event) { args ->
            try {
                val data = args.getOrNull(0)?.toString() ?: run {
                    Log.w(TAG, "Received empty data for event: $event")
                    return@on
                }

                Log.d(TAG, "Received event: '$event' with payload: $data")
                callback(data)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing event: $event", e)
            }
        }
    }

    private fun setupEventListeners(socket: Socket) {
        socket.apply {
            on(Socket.EVENT_CONNECT) {
                isConnected = true
                Log.i(TAG, "✅ Socket connected successfully")
                logSocketDetails()
            }

            on(Socket.EVENT_DISCONNECT) {
                isConnected = false
                Log.w(TAG, "❌ Socket disconnected. Reason: ${it.joinToString()}")
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.getOrNull(0)?.toString() ?: "unknown error"
                Log.e(TAG, "🔥 Connection error: $error")
            }

            // Используем строковые константы вместо отсутствующих в этой версии
            on(EVENT_RECONNECT_ATTEMPT) {
                Log.d(TAG, "🔄 Reconnection attempt #${socket.io().reconnectionAttempts()}")
            }

            on(EVENT_RECONNECT_ERROR) { args ->
                val error = args.getOrNull(0)?.toString() ?: "unknown error"
                Log.e(TAG, "🔥 Reconnection error: $error")
            }

            on(EVENT_RECONNECT_FAILED) {
                Log.e(TAG, "💥 All reconnection attempts failed")
            }
        }
    }

    private fun logConnectionParameters(authToken: String) {
        Log.d(TAG, """
            |Connection parameters:
            |🔗 URL: https://stage.naukotheka.ru
            |🛣️ Path: /api/chat/socket.io
            |🔐 Auth token: ${authToken}
            |♻️ Reconnection attempts: $MAX_RECONNECTION_ATTEMPTS
            |⏱️ Reconnection delay: ${RECONNECTION_DELAY_MS}ms
            """.trimMargin())
    }

    private fun logSocketDetails() {
        try {
            val io = socket.io()
            Log.d(TAG, """
                |Socket details:
              
                """.trimMargin())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log socket details", e)
        }
    }
}

class SocketConnectionException(message: String, cause: Throwable? = null) : Exception(message, cause)
class SocketMessageException(message: String, cause: Throwable? = null) : Exception(message, cause)