package uddug.com.naukoteka.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.widget.EditText
import androidx.annotation.CheckResult
import androidx.annotation.StringRes
import androidx.core.text.parseAsHtml
import androidx.core.text.toHtml
import androidx.core.text.toSpanned
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.common.internal.Preconditions.checkMainThread
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

inline fun doIfTrue(b: Boolean?, block: () -> Unit) {
    if (b == true) block()
}

inline fun <T> doIfIsNotNull(obj: T?, block: (T) -> Unit) {
    if (obj != null) block(obj)
}

inline fun <T> doIfIsNotNullOrEmpty(list: List<T>?, block: (List<T>) -> Unit) {
    if (!list.isNullOrEmpty()) block(list)
}

inline fun doIfIsNotNullOrEmptyString(model: String?, block: (String) -> Unit) {
    if (!model.isNullOrEmpty()) block(model)
}

inline fun doIfFalse(b: Boolean?, block: () -> Unit) {
    if (b == false) block()
}

inline fun doIfFalseOrNull(b: Boolean?, block: () -> Unit) {
    if (b != true) block()
}


fun Resources.getText(@StringRes id: Int, vararg formatArgs: Any?): CharSequence =
    getText(id).toSpanned().toHtml().format(*formatArgs).parseAsHtml()

public fun getHashCodeToString(str:String?, maxVariants:Int):Int {
    var hashCode = 400
    if (maxVariants<0){return 0}
    for (value in str ?: "a") {
        hashCode += value.toInt()
    }
    return hashCode % maxVariants
}

fun Context.copyToClipboard(text: CharSequence){
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label",text)
    clipboard.setPrimaryClip(clip)
}


@ExperimentalCoroutinesApi
@CheckResult
fun EditText.textChanges(): Flow<CharSequence?> {
    return callbackFlow {
        checkMainThread()

        val listener = doOnTextChanged { text, _, _, _ -> trySend(text) }
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text) }
}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}