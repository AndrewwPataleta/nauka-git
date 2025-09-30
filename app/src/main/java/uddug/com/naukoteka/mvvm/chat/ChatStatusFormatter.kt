package uddug.com.naukoteka.mvvm.chat

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import androidx.annotation.StringRes
import uddug.com.naukoteka.R

enum class ChatStatusTextMode {
    GENERIC,
    CONTACT
}

class ChatStatusFormatter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun online(mode: ChatStatusTextMode = ChatStatusTextMode.GENERIC): String {
        return when (mode) {
            ChatStatusTextMode.GENERIC -> context.getString(R.string.chat_status_online)
            ChatStatusTextMode.CONTACT -> context.getString(R.string.chat_status_online_contact)
        }
    }

    fun formatLastSeen(lastSeen: Instant, mode: ChatStatusTextMode = ChatStatusTextMode.GENERIC): String {
        return runCatching {
            val duration = Duration.between(lastSeen, Instant.now())
            val minutes = duration.toMinutes()
            val hours = duration.toHours()
            val days = duration.toDays()
            val weeks = days / 7

            val adjustedMinutes = if (mode == ChatStatusTextMode.CONTACT) minutes.coerceAtLeast(1) else minutes
            val adjustedHours = if (mode == ChatStatusTextMode.CONTACT) hours.coerceAtLeast(1) else hours
            val adjustedDays = if (mode == ChatStatusTextMode.CONTACT) days.coerceAtLeast(1) else days
            val adjustedWeeks = if (mode == ChatStatusTextMode.CONTACT) weeks.coerceAtLeast(1) else weeks

            when {
                mode == ChatStatusTextMode.CONTACT && days == 1L -> context.getString(R.string.chat_status_contact_last_seen_yesterday)
                adjustedMinutes < 60 -> getStringForMode(
                    genericRes = R.string.chat_status_last_seen_minutes,
                    contactRes = R.string.chat_status_contact_last_seen_minutes,
                    mode = mode,
                    value = adjustedMinutes
                )
                adjustedHours < 24 -> getStringForMode(
                    genericRes = R.string.chat_status_last_seen_hours,
                    contactRes = R.string.chat_status_contact_last_seen_hours,
                    mode = mode,
                    value = adjustedHours
                )
                adjustedDays < 7 -> getStringForMode(
                    genericRes = R.string.chat_status_last_seen_days,
                    contactRes = R.string.chat_status_contact_last_seen_days,
                    mode = mode,
                    value = adjustedDays
                )
                else -> getStringForMode(
                    genericRes = R.string.chat_status_last_seen_weeks,
                    contactRes = R.string.chat_status_contact_last_seen_weeks,
                    mode = mode,
                    value = adjustedWeeks
                )
            }
        }.getOrElse { "" }
    }

    private fun getStringForMode(
        @StringRes genericRes: Int,
        @StringRes contactRes: Int,
        mode: ChatStatusTextMode,
        value: Long
    ): String {
        val resId = if (mode == ChatStatusTextMode.CONTACT) contactRes else genericRes
        return context.getString(resId, value)
    }
}
