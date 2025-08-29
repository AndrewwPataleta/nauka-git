package uddug.com.naukoteka.ui.fragments.profile.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileEditBinding
import uddug.com.naukoteka.databinding.FragmentProfileSecurityBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPresenter
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.activities.main.AuthActivity
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.DELETE_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.UPLOAD_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileEditPersonalInfoFragment.Companion.UPDATE_PROFILE_INFO
import uddug.com.naukoteka.utils.getHashCodeToString
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import uddug.com.naukoteka.utils.viewBinding


class ProfileSecurityFragment : BaseFragment(R.layout.fragment_profile_security),
    ProfileSecurityView {

    override val contentView: FragmentProfileSecurityBinding by viewBinding(
        FragmentProfileSecurityBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileSecurityPresenter

    private var navigationView: ContainerNavigationView? = null

    private var pulseAnimation: Animation? = null

    @ProvidePresenter
    fun providePresenter(): ProfileSecurityPresenter {
        return getScope().getInstance(ProfileSecurityPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_security, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(PROFILE_ARGS)
            ?.let { presenter.setProfileFullInfo(it) }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.actionExitProfile.setOnClickListener {
            presenter.selectExitProfile()
        }
        contentView.deleteAccountContainer.setOnClickListener {
            presenter.selectDeleteAccount()
        }
        contentView.btnChangePassword.setOnClickListener {
            presenter.selectChangePassword()
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
        contentView.mainPhone.text = profileInfo.phone
        contentView.mainEmailAddress.text = profileInfo.email
    }

    override fun openLoginPage() {
        val activity = requireActivity()
        activity.finishAffinity()
        activity.startActivity(Intent(activity, AuthActivity::class.java))
    }

    override fun openLogoutDialog() {
        val dialog = Dialog(requireActivity(), R.style.Theme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_exit_profile)
        (dialog.findViewById(R.id.cancelDeleteBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
        }
        (dialog.findViewById(R.id.deleteConfirmBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
            presenter.selectConfirmExit()
        }
        dialog.show()
    }

    override fun openDeleteAccountDialog() {
        val dialog = Dialog(requireActivity(), R.style.Theme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_delete_account)
        (dialog.findViewById(R.id.cancelDeleteBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
        }
        (dialog.findViewById(R.id.deleteConfirmBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
            presenter.selectConfirmDelete(dialog.findViewById<EditText>(R.id.reason).text.toString())
        }
        dialog.show()
    }

    override fun showFrozenAccount() {
        val dialog = Dialog(requireActivity(), R.style.Theme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val window: Window? = dialog.window
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_delete_account)
        (dialog.findViewById(R.id.cancelDeleteBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
        }
        (dialog.findViewById(R.id.deleteConfirmBtn) as? View)?.setOnClickListener {
            dialog.dismiss()
            presenter.selectConfirmDelete(dialog.findViewById<EditText>(R.id.reason).text.toString())
        }
        dialog.show()
    }

    override fun openChangePasswordDialog() {
        findNavController().navigate(R.id.profileSecurityPasswordChangeFragment)
    }

}
