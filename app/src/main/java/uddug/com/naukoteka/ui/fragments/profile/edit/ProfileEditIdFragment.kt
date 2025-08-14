package uddug.com.naukoteka.ui.fragments.profile.edit

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity

class ProfileEditIdFragment : BaseFragment(R.layout.fragment_profile_edit_id), ProfileEditIdView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    override val contentView by viewBinding(FragmentProfileEditIdBinding::bind)

    private var navigationView: ContainerNavigationView? = null

    @InjectPresenter
    lateinit var presenter: ProfileEditIdPresenter


    @ProvidePresenter
    fun providePresenter(): ProfileEditIdPresenter {
        return getScope().getInstance(ProfileEditIdPresenter::class.java)
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
        contentView.done.setOnClickListener {
            presenter.updateCurrentUserId(contentView.newAddress.text.toString())
        }
        arguments?.getParcelable<UserProfileFullInfo>(ContainerActivity.PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }

        contentView.newAddress.addTextChangedListener {
            presenter.checkFreeNickname(it.toString())
        }

    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {
        contentView.newAddress.setText(profileInfo.nickname.toString())
    }

    override fun showNicknameAvailable(isAvailable: Boolean) {
        if (isAvailable) {
            contentView.editIdSuccess.isVisible = true
            contentView.editIdError.isVisible = false
        } else {
            contentView.editIdSuccess.isVisible = false
            contentView.editIdError.isVisible = true
        }
    }

    override fun showUpdatedDone() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.id_has_been_updated), Toast.LENGTH_LONG
        ).show()
    }


}
