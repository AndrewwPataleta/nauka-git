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
import kotlin.math.roundToInt

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

    // Когда пользователь вручную выбрал устройство вывода в шторке звонка, мы
    // перестаём авто-роутить и держим его выбор, пока устройство доступно.
    // null — авто-режим (как раньше: BT > проводные > динамик).
    private var manualRouteId: String? = null

    // Уведомляем ViewModel о смене списка устройств/активного маршрута, чтобы
    // "Микрофон и динамик" и лист выбора устройства были актуальны.
    var onRoutesChanged: (() -> Unit)? = null

    // Пока звонок активен, слушаем подключение/отключение наушников и
    // перекидываем маршрут звука. Раньше динамик включался безусловно и любые
    // наушники "не определялись".
    private val deviceCallback: AudioDeviceCallback? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    updateAudioRoute()
                    onRoutesChanged?.invoke()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    // A manually selected device that just disappeared falls back
                    // to auto-routing.
                    if (manualRouteId != null && availableRoutes().none { it.id == manualRouteId }) {
                        manualRouteId = null
                    }
                    updateAudioRoute()
                    onRoutesChanged?.invoke()
                }
            }
        } else null

    fun acquire(): Boolean {
        if (isActive) return true
        val am = audioManager ?: return false

        previousMode = am.mode
        previousSpeakerphoneOn = am.isSpeakerphoneOn
        manualRouteId = null
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

    /**
     * Список доступных выходов звука для листа «Выбор устройства».
     * На Android 12+ берём системный список коммуникационных устройств; на
     * более старых собираем вручную (динамик/телефон + проводные/BT по факту).
     */
    fun availableRoutes(): List<AudioRoute> {
        val am = audioManager ?: return emptyList()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return emptyList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return am.availableCommunicationDevices
                .mapNotNull { device ->
                    routeTypeFor(device.type)?.let { type ->
                        AudioRoute(device.id.toString(), routeName(device, type), type)
                    }
                }
                .distinctBy { it.id }
        }

        val routes = mutableListOf(
            AudioRoute(ID_EARPIECE, NAME_EARPIECE, AudioRouteType.EARPIECE),
            AudioRoute(ID_SPEAKER, NAME_SPEAKER, AudioRouteType.SPEAKER),
        )
        am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).forEach { d ->
            when (d.type) {
                in WIRED_OUTPUT_TYPES -> routes += AudioRoute(ID_WIRED, NAME_WIRED, AudioRouteType.WIRED)
                in BLUETOOTH_OUTPUT_TYPES ->
                    routes += AudioRoute(ID_BT_PREFIX + d.id, btName(d), AudioRouteType.BLUETOOTH)
            }
        }
        return routes.distinctBy { it.id }
    }

    /** Id текущего активного выхода (для галочки и подписи «Микрофон и динамик»). */
    fun currentRouteId(): String? {
        val am = audioManager ?: return null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return am.communicationDevice?.id?.toString()
        }
        manualRouteId?.let { return it }
        return when {
            @Suppress("DEPRECATION") am.isBluetoothScoOn ->
                availableRoutes().firstOrNull { it.type == AudioRouteType.BLUETOOTH }?.id
            am.isSpeakerphoneOn -> ID_SPEAKER
            hasOutputOfTypes(WIRED_OUTPUT_TYPES) -> ID_WIRED
            else -> ID_EARPIECE
        }
    }

    /** Пользователь выбрал устройство в листе — фиксируем и применяем маршрут. */
    fun selectRoute(id: String) {
        val am = audioManager ?: return
        manualRouteId = id
        if (isActive) applyManualRoute(am, id)
        onRoutesChanged?.invoke()
    }

    /** Текущая громкость звонка в долях [0..1] (STREAM_VOICE_CALL). */
    fun getCallVolume(): Float {
        val am = audioManager ?: return 0f
        val max = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL).takeIf { it > 0 } ?: return 0f
        return am.getStreamVolume(AudioManager.STREAM_VOICE_CALL).toFloat() / max
    }

    /** Устанавливает громкость звонка из доли [0..1]. */
    fun setCallVolume(fraction: Float) {
        val am = audioManager ?: return
        val max = am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
        val target = (fraction.coerceIn(0f, 1f) * max).roundToInt()
        runCatching { am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, target, 0) }
    }

    private fun applyManualRoute(am: AudioManager, id: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val device = am.availableCommunicationDevices
                .firstOrNull { it.id.toString() == id } ?: return false
            stopBluetoothSco(am)
            if (am.isSpeakerphoneOn) am.isSpeakerphoneOn = false
            return runCatching { am.setCommunicationDevice(device) }.getOrDefault(false)
        }
        val route = availableRoutes().firstOrNull { it.id == id } ?: return false
        when (route.type) {
            AudioRouteType.BLUETOOTH -> {
                if (am.isSpeakerphoneOn) am.isSpeakerphoneOn = false
                runCatching {
                    @Suppress("DEPRECATION")
                    if (!am.isBluetoothScoOn) {
                        am.startBluetoothSco()
                        am.isBluetoothScoOn = true
                    }
                }
            }
            AudioRouteType.SPEAKER -> {
                stopBluetoothSco(am)
                if (!am.isSpeakerphoneOn) am.isSpeakerphoneOn = true
            }
            AudioRouteType.WIRED, AudioRouteType.EARPIECE -> {
                stopBluetoothSco(am)
                if (am.isSpeakerphoneOn) am.isSpeakerphoneOn = false
            }
        }
        return true
    }

    private fun routeTypeFor(deviceType: Int): AudioRouteType? = when (deviceType) {
        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> AudioRouteType.EARPIECE
        AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> AudioRouteType.SPEAKER
        in WIRED_OUTPUT_TYPES -> AudioRouteType.WIRED
        in BLUETOOTH_OUTPUT_TYPES -> AudioRouteType.BLUETOOTH
        else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            deviceType == AudioDeviceInfo.TYPE_BLE_HEADSET
        ) AudioRouteType.BLUETOOTH else null
    }

    private fun routeName(device: AudioDeviceInfo, type: AudioRouteType): String = when (type) {
        AudioRouteType.EARPIECE -> NAME_EARPIECE
        AudioRouteType.SPEAKER -> NAME_SPEAKER
        AudioRouteType.WIRED -> NAME_WIRED
        AudioRouteType.BLUETOOTH -> device.productName?.toString()?.takeIf { it.isNotBlank() }
            ?: NAME_BLUETOOTH
    }

    private fun btName(device: AudioDeviceInfo): String =
        device.productName?.toString()?.takeIf { it.isNotBlank() } ?: NAME_BLUETOOTH

    private fun updateAudioRoute() {
        if (!isActive) return
        val am = audioManager ?: return
        if (am.mode != AudioManager.MODE_IN_COMMUNICATION) {
            am.mode = AudioManager.MODE_IN_COMMUNICATION
        }

        // User picked a device explicitly — honour it instead of auto-routing.
        manualRouteId?.let { id ->
            if (applyManualRoute(am, id)) return
            manualRouteId = null
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
        manualRouteId = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { am.clearCommunicationDevice() }
        }

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

        const val ID_EARPIECE = "route_earpiece"
        const val ID_SPEAKER = "route_speaker"
        const val ID_WIRED = "route_wired"
        const val ID_BT_PREFIX = "route_bt_"

        const val NAME_EARPIECE = "Телефон"
        const val NAME_SPEAKER = "Динамик телефона"
        const val NAME_WIRED = "Проводная гарнитура"
        const val NAME_BLUETOOTH = "Bluetooth-устройство"

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

enum class AudioRouteType { EARPIECE, SPEAKER, WIRED, BLUETOOTH }

data class AudioRoute(
    val id: String,
    val name: String,
    val type: AudioRouteType,
)
