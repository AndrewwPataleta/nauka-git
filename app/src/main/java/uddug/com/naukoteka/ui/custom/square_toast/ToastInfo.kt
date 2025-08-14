package uddug.com.naukoteka.ui.custom.square_toast

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ToastInfo(
    val text: String,
    val leftIcon: Int? = null,
    val rightIcon: Int? = null,
    val type: SquareToast.Type = SquareToast.Type.USUAL,
    val action: ToastAction? = null,
) : Parcelable


@Parcelize
data class ToastAction(val text: String, val invoker: () -> Unit) : Parcelable