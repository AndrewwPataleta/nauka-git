package uddug.com.naukoteka.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import uddug.com.naukoteka.databinding.ViewProfileCareerInfoBinding


class CareerExperienceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewProfileCareerInfoBinding


    fun attachWorkExperience(
        jobId: String,
        jobName: String,
        parent: ViewGroup,
        jobPlace: String,
        jobDate: String
    ) {
        binding = ViewProfileCareerInfoBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.place.text = jobPlace
        binding.jobName.text = jobName
        binding.date.text = jobDate
    }

}
