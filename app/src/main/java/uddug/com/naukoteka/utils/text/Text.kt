package uddug.com.naukoteka.utils.text

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import java.util.regex.Matcher
import java.util.regex.Pattern

fun CharSequence?.isNotNullOrEmpty() = !isNullOrEmpty()

@Suppress("DEPRECATION")
fun CharSequence?.formatAsHtml(): CharSequence? = this?.let {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Html.fromHtml(it.toString())
    else Html.fromHtml(it.toString(), Html.FROM_HTML_MODE_LEGACY)
}

@Suppress("DEPRECATION")
fun String?.formatAsHtml() = this.let {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) Html.fromHtml(it).toString()
    else Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString()
}

fun String.getColoredString(subtext: String?, color: Int): Spannable? {
    val spannable = SpannableString(this)
    val i = subtext?.let { indexOf(it) } ?: 0
    if (i > 0) {
        spannable.setSpan(
            RelativeSizeSpan(1.1f),
            i,
            i + (subtext?.length ?: 0),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            ForegroundColorSpan(color),
            i,
            i + (subtext?.length ?: 0),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    return spannable
}

fun String.shareIntent(activity: Activity) {
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.type = "text/plain"
    shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    shareIntent.putExtra(Intent.EXTRA_TEXT, this)
    activity.startActivity(Intent.createChooser(shareIntent, ""))
}

fun String.toHtmlColorFontFormat(fontSize: Int = 15): String {
    return ("<html><style type='text/css'>@font-face { font-family: sfuid_display_regular; src: url('fonts/sfuid_display_regular.otf'); } body p {color: #424242; font-family: sfuid_display_regular;}</style>"
            + "<body >" + "<p align=\"justify\" style=\"font-size: ${fontSize}px; font-family: sfuid_display_regular;\">" + this
        .paintHashTags() + "</p> " + "</body></html>")
}

fun String.replaceNewLineToBr(): String {
    return replace("\n\n\n", "<br><br>")
        .replace("\n\n", "<br><br>")
        .replace("\n", "<br><br>")
}

fun String.paintHashTags(): String {
    var value = this
    val pattern = Pattern.compile("#(\\S+\\s?\\n?)")
    val mat: Matcher = pattern.matcher(this)
    val hashTagsWords: MutableList<String> = ArrayList()
    while (mat.find()) { hashTagsWords.add("#" + mat.group(1)) }
    hashTagsWords.forEach {
        value = value.replace(it, "<font color=#00829F>${it}</font>")
    }
    return value
}
