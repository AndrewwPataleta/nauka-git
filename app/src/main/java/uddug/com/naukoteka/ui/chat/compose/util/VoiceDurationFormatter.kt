package uddug.com.naukoteka.ui.chat.compose.util

import java.util.Locale

fun formatVoiceDuration(duration: String?): String? {
    val value = duration?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (value.contains(':')) return value
    val numeric = value.toLongOrNull() ?: return value
    val totalSeconds = when {
        numeric <= 0L -> return null
        numeric > 86_400L && numeric % 1000L == 0L -> numeric / 1000L
        numeric in 1L..86_400L -> numeric
        else -> numeric / 1000L
    }
    return formatVoiceDurationFromSeconds(totalSeconds)
}

fun formatVoiceDurationFromMillis(durationMillis: Long): String {
    if (durationMillis <= 0L) return "00:00"
    val totalSeconds = (durationMillis / 1000).coerceAtLeast(0L)
    return formatVoiceDurationFromSeconds(totalSeconds)
}

fun parseVoiceDurationToMillis(duration: String?): Long? {
    val value = duration?.trim().orEmpty()
    if (value.isEmpty()) return null
    if (value.contains(':')) {
        val parts = value.split(':')
        if (parts.any { it.toLongOrNull() == null }) return null
        var multiplier = 1L
        var totalSeconds = 0L
        for (segment in parts.asReversed()) {
            totalSeconds += (segment.toLongOrNull() ?: return null) * multiplier
            multiplier *= 60L
        }
        return totalSeconds * 1000L
    }
    val numeric = value.toLongOrNull() ?: return null
    val totalSeconds = when {
        numeric <= 0L -> return null
        numeric > 86_400L && numeric % 1000L == 0L -> numeric / 1000L
        numeric in 1L..86_400L -> numeric
        else -> numeric / 1000L
    }
    return totalSeconds * 1000L
}

private fun formatVoiceDurationFromSeconds(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
