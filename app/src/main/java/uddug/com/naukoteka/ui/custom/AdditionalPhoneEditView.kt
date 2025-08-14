package uddug.com.naukoteka.ui.custom

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.github.stephenvinouze.materialnumberpickercore.MaterialNumberPicker
import uddug.com.domain.entities.profile.ContactData
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ViewProfileAcademicDegreeBinding
import uddug.com.naukoteka.databinding.ViewProfileAdditionalEmailBinding
import uddug.com.naukoteka.databinding.ViewProfileAdditionalPhoneBinding
import uddug.com.naukoteka.presentation.profile.AcademicDegreeModel
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


class AdditionalPhoneEditView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewProfileAdditionalPhoneBinding

    private var phone: ContactData? = null

    fun attachAdditionalPhone(
        parent: ViewGroup,
        phone: ContactData,
        ) {
        binding = ViewProfileAdditionalPhoneBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.value.setText(phone.contact)
        binding.value.addTextChangedListener {
            phone.contact = it.toString()
        }
    }

}
