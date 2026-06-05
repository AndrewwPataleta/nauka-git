package uddug.com.naukoteka.flashphoner

import android.util.Log
import com.flashphoner.fpwcsapi.Flashphoner
import com.flashphoner.fpwcsapi.bean.Connection
import com.flashphoner.fpwcsapi.room.Room
import com.flashphoner.fpwcsapi.room.RoomEvent
import com.flashphoner.fpwcsapi.room.RoomManager
import com.flashphoner.fpwcsapi.room.RoomManagerEvent
import com.flashphoner.fpwcsapi.room.RoomManagerOptions
import com.flashphoner.fpwcsapi.room.RoomOptions
import com.flashphoner.fpwcsapi.session.RestAppCommunicator
import com.flashphoner.fpwcsapi.session.Stream
import com.flashphoner.fpwcsapi.session.StreamOptions
import org.webrtc.SurfaceViewRenderer
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashphonerSessionManager @Inject constructor(
    private val environment: FlashphonerEnvironment
) {

    private val roomManagerRef = AtomicReference<RoomManager?>()
    private val roomRef = AtomicReference<Room?>()
    private val streamRef = AtomicReference<Stream?>()

    private var currentServerUrl: String? = null
    private var currentLogin: String? = null
    private var currentRoomName: String? = null
    private var currentStreamName: String? = null

    fun prepareRoomManager(
        serverUrl: String,
        username: String,
        configureOptions: RoomManagerOptions.() -> Unit = {},
        onManagerReady: RoomManager.() -> Unit = {},
    ): RoomManager {
        reset()
        environment.ensureInitialised()

        require(serverUrl.isNotBlank()) { "serverUrl is blank" }
        require(username.isNotBlank()) { "username is blank" }

        currentServerUrl = serverUrl
        currentLogin = username

        val options = RoomManagerOptions(serverUrl, username).apply(configureOptions)

        log(
            "prepareRoomManager " +
                    "serverUrl=$serverUrl " +
                    "login=$username " +
                    "room=${currentRoomName ?: "n/a"} " +
                    "stream=${currentStreamName ?: "n/a"} " +
                    "authToken=n/a " +
                    "customPayload=n/a " +
                    "note=RoomManagerOptions participant name is used as identity"
        )

        val manager = Flashphoner.createRoomManager(options)
        onManagerReady(manager)
        roomManagerRef.set(manager)
        return manager
    }

    fun connectRoomManager(event: RoomManagerEvent) {
        val manager = roomManagerRef.get()
            ?: error("RoomManager must be prepared before connectRoomManager()")

        manager.on(object : RoomManagerEvent {
            override fun onConnected(connection: Connection) {
                logConnectionDiagnostics("room_manager_connected", connection)
                event.onConnected(connection)
            }

            override fun onDisconnection(connection: Connection) {
                logConnectionDiagnostics("room_manager_disconnected", connection)
                event.onDisconnection(connection)
            }
        })
    }

    fun joinRoom(
        roomName: String,
        roomEvent: ((Room) -> Unit)? = null,
        onRoomReady: Room.() -> Unit = {},
    ): Room {
        val manager = roomManagerRef.get()
            ?: error("RoomManager must be prepared before joining a room")

        require(roomName.isNotBlank()) { "roomName is blank" }

        currentRoomName = roomName

        val options = RoomOptions().apply {
            name = roomName
        }

        log("joinRoom roomName=$roomName login=${currentLogin ?: "n/a"}")

        val room = manager.join(options)
        roomRef.set(room)

        roomEvent?.invoke(room)
        onRoomReady(room)

        return room
    }

    fun isRoomJoined(): Boolean = roomRef.get() != null

    fun publishToCurrentRoom(
        streamName: String,
        renderer: SurfaceViewRenderer? = null,
        configure: StreamOptions.() -> Unit = {},
    ): Stream {
        val room = roomRef.get() ?: error("Room must be joined before publishing")
        require(streamName.isNotBlank()) { "streamName is blank" }

        currentStreamName = streamName

        val options = StreamOptions(streamName).apply(configure)
        options.renderer = renderer

        log(
            "publishToCurrentRoom " +
                    "room=${room.name} " +
                    "streamName=$streamName " +
                    "login=${currentLogin ?: "n/a"}"
        )

        val stream = room.publish(renderer, options)
        streamRef.set(stream)

        log(
            "publishToCurrentRoom_created " +
                    "room=${room.name} " +
                    "requestedStream=$streamName " +
                    "actualStream=${stream.name ?: "n/a"} " +
                    "mediaSessionId=${stream.id ?: "n/a"}"
        )

        return stream
    }

    fun unpublishCurrentStream() {
        val stream = streamRef.getAndSet(null) ?: return
        val room = roomRef.get()

        runCatching {
            room?.unpublish()
        }.onFailure {
            log("unpublishCurrentStream room.unpublish failed error=${it.message}")
        }

        runCatching {
            stream.stop()
        }.onFailure {
            log("unpublishCurrentStream stream.stop failed error=${it.message}")
        }
    }

    fun leaveRoom() {
        roomRef.getAndSet(null)?.leave(defaultHandler)
        currentRoomName = null
    }

    fun disconnectRoom() {
        log("disconnectRoom")
        unpublishCurrentStream()
        leaveRoom()
        roomManagerRef.getAndSet(null)?.disconnect()
        currentServerUrl = null
        currentLogin = null
        currentStreamName = null
    }

    fun reset() {
        log("reset")
        disconnectRoom()
    }

    private fun logConnectionDiagnostics(prefix: String, connection: Connection) {
        val authToken = runCatching {
            Connection::class.java.methods
                .firstOrNull { it.name == "getAuthToken" && it.parameterCount == 0 }
                ?.invoke(connection) as? String
        }.getOrNull()

        val custom = runCatching {
            Connection::class.java.methods
                .firstOrNull { it.name.equals("getCustom", ignoreCase = true) && it.parameterCount == 0 }
                ?.invoke(connection)
        }.getOrNull()

        log(
            "$prefix " +
                    "status=${connection.status} " +
                    "login=${currentLogin ?: "n/a"} " +
                    "room=${currentRoomName ?: "n/a"} " +
                    "stream=${currentStreamName ?: "n/a"} " +
                    "authToken=${authToken ?: "n/a"} " +
                    "custom=${custom ?: "n/a"}"
        )
    }

    private fun log(message: String) {
        Log.d(LOG_TAG, message)
    }

    private val defaultHandler = object : RestAppCommunicator.Handler {
        override fun onAccepted(data: com.flashphoner.fpwcsapi.bean.Data) = Unit
        override fun onRejected(data: com.flashphoner.fpwcsapi.bean.Data) = Unit
    }

    private companion object {
        const val LOG_TAG = "FlashphonerSM"
    }
}
