package uddug.com.naukoteka.ui.custom

import android.R
import android.R.attr.text
import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.TypedArrayUtils.getText
import uddug.com.naukoteka.databinding.ViewInterestTagBinding
import uddug.com.naukoteka.databinding.ViewWorkExperienceBinding
import uddug.com.naukoteka.utils.getText


class TagInterestView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewInterestTagBinding
    fun attachTag(
        name: String,
        parent: ViewGroup,
    ) {
        binding = ViewInterestTagBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.tag.text = name

    }

}
