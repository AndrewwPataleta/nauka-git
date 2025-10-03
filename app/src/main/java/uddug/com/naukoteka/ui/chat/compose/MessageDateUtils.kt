package uddug.com.naukoteka.ui.chat.compose

import android.content.Context
import android.os.Build
import uddug.com.domain.entities.chat.MessageChat
import uddug.com.naukoteka.R
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

internal fun MessageChat.messageDate(zoneId: ZoneId): LocalDate =
    createdAt.atZone(zoneId).toLocalDate()

internal fun shouldShowDateBadge(
    previousMessage: MessageChat?,
    currentMessage: MessageChat,
    zoneId: ZoneId
): Boolean {
    val previousDate = previousMessage?.messageDate(zoneId)
    val currentDate = currentMessage.messageDate(zoneId)
    return previousDate != currentDate
}

internal fun formatMessageDate(
    context: Context,
    date: LocalDate,
    zoneId: ZoneId
): String {
    val today = LocalDate.now(zoneId)
    val daysDifference = ChronoUnit.DAYS.between(date, today)

    return when {
        daysDifference <= -1L -> date.format(createFormatter(context))
        daysDifference == 0L -> context.getString(R.string.chat_message_date_today)
        daysDifference == 1L -> context.getString(R.string.chat_message_date_yesterday)
        daysDifference in 2..6 -> context.resources.getQuantityString(
            R.plurals.chat_message_date_days_ago,
            daysDifference.toInt(),
            daysDifference.toInt()
        )
        daysDifference == 7L -> context.getString(R.string.chat_message_date_week_ago)
        else -> date.format(createFormatter(context))
    }
}

private fun createFormatter(context: Context): DateTimeFormatter {
    val locale = currentLocale(context)
    return DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
}

@Suppress("DEPRECATION")
private fun currentLocale(context: Context): Locale {
    val configuration = context.resources.configuration
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        configuration.locales[0]
    } else {
        configuration.locale
    }
}
