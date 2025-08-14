package uddug.com.naukoteka.utils.ui

import android.content.Context
import android.view.WindowManager
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

fun <F : Fragment> FragmentActivity.findFragment(tag: String): F? {
    return (supportFragmentManager.findFragmentByTag(tag) as? F)
}

fun BottomSheetDialogFragment.setDsMaxHeight() {
    val windowManager: WindowManager =
        view?.context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val maxHeight = windowManager.defaultDisplay.height * 0.9
    setBottomSheetHeight(maxHeight.toInt())
}

fun BottomSheetDialogFragment.setBottomSheetHeight(height: Int) {
    view?.updateLayoutParams {
        this.height = height
    }

    (dialog as BottomSheetDialog).behavior.apply {
        isFitToContents = true
        skipCollapsed = true
        state = BottomSheetBehavior.STATE_EXPANDED
    }
}
