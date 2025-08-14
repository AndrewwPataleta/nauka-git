package com.nauchat.core.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


fun String.parseToLocalDate(): LocalDateTime {
    return try {
        LocalDateTime.parse(this.replace("Z", ""))
    } catch (e: Exception) {
        Clock.System.now().toLocalDateTime(TimeZone.UTC)
    }
}

inline fun <T1 : Any, T2 : Any, R : Any> letMultiple(p1: T1?, p2: T2?, block: (T1, T2) -> R?): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

fun String.parseToLocalDateOrNull(): LocalDateTime? {
    return try {
        LocalDateTime.parse(this.replace("Z", ""))
    } catch (e: Exception) {
        null
    }
}

fun String?.initials(): String {
    this ?: return ""

    val initials = StringBuilder()

    split(" ")
        .take(2)
        .forEach { initials.append(it.take(1)) }

    return initials.toString()
}

fun Int.toFormattedNumberString(): String {
    return integerFormatter.format(this)
}

fun Double.toFormattedDecimalString(pattern: String = "#,##0.0"): String {
    return DecimalFormat(
        pattern,
        DecimalFormatSymbols.getInstance(Locale.getDefault()).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }
    ).format(this)
}

fun Double.isWholeNumber(): Boolean {
    return this.compareTo(this.toInt()) == 0
}

private val integerFormatter: DecimalFormat
    get() = DecimalFormat(
        "#,###",
        DecimalFormatSymbols.getInstance(Locale.getDefault()).apply {
            groupingSeparator = ' '
        }
    )

fun <T1, T2, T3, T4, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    transform: suspend (T1, T2, T3, T4) -> R,
): Flow<R> = kotlinx.coroutines.flow.combine(
    kotlinx.coroutines.flow.combine(flow, flow2, ::Pair),
    kotlinx.coroutines.flow.combine(flow3, flow4, ::Pair)
) { t1, t2 ->
    transform(
        t1.first,
        t1.second,
        t2.first,
        t2.second,
    )
}

fun <T1, T2, T3, T4, T5, T6, R> combine(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = kotlinx.coroutines.flow.combine(
    kotlinx.coroutines.flow.combine(flow, flow2, flow3, ::Triple),
    kotlinx.coroutines.flow.combine(flow4, flow5, flow6, ::Triple)
) { t1, t2 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third
    )
}

fun Context.openBrowserByUrl(
    link: String,
) {
    this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
}
