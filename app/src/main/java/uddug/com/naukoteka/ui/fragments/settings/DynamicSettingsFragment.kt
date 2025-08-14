package uddug.com.naukoteka.ui.fragments.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonElement
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.FormContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.model.DefaultCls
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentDynamicSettingsBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.utils.viewBinding
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.DYNAMIC_SETTINGS_FORM
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.custom.DynamicSettingView

class DynamicSettingsFragment : BaseFragment(R.layout.fragment_dynamic_settings),
    uddug.com.naukoteka.ui.fragments.settings.DynamicSettingsView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    override val contentView by viewBinding(FragmentDynamicSettingsBinding::bind)

    @InjectPresenter
    lateinit var presenter: DynamicSettingsPresenter

    private var navigationView: ContainerNavigationView? = null


    @ProvidePresenter
    fun providePresenter(): DynamicSettingsPresenter {
        return getScope().getInstance(DynamicSettingsPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        arguments?.getSerializable(DYNAMIC_SETTINGS_FORM)
            ?.let {
                (it as? FormContainer)?.let { it1 -> presenter.setUserForm(it1) }
            }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.done.setOnClickListener {
            presenter.selectUpdateCurrentSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setDynamicSettingsForm(
        formContainer: FormContainer,
        clsList: List<DefaultCls>,
        settings: MutableMap<String, String>
    ) {
        contentView.toolbar.text = formContainer.title
        formContainer.elements?.forEach {
            DynamicSettingView(requireContext()).attachDynamicNavigation(
                element = it,
                clsList = clsList,
                userSettings = settings,
                parent = contentView.dynamicSettingsContainer,
            )
        }
    }

    override fun showToastUpdate(toastStatus: ToastStatus) {
        when (toastStatus) {
            ToastStatus.SUCCESS -> Toast.makeText(
                requireContext(),
                getString(R.string.updated_settings_successful), Toast.LENGTH_LONG
            ).show()

            ToastStatus.FAIL -> Toast.makeText(
                requireContext(),
                getString(R.string.updated_settings_fail), Toast.LENGTH_LONG
            ).show()
        }
    }

}
