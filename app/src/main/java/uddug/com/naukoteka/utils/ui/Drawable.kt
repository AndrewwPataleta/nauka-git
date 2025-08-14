package uddug.com.naukoteka.utils.ui

import android.content.res.ColorStateList
import android.view.View

fun View.backgroundTintColor(color: Int) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        backgroundTintList = ColorStateList.valueOf(color)
    }
}