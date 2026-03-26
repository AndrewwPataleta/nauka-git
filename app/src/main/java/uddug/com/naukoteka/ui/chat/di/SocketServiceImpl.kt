package uddug.com.naukoteka.ui.chat.di

import android.util.Log
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import uddug.com.data.cache.cookies.CookiesCache

@Singleton
class SocketServiceImpl @Inject constructor(private val cookiesCache: CookiesCache) : SocketService {

    companion object {
        private const val TAG = "SocketService"
        private const val DEFAULT_TAG = "__default__"
        private const val MAX_RECONNECTION_ATTEMPTS = 5
        private const val RECONNECTION_DELAY_MS = 1000

        private const val EVENT_RECONNECT_ATTEMPT = "reconnect_attempt"
        private const val EVENT_RECONNECT_ERROR = "reconnect_error"
        private const val EVENT_RECONNECT_FAILED = "reconnect_failed"
    }

    private val gson = Gson()
    private var isConnected = false

    // event -> (tag -> callback)
    private val eventListeners = mutableMapOf<String, MutableMap<String, (String) -> Unit>>()
    private val registeredSocketEvents = mutableSetOf<String>()

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
            logConnectionParameters()
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
        setOnEvent(event, DEFAULT_TAG, callback)
    }

    override fun setOnEvent(event: String, tag: String, callback: (data: String) -> Unit) {
        val listeners = eventListeners.getOrPut(event) { mutableMapOf() }
        listeners[tag] = callback
        Log.d(TAG, "Registered listener for event '$event' with tag '$tag' (total: ${listeners.size})")
        ensureSocketListener(event)
    }

    override fun removeEvent(event: String) {
        eventListeners.remove(event)
        registeredSocketEvents.remove(event)
        socket.off(event)
        Log.d(TAG, "Removed all listeners for event '$event'")
    }

    override fun removeEvent(event: String, tag: String) {
        val listeners = eventListeners[event] ?: return
        listeners.remove(tag)
        Log.d(TAG, "Removed listener for event '$event' tag '$tag' (remaining: ${listeners.size})")
        if (listeners.isEmpty()) {
            eventListeners.remove(event)
            registeredSocketEvents.remove(event)
            socket.off(event)
        }
    }

    private fun ensureSocketListener(event: String) {
        if (event in registeredSocketEvents) return
        registeredSocketEvents.add(event)

        socket.off(event)
        socket.on(event) { args ->
            try {
                val data = args.getOrNull(0)?.toString() ?: run {
                    Log.w(TAG, "Received empty data for event: $event")
                    return@on
                }

                Log.d(TAG, "Received event: '$event' with payload: $data")
                val listeners = eventListeners[event]?.values?.toList() ?: return@on
                listeners.forEach { callback ->
                    try {
                        callback(data)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in listener for event: $event", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error dispatching event: $event", e)
            }
        }
    }

    private fun setupEventListeners(socket: Socket) {
        socket.apply {
            on(Socket.EVENT_CONNECT) {
                isConnected = true
                Log.i(TAG, "Socket connected successfully")
                logSocketDetails()
            }

            on(Socket.EVENT_DISCONNECT) {
                isConnected = false
                Log.w(TAG, "Socket disconnected. Reason: ${it.joinToString()}")
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.getOrNull(0)?.toString() ?: "unknown error"
                Log.e(TAG, "Connection error: $error")
            }

            on(EVENT_RECONNECT_ATTEMPT) {
                Log.d(TAG, "Reconnection attempt #${socket.io().reconnectionAttempts()}")
            }

            on(EVENT_RECONNECT_ERROR) { args ->
                val error = args.getOrNull(0)?.toString() ?: "unknown error"
                Log.e(TAG, "Reconnection error: $error")
            }

            on(EVENT_RECONNECT_FAILED) {
                Log.e(TAG, "All reconnection attempts failed")
            }
        }
    }

    private fun logConnectionParameters() {
        Log.d(TAG, """
            |Connection parameters:
            |URL: https://stage.naukotheka.ru
            |Path: /api/chat/socket.io
            |Reconnection attempts: $MAX_RECONNECTION_ATTEMPTS
            |Reconnection delay: ${RECONNECTION_DELAY_MS}ms
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
