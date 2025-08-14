package uddug.com.naukoteka.ui.fragments.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileAdditionalActionBinding
import uddug.com.naukoteka.global.base.BaseBottomSheetDialogFragment
import uddug.com.naukoteka.presentation.profile.navigation.ContainerView
import uddug.com.naukoteka.presentation.profile.ProfileAdditionalActionPresenter
import uddug.com.naukoteka.presentation.profile.ProfileAdditionalActionView
import uddug.com.naukoteka.ui.activities.main.AuthActivity
import uddug.com.naukoteka.utils.copyToClipboard
import uddug.com.naukoteka.utils.viewBinding


class ProfileAdditionalActionBottomSheetFragment : BaseBottomSheetDialogFragment(),
    ProfileAdditionalActionView {

    private lateinit var mBehavior: BottomSheetBehavior<FrameLayout>

    private var containerNavigation: ContainerView? = null

    @InjectPresenter
    lateinit var presenter: ProfileAdditionalActionPresenter

    @ProvidePresenter
    fun providePresenter(): ProfileAdditionalActionPresenter {
        return getScope().getInstance(ProfileAdditionalActionPresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.containerNavigation = requireActivity() as ContainerView
    }

    companion object {

        private const val PROFILE_FULL_INFO_ARGS = "PROFILE_FULL_INFO_ARGS"

        fun newInstance(profileFullInfo: UserProfileFullInfo): ProfileAdditionalActionBottomSheetFragment {
            return ProfileAdditionalActionBottomSheetFragment().apply {
                arguments = bundleOf(PROFILE_FULL_INFO_ARGS to profileFullInfo)
            }
        }
    }

    override val contentView: FragmentProfileAdditionalActionBinding by viewBinding(
        FragmentProfileAdditionalActionBinding::bind
    )

    override fun getTheme(): Int = R.style.NauDSBottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        BottomSheetDialog(requireContext(), theme)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_FULL_INFO_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        return inflater.inflate(R.layout.fragment_profile_additional_action, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.linkToProfileContainer.setOnClickListener {
            presenter.selectCopyToClipboard()
        }
        contentView.editProfileContainer.setOnClickListener {
            presenter.selectShowProfileEdit()
            dismiss()
        }
        contentView.editFromApp.setOnClickListener {
            startActivity(Intent(requireActivity(), AuthActivity::class.java))
        }
        contentView.settingsAppContainer.setOnClickListener {
            presenter.selectOpenAppSettings()
        }
        contentView.helpWithSupport.setOnClickListener {
            presenter.selectOpenHelpWithSupport()

        }
    }

    override fun copyToClipboardAndClose(info: String) {
        requireActivity().copyToClipboard(getString(R.string.naukotheka_ru_link, info))
        Toast.makeText(
            requireActivity(),
            getString(R.string.link_to_profile_has_been_copied),
            Toast.LENGTH_LONG
        ).show()
        dismiss()
    }

    override fun openProfileEditFragment(userProfileFullInfo: UserProfileFullInfo) {
        containerNavigation?.openEditFragment(userProfileFullInfo)
    }

    override fun openProfilePhotoImageView(userProfileFullInfo: UserProfileFullInfo) {
        dismiss()
        containerNavigation?.openPhotoView(userProfileFullInfo)
    }

    override fun openAppProfileSettings(userProfileFullInfo: UserProfileFullInfo) {
        dismiss()
        containerNavigation?.openAppSettings(userProfileFullInfo)
    }

    override fun helpWithSupport(userProfileFullInfo: UserProfileFullInfo) {
        dismiss()
        containerNavigation?.openSupportWithHelp(userProfileFullInfo)
    }

}
