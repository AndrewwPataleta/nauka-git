package uddug.com.naukoteka.ui.fragments.profile.edit

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.DELETE_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.UPLOAD_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileEditPersonalInfoFragment.Companion.UPDATE_PROFILE_INFO
import uddug.com.naukoteka.utils.getHashCodeToString
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import uddug.com.naukoteka.utils.viewBinding

class ProfileEditFragment : BaseFragment(R.layout.fragment_profile_edit), ProfileEditView {

    override val contentView: FragmentProfileEditBinding by viewBinding(FragmentProfileEditBinding::bind)

    @InjectPresenter
    lateinit var presenter: ProfileEditPresenter

    private var navigationView: ContainerNavigationView? = null
    private var pulseAnimation: Animation? = null

    @ProvidePresenter
    fun providePresenter(): ProfileEditPresenter =
        getScope().getInstance(ProfileEditPresenter::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)?.let { presenter.setProfileFullInfo(it) }
        setupClickListeners()
        setupFragmentResultListeners()
    }

    private fun setupClickListeners() = with(contentView) {
        done.setOnClickListener { findNavController().popBackStack() }
        back.setOnClickListener { findNavController().popBackStack() }
        updateBackgroundHeader.setOnClickListener { presenter.selectUpdateProfileHeader() }
        profileImage.setOnClickListener { presenter.selectUpdateProfileImage() }
        nickNameContainer.setOnClickListener { presenter.selectEditProfileId() }
        profileBackgroundDelete.setOnClickListener { presenter.askForDeleteBanner() }
        personalInfoContainer.setOnClickListener { presenter.askForOpenUserData() }
        personalIdsContainer.setOnClickListener { presenter.askForOpenPersonalIds() }
        educationAchivmentContainer.setOnClickListener { presenter.askForOpenAcademicProfile() }
        educationQulContainer.setOnClickListener { presenter.askForOpenEducationInfo() }
        carrierContainer.setOnClickListener { presenter.askForOpenCarrierInfo() }
        addressesContainer.setOnClickListener { presenter.askForOpenAddressesList() }
        contactsContainer.setOnClickListener { presenter.askForOpenContactsEdit() }
        settingsTextContainer.setOnClickListener { presenter.askForOpenSettings() }
    }

    private fun setupFragmentResultListeners() {
        setFragmentResultListener(DELETE_AVATAR_RESULT) { _, _ -> presenter.loadProfile() }
        setFragmentResultListener(UPLOAD_AVATAR_RESULT) { _, _ -> presenter.loadProfile() }
        setFragmentResultListener(UPDATE_PROFILE_INFO) { _, _ -> presenter.loadProfile() }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setMainInformation(profileInfo: UserProfileFullInfo): Unit = with(contentView) {
        name.text = profileInfo.fullName
        profileImage.setOnClickListener { presenter.selectUpdateProfileImage() }
        profileImage.setImageDrawable(null)
        profileTopBackground.setImageDrawable(null)
        initials.isVisible = false

        profileInfo.bannerUrl?.let { banner ->
            profileTopBackground.load(withAnimation = false, model = BuildConfig.IMAGE_SERVER_URL + banner)
        }

        if (profileInfo.image?.path.isNotNullOrEmpty()) {
            profileInfo.image?.path?.let { path ->
                profileImage.load(withAnimation = false, model = BuildConfig.IMAGE_SERVER_URL + path)
            }
        } else {
            val gradientRes = when (getHashCodeToString(profileInfo.id, 8)) {
                0 -> R.drawable.background_gradient_one
                1 -> R.drawable.background_gradient_two
                2 -> R.drawable.background_gradient_three
                3 -> R.drawable.background_gradient_four
                4 -> R.drawable.background_gradient_five
                5 -> R.drawable.background_gradient_six
                6 -> R.drawable.background_gradient_seven
                7 -> R.drawable.background_gradient_eight
                else -> R.drawable.background_gradient_one
            }
            profileImage.background = resources.getDrawable(gradientRes)
            initials.text = (profileInfo.firstName?.firstOrNull()?.toString() ?: "") +
                    (profileInfo.lastName?.firstOrNull()?.toString() ?: "")
            initials.isVisible = true
        }
        profileInfo.nickname?.let { nick ->
            nickname.text = getString(R.string.naukotheka_ru_link, nick)
        }
    }

    override fun openProfileEditPhotoDialog(profileInfo: UserProfileFullInfo) {
        ProfileAvatarEditActionBottomSheetFragment.newInstance(
            profileInfo,
            ProfileAvatarActionPresenter.IMAGE_TYPE_AVATAR.AVATAR
        ).show(requireFragmentManager(), "")
    }

    override fun openProfileHeaderEditPhotoDialog(profileInfo: UserProfileFullInfo) {
        ProfileAvatarEditActionBottomSheetFragment.newInstance(
            profileInfo,
            ProfileAvatarActionPresenter.IMAGE_TYPE_AVATAR.BANNER
        ).show(requireFragmentManager(), "")
    }

    private fun navigateTo(destination: Int, profileInfo: UserProfileFullInfo) {
        val bundle = bundleOf(PROFILE_ARGS to profileInfo)
        findNavController().navigate(destination, bundle)
    }

    override fun openProfileEditPersonalData(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.profileEditPersonalData, profileInfo)
    }

    override fun openProfileEditPersonalIdsData(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.editProfilePersonalIds, profileInfo)
    }

    override fun openProfileAcademicDegreeEdit(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.profileEditAcademicDegreeFragment, profileInfo)
    }

    override fun openProfileEducationInfo(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.profileEditEducationFragment, profileInfo)
    }

    override fun openProfileCarrierInfo(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.profileCarrierList, profileInfo)
    }

    override fun openProfileAddressesEdit(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.profileEditAddressList, profileInfo)
    }

    override fun openProfileContactsEdit(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.profileEditContacts, profileInfo)
    }

    override fun openProfileSettings(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.appSettingsFragment, profileInfo)
    }

    override fun openEditProfileId(profileInfo: UserProfileFullInfo) {
        navigateTo(R.id.profileEditId, profileInfo)
    }

    override fun askForDeleteBanner() {
        Dialog(requireActivity(), R.style.Theme_Dialog).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setContentView(R.layout.dialog_remove_banner)
            findViewById<View>(R.id.cancelDeleteBtn)?.setOnClickListener { dismiss() }
            findViewById<View>(R.id.deleteConfirmBtn)?.setOnClickListener {
                dismiss()
                presenter.confirmDeleteBanner()
            }
        }.show()
    }
}
