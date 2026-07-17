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
import uddug.com.naukoteka.databinding.FragmentProfileEditBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.utils.viewBinding


class ProfileEditSettingsFragment : BaseFragment(R.layout.fragment_app_settings), ProfileAppSettingsView {

    override val contentView: FragmentProfileEditBinding by viewBinding(
        FragmentProfileEditBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileAppSettingsPresenter

    private var navigationView: ContainerNavigationView? = null

    @ProvidePresenter
    fun providePresenter(): ProfileAppSettingsPresenter {
        return getScope().getInstance(ProfileAppSettingsPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
