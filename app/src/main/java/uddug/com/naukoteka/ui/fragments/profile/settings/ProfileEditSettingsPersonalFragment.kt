package uddug.com.naukoteka.ui.fragments.profile.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.widget.EditText
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditBinding
import uddug.com.naukoteka.databinding.FragmentProfileEditSettingsPersonalBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.DELETE_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.UPLOAD_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileEditPersonalInfoFragment.Companion.UPDATE_PROFILE_INFO
import uddug.com.naukoteka.utils.getHashCodeToString
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import uddug.com.naukoteka.utils.viewBinding


class ProfileEditSettingsPersonalFragment :
    BaseFragment(R.layout.fragment_profile_edit_settings_personal),
    ProfileEditSettingsPersonalView {

    override val contentView: FragmentProfileEditSettingsPersonalBinding by viewBinding(
        FragmentProfileEditSettingsPersonalBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileEditSettingsPersonalPresenter

    private var navigationView: ContainerNavigationView? = null

    private var pulseAnimation: Animation? = null

    @ProvidePresenter
    fun providePresenter(): ProfileEditSettingsPersonalPresenter {
        return getScope().getInstance(ProfileEditSettingsPersonalPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_edit_settings_personal, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.blockMainInfoContainer.setOnClickListener {
            presenter.askForEditVisibility(VisibilityType.BLOCK_MAIN_INFO)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {

    }

    override fun setVisibilitySettings(
        visibilityType: VisibilityType,
        visibilityMode: VisibilityMode
    ) {
        val visibilityModeValue = when (visibilityMode) {
            VisibilityMode.ALL -> getString(R.string.to_all_visibility)
            VisibilityMode.SUBS -> getString(R.string.to_subs_visibility)
            VisibilityMode.NO_ONE -> getString(R.string.to_nobody_visibilty)
        }
        when (visibilityType) {
            VisibilityType.BLOCK_MAIN_INFO -> {
                contentView.blockMainInfoValue.text = visibilityModeValue
            }
        }
    }

    override fun showVisibilityChangeDialog(
        visibilityType: VisibilityType,
        visibilityMode: VisibilityMode
    ) {
        val dialog = Dialog(requireActivity(), R.style.Theme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_visibility_settings_type)
        dialog.setOnDismissListener {

        }
        dialog.show()
    }


}
