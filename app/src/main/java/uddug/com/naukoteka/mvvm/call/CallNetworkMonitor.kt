package uddug.com.naukoteka.mvvm.call

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Активный мониторинг сети на время звонка. Полагаться только на то, что WCS сам
 * сообщит о разрыве, нельзя: при смене WiFi на моб. связь старый сокет привязан к
 * умершему интерфейсу и мёртвый TCP висит до таймаута (десятки секунд). Всё это
 * время сервер уже считает нас ушедшими и показывает вебу «выпал, вызвать
 * повторно», а мы ещё думаем что в звонке. Ловим смену интерфейса мгновенно через
 * ConnectivityManager и сами инициируем восстановление.
 */
@Singleton
class CallNetworkMonitor @Inject constructor(
    @ApplicationContext context: Context,
) {

    interface Listener {
        /** Интернета нет вовсе (все сети пропали). */
        fun onConnectionLost()

        /**
         * Интернет доступен. [switched] = активный транспорт сменился на лету
         * (напр. WiFi отвалился, остался LTE) — старый сокет мёртв, надо
         * переустанавливать соединение. false = просто снова появилась сеть.
         */
        fun onConnectionAvailable(switched: Boolean)
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val available = LinkedHashSet<Network>()
    private var activeNetwork: Network? = null
    private var listener: Listener? = null
    private var callback: ConnectivityManager.NetworkCallback? = null

    fun start(listener: Listener) {
        if (callback != null) return
        val cm = connectivityManager ?: return
        this.listener = listener
        available.clear()
        activeNetwork = null

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val wasOffline = available.isEmpty()
                available.add(network)
                if (wasOffline) {
                    activeNetwork = network
                    Log.d(TAG, "network available (was offline)")
                    listener.onConnectionAvailable(switched = false)
                }
            }

            override fun onLost(network: Network) {
                available.remove(network)
                when {
                    available.isEmpty() -> {
                        activeNetwork = null
                        Log.d(TAG, "network lost, fully offline")
                        listener.onConnectionLost()
                    }
                    network == activeNetwork -> {
                        activeNetwork = available.last()
                        Log.d(TAG, "active transport dropped, switched to another")
                        listener.onConnectionAvailable(switched = true)
                    }
                }
            }
        }
        callback = cb
        runCatching { cm.registerNetworkCallback(request, cb) }
            .onFailure { Log.e(TAG, "registerNetworkCallback failed", it) }
    }

    fun stop() {
        callback?.let { cb -> runCatching { connectivityManager?.unregisterNetworkCallback(cb) } }
        callback = null
        listener = null
        available.clear()
        activeNetwork = null
    }

    private companion object {
        const val TAG = "CallNetMonitor"
    }
}
