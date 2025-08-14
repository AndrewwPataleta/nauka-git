package uddug.com.naukoteka.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import uddug.com.naukoteka.databinding.ViewProfileCareerInfoBinding
import uddug.com.naukoteka.databinding.ViewProfileStudyInfoBinding


class EducationExperienceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewProfileStudyInfoBinding


    fun attachStudyExperience(
        educationName: String,
        placeWithDate: String,
        speciality: String,
        parent: ViewGroup,
    ) {
        binding = ViewProfileStudyInfoBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.educationName.text = educationName
        binding.placeWithDate.text = placeWithDate
        binding.speciality.text = speciality
    }

}
