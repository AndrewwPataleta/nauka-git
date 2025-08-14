package uddug.com.naukoteka.ui.fragments.profile.settings

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfileChangePasswordBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.edit.ProfileChangePasswordView
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditPresenter
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.utils.getColorCompat
import uddug.com.naukoteka.utils.viewBinding


class ProfileSecurityPasswordChangeFragment :
    BaseFragment(R.layout.fragment_profile_change_password),
    uddug.com.naukoteka.ui.fragments.profile.settings.ProfileChangePasswordView {

    override val contentView: FragmentProfileChangePasswordBinding by viewBinding(
        FragmentProfileChangePasswordBinding::bind
    )

    @InjectPresenter
    lateinit var presenter: ProfileChangePasswordPresenter

    private var navigationView: ContainerNavigationView? = null

    private var pulseAnimation: Animation? = null

    @ProvidePresenter
    fun providePresenter(): ProfileChangePasswordPresenter {
        return getScope().getInstance(ProfileChangePasswordPresenter::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_change_password, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.btnChangePassword.setOnClickListener {
            presenter.selectChangePassword()
        }
        contentView.newPasswordFirst.addTextChangedListener {
            presenter.setNewPassword(it.toString())
        }
        contentView.newPasswordConfirmValue.addTextChangedListener {
            presenter.setNewPasswordConfirm(it.toString())
        }
        contentView.currentPassword.addTextChangedListener {
            presenter.setCurrentPassword(it.toString())
        }
        contentView.showNewPassword.setOnClickListener {
            presenter.onNewPasswordVisibilitySelect()
        }
        contentView.showConfirmNewPassword.setOnClickListener {
            presenter.onNewPasswordConfirmVisibilitySelect()
        }
        contentView.newPasswordFirst.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        contentView.newPasswordConfirmValue.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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

    override fun setUpdateButtonStatus(passwordButtonStatus: PasswordButtonStatus) {
        when (passwordButtonStatus) {
            PasswordButtonStatus.ENABLED -> {
                contentView.btnChangePassword.backgroundTintList =
                    ColorStateList.valueOf(requireContext().getColorCompat(R.color.text_link))
            }

            PasswordButtonStatus.DISABLED -> {
                contentView.btnChangePassword.backgroundTintList =
                    ColorStateList.valueOf(requireContext().getColorCompat(R.color.password_button_disabled_color))
            }
        }
    }

    override fun setVisibilityNewPassword(visibility: Boolean) {
        if (!visibility) {
            contentView.newPasswordFirst.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            contentView.newPasswordFirst.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        contentView.newPasswordFirst.setSelection(contentView.newPasswordFirst.text.length);
    }

    override fun setVisibilityNewPasswordConfirm(visibility: Boolean) {
        if (!visibility) {
            contentView.newPasswordConfirmValue.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            contentView.newPasswordConfirmValue.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        contentView.newPasswordConfirmValue.setSelection(contentView.newPasswordConfirmValue.text.length);
    }

    override fun setVisibilityCurrentPasswordConfirm(visibility: Boolean) {
        if (!visibility) {
            contentView.currentPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            contentView.currentPassword.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
        contentView.currentPassword.setSelection(contentView.currentPassword.text.length);
    }

    override fun showPasswordUpdateFailToast() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.password_not_updated),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun showPasswordUpdateToast() {
        Toast.makeText(
            requireActivity(),
            getString(R.string.password_has_been_updated),
            Toast.LENGTH_LONG
        ).show()
    }

}
