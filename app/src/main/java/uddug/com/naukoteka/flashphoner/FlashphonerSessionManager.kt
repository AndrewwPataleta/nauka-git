package uddug.com.naukoteka.flashphoner

import com.flashphoner.fpwcsapi.bean.Data
import com.flashphoner.fpwcsapi.room.Room
import com.flashphoner.fpwcsapi.room.RoomManager
import com.flashphoner.fpwcsapi.room.RoomManagerOptions
import com.flashphoner.fpwcsapi.room.RoomManagerEvent
import com.flashphoner.fpwcsapi.room.RoomOptions
import com.flashphoner.fpwcsapi.session.RestAppCommunicator
import com.flashphoner.fpwcsapi.session.Session
import com.flashphoner.fpwcsapi.session.Stream
import com.flashphoner.fpwcsapi.session.StreamOptions
import com.flashphoner.fpwcsapi.session.SessionOptions
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper responsible for holding references to the current Flashphoner [Session] and
 * [Stream]. The class centralises session lifecycle handling so that future features can
 * focus on business logic instead of repetitive boilerplate.
 */
@Singleton
class FlashphonerSessionManager @Inject constructor(
    private val environment: FlashphonerEnvironment
) {

    private val sessionRef = AtomicReference<Session?>()
    private val streamRef = AtomicReference<Stream?>()
    private val roomManagerRef = AtomicReference<RoomManager?>()
    private val roomRef = AtomicReference<Room?>()

    fun prepareSession(
        serverUrl: String,
        configureOptions: SessionOptions.() -> Unit = {},
        onSessionReady: Session.() -> Unit = {}
    ): Session {
        reset()
        val session = environment.createSession(serverUrl) {

            configureOptions()
        }
        session.onSessionReady()
        sessionRef.set(session)
        return session
    }

    fun createStream(
        streamName: String,
        configure: StreamOptions.() -> Unit = {},
    ): Stream {
        val session = sessionRef.get()
            ?: error("Flashphoner session must be prepared before creating streams")
        val options = StreamOptions(streamName).apply(configure)
        val stream = session.createStream(options)
        streamRef.set(stream)
        return stream
    }

    fun prepareRoomManager(
        serverUrl: String,
        username: String,
        configureOptions: SessionOptions.() -> Unit = {},
        onManagerReady: RoomManager.() -> Unit = {},
    ): RoomManager {
        reset()
        environment.ensureInitialised()
        val options = RoomManagerOptions(serverUrl, username).apply {
            configureOptions()
        }
        val manager = RoomManager(options)
        onManagerReady(manager)
        roomManagerRef.set(manager)

        return manager
    }

    fun connectRoomManager(event: RoomManagerEvent) {
        val manager = roomManagerRef.get()
            ?: error("Flashphoner room manager must be prepared before connecting")

        manager.on(event)

    }

    fun joinRoom(
        roomName: String,
        roomEvent: ((Room) -> Unit)? = null,
        onRoomReady: Room.() -> Unit = {},
    ): Room {
        val manager = roomManagerRef.get()
            ?: error("Flashphoner room manager must be prepared before joining rooms")
        val options = RoomOptions().apply { name = roomName }
        val room = manager.join(options)
        roomEvent?.invoke(room)
        onRoomReady(room)
        roomRef.set(room)
        return room
    }

    fun publishToCurrentRoom(
        streamName: String,
        configure: StreamOptions.() -> Unit = {},
    ): Stream {
        val room = roomRef.get() ?: error("Room must be joined before publishing a stream")
        val options = StreamOptions(streamName).apply(configure)
        val stream = room.publish(null, options)
        streamRef.set(stream)
        return stream
    }

    fun leaveRoom() {
        roomRef.getAndSet(null)?.leave(defaultHandler)
    }

    fun stopStream() {
        streamRef.getAndSet(null)?.stop()
    }

    /**
     * Acceptable way to leave a call: stop publishing media for the current room.
     * If the room is still attached, request unpublish so the backend cleans up
     * the media session; otherwise, fall back to stopping the stream directly.
     */
    fun unpublishCurrentStream() {
        val stream = streamRef.getAndSet(null) ?: return
        val room = roomRef.get()
        if (room != null) {
            room.unpublish()
        } else {
            stream.stop()
        }
    }

    fun disconnectSession() {
        val session = sessionRef.getAndSet(null) ?: return
        session.disconnect()
    }

    /**
     * Proper way to leave a call: unpublish active media and leave the room so
     * the backend receives the appropriate callbacks before the session closes.
     */
    fun disconnectRoom() {
        unpublishCurrentStream()
        leaveRoom()
        roomManagerRef.getAndSet(null)?.disconnect()
        disconnectSession()
    }

    /**
     * Fully clears any existing session, stream or room manager state to ensure a fresh
     * connection can be established. This is intentionally idempotent and safe to call
     * before creating new Flashphoner objects.
     */
    fun reset() {
        stopStream()
        leaveRoom()
        roomManagerRef.getAndSet(null)?.disconnect()
        sessionRef.getAndSet(null)?.disconnect()
    }

    private val defaultHandler = object : RestAppCommunicator.Handler {
        override fun onAccepted(data: Data) = Unit

        override fun onRejected(data: Data) = Unit
    }
}
