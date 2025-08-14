package uddug.com.naukoteka.ui.custom

import android.app.Dialog
import android.content.Context
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import uddug.com.domain.entities.profile.SettingsElement
import uddug.com.domain.interactors.user_profile.model.DefaultCls
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.ViewDynamicSettingBinding


class DynamicSettingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var binding: ViewDynamicSettingBinding


    fun attachDynamicNavigation(
        element: SettingsElement,
        clsList: List<DefaultCls>,
        userSettings: MutableMap<String, String>,
        parent: ViewGroup,
    ) {
        binding = ViewDynamicSettingBinding.inflate(
            LayoutInflater.from(context),
            parent,
            true
        )
        binding.root.setOnClickListener {
            showVisibilityChangeDialog(
                element = element,
                context = parent.context,
                clsList = clsList,
                userSettings = userSettings,
                selectedName = binding.radioSelected.text.toString()
            )
        }

        binding.radioSelected.text =
            clsList.find { it.uref == userSettings[element.uref ?: element.defaultUref] }?.term
                ?: clsList.first().term
        binding.title.text = element.title
    }

    private fun showVisibilityChangeDialog(
        selectedName: String,
        element: SettingsElement,
        clsList: List<DefaultCls>,
        userSettings: MutableMap<String, String>,
        context: Context
    ) {
        val dialog = Dialog(context, R.style.Theme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_visibility_settings_type)
        dialog.findViewById<View>(R.id.okBtn).setOnClickListener {
            dialog.cancel()
        }
        dialog.findViewById<RadioGroup>(R.id.radioGroup)?.let { group ->
            group.removeAllViews()
            clsList.forEachIndexed { index, cls ->
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                group.addView(
                    createCustomRadioButton(
                        id = index,
                        context = group.context,
                        radioName = cls.term.orEmpty(),
                        uref = cls.uref.orEmpty(),
                        selected = selectedName == cls.term,
                        parentRadioGroup = group,
                        onSelectedRadio = {
                            binding.radioSelected.text = it.first
                            userSettings[element.uref ?: element.defaultUref.orEmpty()] = it.second
                        }
                    ),
                    layoutParams
                )
            }
        }
        dialog.setOnDismissListener {

        }
        dialog.show()
    }

    private fun createCustomRadioButton(
        id: Int,
        context: Context,
        radioName: String,
        selected: Boolean,
        uref: String,
        parentRadioGroup: RadioGroup,
        onSelectedRadio: (Pair<String, String>) -> Unit
    ): RadioButton {
        val inflater = LayoutInflater.from(context)
        val v: View = inflater.inflate(R.layout.view_settings_radio_button, null)
        val button = v.findViewById<View>(R.id.radioContainer) as RadioButton
        button.id = id
        button.text = radioName
        button.isChecked = selected
        button.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                onSelectedRadio(
                    Pair(radioName, uref)
                )
            }
        }
        return button
    }

}
