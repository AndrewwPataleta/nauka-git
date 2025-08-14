package uddug.com.naukoteka.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ViewProfileAcademicDegreeBinding
import uddug.com.naukoteka.presentation.profile.AcademicDegreeModel
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


class AcademicDegreeEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewProfileAcademicDegreeBinding

    public val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    companion object {
        private const val MIN_YEAR_PICKER = 1920
    }

    private var id: String? = null

    fun attachAcademicDegree(
        id: String?,
        academicDegree: String,
        date: String,
        parent: ViewGroup,
    ) {
        binding = ViewProfileAcademicDegreeBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.degreeValue.setText(academicDegree)
        binding.dateGetValue.text = LocalDate.parse(date, EducationAdapter.dateFormat).year.toString()
        binding.dateGetValue.setOnClickListener {
            val numberPicker = MaterialNumberPicker(
                context = context,
                minValue = MIN_YEAR_PICKER,
                maxValue = Calendar.getInstance().get(Calendar.YEAR),
                value = try {
                    LocalDate.parse(date, EducationAdapter.dateFormat).year
                } catch (e: Exception) {
                    Calendar.getInstance().get(Calendar.YEAR)
                },
                separatorColor = ContextCompat.getColor(context, android.R.color.transparent),
                textStyle = Typeface.BOLD_ITALIC,
                editable = false,
                wrapped = false,
                formatter = NumberPicker.Formatter {
                    return@Formatter "${it}"
                }
            )
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.select_year))
                .setView(numberPicker)
                .setNegativeButton(context.getString(R.string.cancel), null)
                .setPositiveButton(context.getString(R.string.choose), { _, _ ->
                    binding.dateGetValue.setText(numberPicker.value.toString())
                })
                .show()
        }
    }

    fun getValue(): AcademicDegreeModel {
        return AcademicDegreeModel(
            id = id,
            academicName = binding.degreeValue.text.toString(),
            academicYear = binding.dateGetValue.text.toString(),
        )
    }

}
