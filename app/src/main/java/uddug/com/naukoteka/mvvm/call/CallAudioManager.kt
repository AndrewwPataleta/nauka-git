package uddug.com.naukoteka.mvvm.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallAudioManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val audioManager: AudioManager? =
        context.getSystemService(AudioManager::class.java)

    private var focusRequest: AudioFocusRequest? = null
    private var previousMode: Int? = null
    private var previousSpeakerphoneOn: Boolean? = null
    private var isActive = false

    // Пока звонок активен, слушаем подключение/отключение наушников и
    // перекидываем маршрут звука. Раньше динамик включался безусловно и любые
    // наушники "не определялись".
    private val deviceCallback: AudioDeviceCallback? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    updateAudioRoute()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    updateAudioRoute()
                }
            }
        } else null

    fun acquire(): Boolean {
        if (isActive) return true
        val am = audioManager ?: return false

        previousMode = am.mode
        previousSpeakerphoneOn = am.isSpeakerphoneOn
        am.mode = AudioManager.MODE_IN_COMMUNICATION

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
            return false
        }

        deviceCallback?.let { am.registerAudioDeviceCallback(it, null) }
        updateAudioRoute()
        return true
    }

    /**
     * Перевыбираем маршрут звука по подключённым устройствам: если есть
     * проводная/USB/Bluetooth-гарнитура — звук идёт туда (динамик выключаем,
     * для Bluetooth поднимаем SCO). Если внешних выходов нет — включаем динамик.
     */
    fun ensureSpeakerphoneOn() {
        updateAudioRoute()
    }

    private fun updateAudioRoute() {
        if (!isActive) return
        val am = audioManager ?: return
        if (am.mode != AudioManager.MODE_IN_COMMUNICATION) {
            am.mode = AudioManager.MODE_IN_COMMUNICATION
        }

        val hasBluetooth = hasOutputOfTypes(BLUETOOTH_OUTPUT_TYPES)
        val hasWired = hasOutputOfTypes(WIRED_OUTPUT_TYPES)

        when {
            hasBluetooth -> {
                val btConnectGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT,
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
                if (btConnectGranted) {
                    if (am.isSpeakerphoneOn) am.isSpeakerphoneOn = false
                    runCatching {
                        @Suppress("DEPRECATION")
                        if (!am.isBluetoothScoOn) {
                            am.startBluetoothSco()
                            am.isBluetoothScoOn = true
                        }
                    }.onFailure { t ->
                        Log.w(TAG, "startBluetoothSco failed: ${t.message}")
                        if (!am.isSpeakerphoneOn) am.isSpeakerphoneOn = true
                    }
                } else {
                    Log.w(TAG, "BLUETOOTH_CONNECT not granted – falling back to speakerphone")
                    stopBluetoothSco(am)
                    if (!am.isSpeakerphoneOn) am.isSpeakerphoneOn = true
                }
            }

            hasWired -> {
                stopBluetoothSco(am)
                if (am.isSpeakerphoneOn) am.isSpeakerphoneOn = false
            }

            else -> {
                stopBluetoothSco(am)
                if (!am.isSpeakerphoneOn) am.isSpeakerphoneOn = true
            }
        }
    }

    private fun hasOutputOfTypes(types: IntArray): Boolean {
        val am = audioManager ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        return am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            .any { it.type in types }
    }

    private fun stopBluetoothSco(am: AudioManager) {
        runCatching {
            @Suppress("DEPRECATION")
            if (am.isBluetoothScoOn) {
                am.isBluetoothScoOn = false
                am.stopBluetoothSco()
            }
        }
    }

    fun release() {
        if (!isActive) return
        val am = audioManager ?: return

        deviceCallback?.let { runCatching { am.unregisterAudioDeviceCallback(it) } }
        stopBluetoothSco(am)

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

        val WIRED_OUTPUT_TYPES = intArrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE,
        )

        val BLUETOOTH_OUTPUT_TYPES = intArrayOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        )
    }
}
