package uddug.com.naukoteka.flashphoner

import android.app.Activity
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

    fun prepareSession(
        activity: Activity,
        serverUrl: String,
        configureOptions: SessionOptions.() -> Unit = {},
        onSessionReady: Session.() -> Unit = {}
    ): Session {
        val session = environment.createSession(activity, serverUrl, configureOptions)
        session.onSessionReady()
        sessionRef.set(session)
        return session
    }

    fun createStream(
        streamName: String,
        configure: StreamOptions.() -> Unit = {}
    ): Stream {
        val session = sessionRef.get()
            ?: error("Flashphoner session must be prepared before creating streams")
        val options = StreamOptions(streamName).apply(configure)
        val stream = session.createStream(options)
        streamRef.set(stream)
        return stream
    }

    fun stopStream() {
        streamRef.getAndSet(null)?.stop()
    }

    fun disconnectSession() {
        stopStream()
        sessionRef.getAndSet(null)?.disconnect()
    }
}
