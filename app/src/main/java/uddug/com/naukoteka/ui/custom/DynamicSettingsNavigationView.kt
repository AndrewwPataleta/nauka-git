package uddug.com.naukoteka.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import uddug.com.domain.entities.profile.FormContainer
import uddug.com.naukoteka.databinding.ViewDynamicSettingsNavigationBinding


class DynamicSettingsNavigationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewDynamicSettingsNavigationBinding


    fun attachDynamicNavigation(
        formContainer: FormContainer,
        parent: ViewGroup,
        onFormNavigationClick: (FormContainer) -> Unit,
    ) {
        binding = ViewDynamicSettingsNavigationBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.formName.text = formContainer.title
        binding.dynamicContainer.setOnClickListener {
            onFormNavigationClick(formContainer)
        }

    }

}
