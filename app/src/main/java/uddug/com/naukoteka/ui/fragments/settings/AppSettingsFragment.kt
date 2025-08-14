package uddug.com.naukoteka.ui.fragments.settings

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.FormContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentAppSettingsBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.utils.viewBinding
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.DYNAMIC_SETTINGS_FORM
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.IMAGE_TYPE_BANNER
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.IMAGE_TYPE_PARAM
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.custom.DynamicSettingsNavigationView

class AppSettingsFragment : BaseFragment(R.layout.fragment_app_settings), AppSettingsView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    override val contentView by viewBinding(FragmentAppSettingsBinding::bind)

    @InjectPresenter
    lateinit var presenter: AppSettingsPresenter

    private var navigationView: ContainerNavigationView? = null


    @ProvidePresenter
    fun providePresenter(): AppSettingsPresenter {
        return getScope().getInstance(AppSettingsPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.securityContainer.setOnClickListener {
            presenter.askForOpenAppSettings()
        }
        contentView.blackListContainer.setOnClickListener {

        }
        contentView.systemSettingsTitle.setOnClickListener {
            presenter.askForOpenSystemSettings()

        }
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun openProfileSecure(profileInfo: UserProfileFullInfo) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileInfo)
        findNavController().navigate(R.id.profileSecure, bundle)
    }

    override fun setUserDynamicNavigationForms(formContainer: List<FormContainer>) {
        contentView.dynamicSettingsContainer.removeAllViews()
        formContainer.forEach { form ->
            DynamicSettingsNavigationView(requireContext()).attachDynamicNavigation(
                formContainer = form,
                parent = contentView.dynamicSettingsContainer,
                onFormNavigationClick = {
                    presenter.selectNavigationContainer(it)
                }
            )

        }
    }

    override fun openDynamicNavigationContainer(
        formContainer: FormContainer,
        userProfileFullInfo: UserProfileFullInfo
    ) {
        val bundle = bundleOf()
        bundle.putParcelable(PROFILE_ARGS, userProfileFullInfo)
        bundle.putSerializable(DYNAMIC_SETTINGS_FORM, formContainer)
        findNavController().navigate(
            R.id.dynamicSettingsFragment, bundle
        )
    }

    override fun openSystemSettings(profileInfo: UserProfileFullInfo) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileInfo)
        bundle.putString(IMAGE_TYPE_PARAM, IMAGE_TYPE_BANNER)
        findNavController().navigate(R.id.profileEditSettingsSystemFragment, bundle)
    }


}
