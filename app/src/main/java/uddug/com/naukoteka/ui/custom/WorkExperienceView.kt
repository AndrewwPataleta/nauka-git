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
import uddug.com.naukoteka.databinding.ViewWorkExperienceBinding
import uddug.com.naukoteka.utils.getText


class WorkExperienceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewWorkExperienceBinding


    fun attachWorkExperience(
        jobId: String,
        jobTitle: String,
        parent: ViewGroup,
        jobPlace: String,
        onWorkExperienceClick: (String) -> Unit,
    ) {
        binding = ViewWorkExperienceBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.root.setOnClickListener {
            onWorkExperienceClick(jobId)
        }

        val spannable = SpannableString(
            resources.getString(
                uddug.com.naukoteka.R.string.labor_activity_name,
                jobTitle,
                jobPlace
            )
        )
        spannable.setSpan(
            TextAppearanceSpan(
                context,
                uddug.com.naukoteka.R.style.NauTextAppearance_AppCompat_SubTitleDark
            ),
            0, // beginning of hPa
            jobTitle.length + 2, // end of string
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            TextAppearanceSpan(
                context,
                uddug.com.naukoteka.R.style.NauTextAppearance_AppCompat_Link
            ),
            jobTitle.length + 2, // beginning of hPa
            spannable.length, // end of string
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.workDescription.text = spannable

    }

}
