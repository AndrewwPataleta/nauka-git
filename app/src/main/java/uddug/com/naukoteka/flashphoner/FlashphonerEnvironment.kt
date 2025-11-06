package uddug.com.naukoteka.flashphoner

import android.content.Context
import com.flashphoner.fpwcsapi.Flashphoner
import com.flashphoner.fpwcsapi.Session
import com.flashphoner.fpwcsapi.SessionOptions
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Central entry point to the Flashphoner Android SDK. The environment is responsible
 * for initialising the SDK once and creating pre-configured [Session] instances that
 * can be used by feature modules.
 */
@Singleton
class FlashphonerEnvironment @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    private var isInitialised: Boolean = false

    /**
     * Initialise Flashphoner lazily on first usage. According to the SDK documentation
     * the call is idempotent, therefore we protect it with an [isInitialised] flag to
     * avoid extra work on subsequent injections.
     */
    fun ensureInitialised() {
        if (!isInitialised) {
            Flashphoner.init(appContext)
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
        configure: SessionOptions.() -> Unit = {}
    ): Session {
        ensureInitialised()
        val options = SessionOptions(serverUrl).apply(configure)
        return Flashphoner.createSession(options)
    }
}
