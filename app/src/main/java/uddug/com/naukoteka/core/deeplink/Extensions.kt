package uddug.com.naukoteka.core.deeplink

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.annotation.ColorRes
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import androidx.core.content.ContextCompat
import uddug.com.naukoteka.R
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.ZoneOffset
import java.time.ZonedDateTime

private const val ENCODING_SYSTEM = "utf-8"

internal fun String.toUrlEncode(): String = URLEncoder.encode(this, ENCODING_SYSTEM)

fun String.toUrlDecode(): String = URLDecoder.decode(this, ENCODING_SYSTEM)

fun Context.launchCustomTabsByUrl(
    showTitle: Boolean = true,
    instantAppsEnabled: Boolean = true,
    shareState: Int = SHARE_STATE_ON,
    link: String,
    @ColorRes toolbarColor: Int = R.color.back_blue,
    onError: () -> Unit = {}
) {
    try {
        val builder = CustomTabsIntent.Builder()
        val params = CustomTabColorSchemeParams.Builder()
        params.setToolbarColor(ContextCompat.getColor(this, toolbarColor))
        builder.setDefaultColorSchemeParams(params.build())
        builder.setShowTitle(showTitle)
        builder.setShareState(shareState)
        builder.setInstantAppsEnabled(instantAppsEnabled)
        val customBuilder = builder.build()
        customBuilder.launchUrl(this, Uri.parse(link))
      //  customBuilder.launchUrl(this, Uri.parse(getString(R.string.docs_link_to_google, link)))
    } catch (e: ActivityNotFoundException) {
        onError()
    }
}


fun formatMessageTime(time: String): String {
    val messageDate = ZonedDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME)
    val currentDate = ZonedDateTime.now(ZoneOffset.UTC)

    return when {
        messageDate.toLocalDate().isEqual(currentDate.toLocalDate()) -> {
            messageDate.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        messageDate.isAfter(currentDate.minusDays(7)) -> {
            val dayOfWeek = messageDate.dayOfWeek
            val shortDay = dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale("ru"))
            shortDay
        }
        else -> messageDate.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
    }
}