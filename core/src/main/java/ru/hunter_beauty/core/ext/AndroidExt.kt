package com.nauchat.core.ext

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.savedstate.SavedStateRegistryOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.math.roundToInt

fun Context.getDrawableByRes(@DrawableRes resId: Int): Drawable? {
    return ContextCompat.getDrawable(this, resId)
}

fun Context.getColorByRes(@ColorRes resId: Int): Int {
    return ContextCompat.getColor(this, resId)
}

fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Int.toPx(): Int = (this * displayDensity).toInt()

fun Float.toPx(): Float = this * displayDensity

fun Double.toPx(): Double = this * displayDensity

fun Int.toDp(): Int = (this / displayDensity).toInt()

private val displayDensity get() = Resources.getSystem().displayMetrics.density

fun NavController.safeNavigate(direction: NavDirections) {
    currentDestination?.getAction(direction.actionId)?.run { navigate(direction) }
}

fun setSearchViewOnClickListener(v: View, listener: View.OnClickListener) {
    if (v is ViewGroup) {
        val count = v.childCount
        for (i in 0 until count) {
            val child = v.getChildAt(i)
            if (child is LinearLayout || child is RelativeLayout) {
                setSearchViewOnClickListener(child, listener)
            }

            if (child is TextView) {
                child.isFocusable = false
            }
            child.setOnClickListener(listener)
        }
    }
}

fun View.hideKeyboard() {
    val imm: InputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(applicationWindowToken, 0)
}

fun View.showKeyboard() {
    val imm: InputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(this, 0)
}

fun TextView.setOrHideIfBlank(value: String) {
    if (value.isNotBlank()) {
        isVisible = true
        text = value
    } else {
        isVisible = false
    }
}



fun <T> Flow<T>.launchWhenStarted(lifecycleScope: LifecycleCoroutineScope) {
    lifecycleScope.launchWhenStarted {
        this@launchWhenStarted.collect()
    }
}

fun <T> Flow<T>.launchWhenCreated(lifecycleScope: LifecycleCoroutineScope) {
    lifecycleScope.launchWhenCreated {
        this@launchWhenCreated.collect()
    }
}

fun <T> Flow<T>.launchWhenResumed(lifecycleScope: LifecycleCoroutineScope) {
    lifecycleScope.launchWhenResumed {
        this@launchWhenResumed.collect()
    }
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

fun BottomSheetDialogFragment.setDraggable(draggable: Boolean) {
    (dialog as? BottomSheetDialog)?.behavior?.apply {
        isDraggable = draggable
    }
}

inline fun <reified VM : ViewModel> ComponentActivity.assistedViewModel(
    noinline argumentsProducer: (() -> Bundle)? = null,
    noinline viewModelProducer: (SavedStateHandle) -> VM,
): Lazy<VM> = lazy {
    ViewModelProvider(
        this,
        createSavedStateViewModelFactory(
            arguments = argumentsProducer?.invoke() ?: intent.extras,
            creator = viewModelProducer
        )
    ).get(VM::class.java)
}

inline fun <reified VM : ViewModel> Fragment.assistedViewModel(
    noinline viewModelProducer: (SavedStateHandle) -> VM,
): Lazy<VM> = lazy {
    ViewModelProvider(this, createSavedStateViewModelFactory(arguments, viewModelProducer))
        .get(VM::class.java)
}

inline fun <reified VM : ViewModel> Fragment.assistedActivityViewModel(
    noinline viewModelProducer: (SavedStateHandle) -> VM,
): Lazy<VM> = lazy {
    ViewModelProvider(
        this.requireActivity(),
        createSavedStateViewModelFactory(arguments, viewModelProducer)
    )
        .get(VM::class.java)
}

@PublishedApi
internal inline fun <reified VM : ViewModel> SavedStateRegistryOwner.createSavedStateViewModelFactory(
    arguments: Bundle?,
    crossinline creator: (SavedStateHandle) -> VM,
): ViewModelProvider.Factory =
    object : AbstractSavedStateViewModelFactory(this@createSavedStateViewModelFactory, arguments) {

        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle,
        ): T = creator(handle) as T
    }

fun View.toggleOnRecyclerScroll(yAxis: Int) {
    translationY += yAxis * 0.3f

    when {
        translationY < 0 -> {
            translationY = 0f
        }

        translationY > height -> {
            translationY = height.toFloat()
        }
    }
}

fun Window.changeStatusBarColor(@ColorInt color: Int) {
    addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    statusBarColor = color
}

@ColorInt
fun Int.withAlpha(ratio: Float): Int {
    val alpha = (Color.alpha(this) * ratio).roundToInt()
    val r: Int = Color.red(this)
    val g: Int = Color.green(this)
    val b: Int = Color.blue(this)
    return Color.argb(alpha, r, g, b)
}

inline fun doIfTrue(b: Boolean?, block: () -> Unit) {
    if (b == true) block()
}

inline fun <T> doIfIsNotNull(obj: T?, block: (T) -> Unit) {
    if (obj != null) block(obj)
}

inline fun <T> doIfIsNotNullOrEmpty(list: List<T>?, block: (List<T>) -> Unit) {
    if (!list.isNullOrEmpty()) block(list)
}

inline fun doIfFalse(b: Boolean?, block: () -> Unit) {
    if (b == false) block()
}

inline fun doIfFalseOrNull(b: Boolean?, block: () -> Unit) {
    if (b != true) block()
}
