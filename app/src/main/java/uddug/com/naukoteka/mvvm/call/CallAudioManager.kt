package uddug.com.naukoteka.mvvm.call

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallAudioManager @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val audioManager: AudioManager? =
        context.getSystemService(AudioManager::class.java)

    private var focusRequest: AudioFocusRequest? = null
    private var previousMode: Int? = null
    private var previousSpeakerphoneOn: Boolean? = null
    private var isActive = false

    fun acquire(): Boolean {
        if (isActive) return true
        val am = audioManager ?: return false

        previousMode = am.mode
        previousSpeakerphoneOn = am.isSpeakerphoneOn
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        am.isSpeakerphoneOn = true

        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                .setOnAudioFocusChangeListener { }
                .build()
            focusRequest = request
            am.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                null,
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        isActive = granted
        if (!granted) {
            Log.w(TAG, "Audio focus not granted")
            restorePreviousState(am)
        }
        return granted
    }

    fun ensureSpeakerphoneOn() {
        if (!isActive) return
        val am = audioManager ?: return
        if (!am.isSpeakerphoneOn) {
            am.isSpeakerphoneOn = true
        }
        if (am.mode != AudioManager.MODE_IN_COMMUNICATION) {
            am.mode = AudioManager.MODE_IN_COMMUNICATION
        }
    }

    fun release() {
        if (!isActive) return
        val am = audioManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
        focusRequest = null
        restorePreviousState(am)
        isActive = false
    }

    private fun restorePreviousState(am: AudioManager) {
        previousSpeakerphoneOn?.let { am.isSpeakerphoneOn = it }
        previousMode?.let { am.mode = it }
        previousSpeakerphoneOn = null
        previousMode = null
    }

    private companion object {
        const val TAG = "CallAudioManager"
    }
}
