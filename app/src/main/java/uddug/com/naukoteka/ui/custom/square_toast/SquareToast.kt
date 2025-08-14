package uddug.com.naukoteka.ui.custom.square_toast

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import uddug.com.naukoteka.R
import uddug.com.naukoteka.ui.custom.square_toast.swipedismissdialog.Params
import uddug.com.naukoteka.ui.custom.square_toast.swipedismissdialog.SwipeDismissDialog
import uddug.com.naukoteka.utils.getColorCompat

@SuppressLint("ViewConstructor")
class SquareToast private constructor(context: Context, params: Params) : SwipeDismissDialog(context, params) {
    private var dismissRunnable: Runnable? = null

    override fun show(): SwipeDismissDialog {
        throw RuntimeException("Use show(String text) instead")
    }

    fun show(data: ToastInfo) = data.run {
        val tvToast = findViewById<TextView>(R.id.tv_message)
        findViewById<TextView>(R.id.tv_action).run {
            isVisible = action != null
            text = action?.text
            setOnClickListener {
                action?.invoker?.invoke()
                dismissRunnable?.run()
            }
        }
        findViewById<ImageView>(R.id.iv_icon_left).run {
            isVisible = leftIcon != null
            if (leftIcon != null) {
                setImageResource(leftIcon)
            }
        }
        findViewById<ImageView>(R.id.iv_icon_right).run {
            isVisible = rightIcon != null
            if (rightIcon != null) {
                setImageResource(rightIcon)
            }
        }
        findViewById<ConstraintLayout>(R.id.cl_root).run {
            val color = when(type){
                Type.ERROR -> R.color.background_toast_error
                Type.SUCCESS -> R.color.background_toast_success
                Type.USUAL -> R.color.background_toast
            }
            backgroundTintList = ColorStateList.valueOf(context.getColorCompat(color))
        }
        tvToast.text = text

        super.showToast()
        dismissRunnable = Runnable { this@SquareToast.dismiss() }
        postDelayed(dismissRunnable, 5000)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(dismissRunnable)
        super.onDetachedFromWindow()
    }



    enum class Type { ERROR, SUCCESS, USUAL }

    companion object {
        fun getInstance(context: Context): SquareToast {
            val params = Params()
            params.layoutRes = R.layout.layout_square_toast
            params.overlayColor = android.R.color.transparent
            params.horizontalSwipeEnabled = false
            params.verticalSwipeEnabled = false
            params.isRotationEnabled = false
            params.dismissOnClick = true
            params.dismissOnCancel = true
            params.customLayoutGravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            return SquareToast(context, params)
        }
    }
}