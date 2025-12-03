package uddug.com.naukoteka.flashphoner

import android.app.Activity
import java.lang.ref.WeakReference
import com.flashphoner.fpwcsapi.Flashphoner
import com.flashphoner.fpwcsapi.session.Session
import com.flashphoner.fpwcsapi.session.SessionOptions
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central entry point to the Flashphoner Android SDK. The environment is responsible
 * for initialising the SDK once and creating pre-configured [Session] instances that
 * can be used by feature modules.
 */
@Singleton
class FlashphonerEnvironment @Inject constructor() {

    private var isInitialised: Boolean = false
    private var containerActivityRef: WeakReference<Activity?> = WeakReference(null)

    fun attachContainerActivity(activity: Activity) {
        containerActivityRef = WeakReference(activity)
    }

    /**
     * Initialise Flashphoner lazily on first usage. According to the SDK documentation
     * the call is idempotent, therefore we protect it with an [isInitialised] flag to
     * avoid extra work on subsequent injections.
     */
    fun ensureInitialised(activity: Activity? = containerActivityRef.get()) {
        if (!isInitialised) {
            val hostActivity = activity ?: error("Container Activity must be attached before initializing Flashphoner")
            Flashphoner.init(hostActivity)
            isInitialised = true
        }
    }

    /**
     * Creates a new [Session] with provided [serverUrl] and optional configuration block.
     * The caller is still responsible for connecting the session and listening for state
     * callbacks according to the Flashphoner SDK documentation.
     */
    fun createSession(
        serverUrl: String,
        configure: SessionOptions.() -> Unit = {},
    ): Session {
        ensureInitialised()
        val options = SessionOptions(serverUrl).apply(configure)
        return Flashphoner.createSession(options)
    }
}
