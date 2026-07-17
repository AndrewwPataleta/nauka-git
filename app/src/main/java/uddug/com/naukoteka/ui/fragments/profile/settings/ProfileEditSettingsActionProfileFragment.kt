package uddug.com.naukoteka.ui.fragments.profile.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditSettingsActionBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.utils.viewBinding


class ProfileEditSettingsActionProfileFragment :
    BaseFragment(R.layout.fragment_profile_edit_settings_action),
    ProfileEditSettingsActionProfileView {

    override val contentView: FragmentProfileEditSettingsActionBinding by viewBinding(
        FragmentProfileEditSettingsActionBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileEditSettingsActionProfilePresenter

    private var navigationView: ContainerNavigationView? = null

    @ProvidePresenter
    fun providePresenter(): ProfileEditSettingsActionProfilePresenter {
        return getScope().getInstance(ProfileEditSettingsActionProfilePresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.back.setOnClickListener { findNavController().popBackStack() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {}
}
