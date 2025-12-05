package uddug.com.naukoteka.flashphoner

import android.app.Activity
import java.lang.ref.WeakReference
import com.flashphoner.fpwcsapi.Flashphoner
import com.flashphoner.fpwcsapi.session.Session
import com.flashphoner.fpwcsapi.session.SessionOptions
import com.flashphoner.fpwcsapi.room.RoomManager
import com.flashphoner.fpwcsapi.room.RoomManagerOptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashphonerEnvironment @Inject constructor() {

    private var isInitialised: Boolean = false
    private var containerActivityRef: WeakReference<Activity?> = WeakReference(null)

    fun attachContainerActivity(activity: Activity) {
        containerActivityRef = WeakReference(activity)
    }

    fun ensureInitialised(activity: Activity? = containerActivityRef.get()) {
        if (!isInitialised) {
            val hostActivity = activity ?: error("Container Activity must be attached before initializing Flashphoner")
            Flashphoner.init(hostActivity)
            isInitialised = true
        }
    }

    fun createSession(
        serverUrl: String,
        configure: SessionOptions.() -> Unit = {},
    ): Session {
        ensureInitialised()
        val options = SessionOptions(serverUrl).apply(configure)
        return Flashphoner.createSession(options)
    }

    fun createRoomManager(
        serverUrl: String,
        username: String,
        configure: RoomManagerOptions.() -> Unit = {},
    ): RoomManager {
        ensureInitialised()
        val options = RoomManagerOptions(serverUrl, username).apply(configure)
        return Flashphoner.createRoomManager(options)
    }
}
