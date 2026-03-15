package uddug.com.naukoteka.flashphoner

import android.os.Build
import android.util.Log
import com.flashphoner.fpwcsapi.bean.Data
import com.flashphoner.fpwcsapi.Flashphoner
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
import org.webrtc.SurfaceViewRenderer
import uddug.com.naukoteka.BuildConfig
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

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
       // reset()
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
        configureOptions: RoomManagerOptions.() -> Unit = {},
        onManagerReady: RoomManager.() -> Unit = {},
    ): RoomManager {
        //reset()
        environment.ensureInitialised()

        Log.d(
            LOG_TAG,
            "prepareRoomManager serverUrl=$serverUrl username=$username custom={login=$username} customLoginPresent=${username.isNotBlank()}"
        )
        logRoomManagerSdkCapabilities()
        val options = RoomManagerOptions(serverUrl, username).apply(configureOptions)
        applyConnectionMetadata(options, username)
        val manager = Flashphoner.createRoomManager(options)

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
        roomRef.set(room)
        roomEvent?.invoke(room)
        onRoomReady(room)
        return room
    }

    fun publishToCurrentRoom(
        streamName: String,
        renderer: SurfaceViewRenderer? = null,
        configure: StreamOptions.() -> Unit = {},
    ): Stream {
        val room = roomRef.get() ?: error("Room must be joined before publishing a stream")
        val options = StreamOptions(streamName).apply(configure)
        options.renderer = renderer
        options.setCustom("name", room.name)
        val actualStreamNameBeforePublish = options.name
        Log.d(
            LOG_TAG,
            "publishToCurrentRoom requestedStreamName=$streamName actualStreamNameBeforePublish=$actualStreamNameBeforePublish room=${room.name} publishMode=room.publish"
        )

        val stream = room.publish(renderer, options)
        val publishedName = stream.name ?: "n/a"
        val mediaSessionId = stream.id ?: "n/a"
        Log.d(
            LOG_TAG,
            "publishToCurrentRoom created room=${room.name} requestedStreamName=$streamName actualStreamName=$publishedName actualMediaSessionId=$mediaSessionId"
        )
        streamRef.set(stream)
        return stream
    }

    fun leaveRoom() {
        roomRef.getAndSet(null)?.leave(defaultHandler)
    }

    fun stopStream() {
        streamRef.getAndSet(null)?.stop()
    }

    fun unpublishCurrentStream() {
        val stream = streamRef.getAndSet(null) ?: return
        val room = roomRef.get()
        if (room != null) room.unpublish()
        stream.stop()
    }

    fun disconnectSession() {
        logLifecycleCall("disconnectSession")
        val session = sessionRef.getAndSet(null) ?: return
        session.disconnect()
    }

    fun disconnectRoom() {
        logLifecycleCall("disconnectRoom")
        unpublishCurrentStream()
        leaveRoom()
        roomManagerRef.getAndSet(null)?.disconnect()
        disconnectSession()
    }

    fun reset() {
        logLifecycleCall("reset")
        stopStream()
        leaveRoom()
        roomManagerRef.getAndSet(null)?.disconnect()
        sessionRef.getAndSet(null)?.disconnect()
    }

    private fun applyConnectionMetadata(options: RoomManagerOptions, username: String) {
        val clientInfo = mapOf(
            "mobile" to true,
            "platform" to "Android ${Build.VERSION.RELEASE}",
            "brands" to listOf(
                mapOf("brand" to "NauchatAndroid", "version" to BuildConfig.VERSION_NAME)
            ),
            "fullVersionList" to listOf(
                mapOf("brand" to "NauchatAndroid", "version" to BuildConfig.VERSION_NAME)
            )
        )
        val customPayload = mapOf(
            "login" to username,
            "clientInfo" to clientInfo,
        )

        val methods = RoomManagerOptions::class.java.methods.associateBy { it.name }
        val customApplied = runCatching {
            when {
                methods.containsKey("setCustom") -> {
                    val method = methods.getValue("setCustom")
                    val params = method.parameterTypes
                    when {
                        params.size == 1 -> method.invoke(options, customPayload)
                        params.size == 2 && params[0] == String::class.java -> {
                            method.invoke(options, "login", username)
                            method.invoke(options, "clientInfo", clientInfo)
                        }
                    }
                    true
                }
                else -> false
            }
        }.getOrDefault(false)

        val clientVersionApplied = runCatching {
            methods["setClientVersion"]?.invoke(options, BuildConfig.VERSION_NAME)
            methods["setClientOSVersion"]?.invoke(options, "Android ${Build.VERSION.RELEASE}")
            methods["setClientBrowserVersion"]?.invoke(options, "AndroidSdk/1.1")
            true
        }.getOrDefault(false)

        Log.d(
            LOG_TAG,
            "room_manager_metadata_apply customApplied=$customApplied clientVersionApplied=$clientVersionApplied requestedCustomKeys=${customPayload.keys}"
        )
    }

    private fun logLifecycleCall(methodName: String) {
        Log.d(LOG_TAG, "$methodName() called", Throwable())
    }

    private fun logRoomManagerSdkCapabilities() {
        val optionMethods = RoomManagerOptions::class.java.methods
            .map { it.name }
            .distinct()
            .sorted()

        val supportsOrigin = optionMethods.any { it.equals("setOrigin", ignoreCase = true) }
        val supportsClientVersion = optionMethods.any { it.equals("setClientVersion", ignoreCase = true) }
        val supportsClientInfo = optionMethods.any { it.contains("ClientInfo", ignoreCase = true) }
        val supportsMediaProviders = optionMethods.any { it.contains("MediaProvider", ignoreCase = true) }

        Log.d(
            LOG_TAG,
            "room_manager_sdk_capabilities sdkAar=1.1.0.64 supportsOrigin=$supportsOrigin supportsClientVersion=$supportsClientVersion supportsClientInfo=$supportsClientInfo supportsMediaProviders=$supportsMediaProviders availableMethods=$optionMethods"
        )
    }

    private val defaultHandler = object : RestAppCommunicator.Handler {
        override fun onAccepted(data: Data) = Unit

        override fun onRejected(data: Data) = Unit
    }

    private companion object {
        const val LOG_TAG = "FlashphonerSM"
    }
}
