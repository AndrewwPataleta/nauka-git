package uddug.com.naukoteka.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager


fun Context.isOffline() = !isOnline()

fun Context.isOnline(): Boolean {
    val manager = getSystemService(CONNECTIVITY_SERVICE) as? ConnectivityManager
    val networkInfo = manager?.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
}



