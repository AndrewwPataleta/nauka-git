package uddug.com.naukoteka.ui.fragments.profile.edit

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.utils.viewBinding
import uddug.com.naukoteka.databinding.FragmentProfileEditIdBinding
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditIdPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditIdView
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPlacementPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPlacementView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity

class ProfileEditPlacementFragment : BaseFragment(R.layout.fragment_profile_edit_id), ProfileEditPlacementView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    override val contentView by viewBinding(FragmentProfileEditIdBinding::bind)

    private var navigationView: ContainerNavigationView? = null

    @InjectPresenter
    lateinit var presenter: ProfileEditPlacementPresenter


    @ProvidePresenter
    fun providePresenter(): ProfileEditPlacementPresenter {
        return getScope().getInstance(ProfileEditPlacementPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        arguments?.getParcelable<UserProfileFullInfo>(ContainerActivity.PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }

    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {
        contentView.newAddress.setText(profileInfo.nickname.toString())
    }


}
