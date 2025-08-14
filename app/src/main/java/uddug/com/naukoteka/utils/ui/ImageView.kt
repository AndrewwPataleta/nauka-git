package uddug.com.naukoteka.utils.ui

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import uddug.com.naukoteka.GlideApp

fun ImageView.load(
    model: String,
    withAnimation: Boolean = true,
    requestOptions: RequestOptions? = null,
    @DrawableRes placeholder: Int? = android.R.color.transparent,
    @DrawableRes error: Int? = placeholder

) {
    GlideApp.with(this).load(model)
        .placeholder(placeholder ?: 0)
        .error(error ?: 0)
        .apply(requestOptions ?: RequestOptions())
        .apply {
            if (withAnimation) {
                transition(
                    DrawableTransitionOptions.withCrossFade(
                        DrawableCrossFadeFactory.Builder()
                            .setCrossFadeEnabled(true).build()
                    )
                )
            }
        }
        .into(this)
}
