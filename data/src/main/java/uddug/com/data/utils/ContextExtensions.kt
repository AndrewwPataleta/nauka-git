package uddug.com.data.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.ActivityCompat

fun Context.isLocationPermissionGranted() =
    isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

fun Context.isPermissionGranted(vararg permissions: String): Boolean {
    val notGranted = permissions.filter {
        ActivityCompat.checkSelfPermission(
            this,
            it
        ) != PackageManager.PERMISSION_GRANTED
    }
    return notGranted.isEmpty()
}

fun Context.isLocationEnabled(): Boolean {
    var locationMode = 0
    val locationProviders: String

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        try {
            locationMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE)

        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF

    } else {
        locationProviders =
            Settings.Secure.getString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED)
        return !TextUtils.isEmpty(locationProviders)
    }
}
