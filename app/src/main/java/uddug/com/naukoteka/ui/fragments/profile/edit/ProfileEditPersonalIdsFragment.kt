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
import uddug.com.naukoteka.databinding.FragmentProfileEditPersonalIdsBinding
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditIdPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditIdView
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalIdsPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPersonalIdsView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity

class ProfileEditPersonalIdsFragment : BaseFragment(R.layout.fragment_profile_edit_personal_ids),
    ProfileEditPersonalIdsView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    override val contentView by viewBinding(FragmentProfileEditPersonalIdsBinding::bind)

    private var navigationView: ContainerNavigationView? = null

    val re = Regex("[^A-Za-z0-9 ]")

    @InjectPresenter
    lateinit var presenter: ProfileEditPersonalIdsPresenter


    @ProvidePresenter
    fun providePresenter(): ProfileEditPersonalIdsPresenter {
        return getScope().getInstance(ProfileEditPersonalIdsPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(ContainerActivity.PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }

        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.done.setOnClickListener {
            presenter.selectUpdateUserIds()
        }
        contentView.spinCodeValue.addTextChangedListener {
            presenter.setCurrentSpinCode(it.toString())
        }
        contentView.orchidCodeValue.addTextChangedListener {
            presenter.setCurrentOrchid(it.toString())
        }
        contentView.researcerCodeValue.addTextChangedListener {
            presenter.setCurrentReserchId(it.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {

    }

    override fun setSpinCode(spinCode: String) {
        contentView.spinCodeValue.setText(re.replace(spinCode, ""))
    }

    override fun setOrchid(orchid: String) {
        contentView.orchidCodeValue.setText(re.replace(orchid, ""))
    }

    override fun setReserch(reserched: String) {
        contentView.researcerCodeValue.setText(
            re.replace(reserched, "")
        )
    }

    override fun userIdsSuccessUpdated() {

    }

    override fun showIdsUpdatedSuccess() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.data_updated_success), Toast.LENGTH_LONG
        ).show()
    }


}
