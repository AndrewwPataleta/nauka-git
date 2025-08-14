package uddug.com.naukoteka.ui.fragments.password_recovery

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.parseAsHtml
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentPasswordRecoveryBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.password_recovery.PasswordRecoveryPresenter
import uddug.com.naukoteka.presentation.password_recovery.PasswordRecoveryView
import uddug.com.naukoteka.ui.AppConfigs
import uddug.com.naukoteka.ui.adapters.password_requirements.PasswordRequirementsAdapter
import uddug.com.naukoteka.utils.BackButtonListener
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class PasswordRecoveryFragment : BaseFragment(R.layout.fragment_password_recovery),
    PasswordRecoveryView, BackButtonListener {

    private val FLIPPER_PASSWORD_RECOVERY = 0
    private val FLIPPER_PASSWORD_RECOVERY_VERIFICATION = 1
    private val FLIPPER_PASSWORD_RECOVERY_NEW_PASSWORD = 2

    private var dialog: AlertDialog? = null

    override val contentView by viewBinding(FragmentPasswordRecoveryBinding::bind)

    @InjectPresenter
    lateinit var presenter: PasswordRecoveryPresenter

    @ProvidePresenter
    fun providePresenter(): PasswordRecoveryPresenter {
        return getScope().getInstance(PasswordRecoveryPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("on view created ${javaClass.canonicalName}")
        contentView.run {
            ivBack.setOnClickListener { onBackPressed() }
            passwordRecovery.run {
                btnSend.setOnClickListener { presenter.recoveryPassword(ctiEmail.text()) }
                ctiEmail.doAfterTextChange { checkValidEmail() }
                ctiEmail.validFieldEditTextListener = { isValid ->
                    ctiEmail.showError(!isValid)
                    tvError.setText(R.string.error_email_format)
                    tvError.isGone = isValid
                }
            }
            passwordRecoveryNewPassword.run {
                ctiNewPass.doAfterTextChange { checkValidFields() }
                ctiNewPassConfirmation.doAfterTextChange { checkValidFields() }
                tvRequirements.setOnClickListener { showDialogRequirements() }
                btnSavePassword.setOnClickListener { presenter.changePassword(ctiNewPass.text()) }
            }
        }
    }

    override fun showTimer() {
        presenter.startTimerResendCode()
        contentView.viewFlipper.displayedChild = FLIPPER_PASSWORD_RECOVERY_VERIFICATION
    }

    override fun onBackPressed(): Boolean {
        contentView.run {
            when (viewFlipper.displayedChild) {
                FLIPPER_PASSWORD_RECOVERY -> presenter.exit()
                FLIPPER_PASSWORD_RECOVERY_NEW_PASSWORD -> startDialogCancelPasswordRecovery(
                    FLIPPER_PASSWORD_RECOVERY
                )
                FLIPPER_PASSWORD_RECOVERY_VERIFICATION -> startDialogCancelPasswordRecovery(
                    FLIPPER_PASSWORD_RECOVERY
                )
            }
        }
        return true
    }

    private fun checkValidEmail() {
        contentView.passwordRecovery.run {
            presenter.isValidEmail(ctiEmail.text())
        }
    }

    private fun checkValidFields() {
        contentView.passwordRecoveryNewPassword.run {
            val password = ctiNewPass.text()
            val passwordConfirmation = ctiNewPassConfirmation.text()
            presenter.isValidPasswords(password, passwordConfirmation)
        }
    }

    override fun showButtonState(isEnabled: Boolean) {
        contentView.passwordRecovery.btnSend.isEnabled = isEnabled
    }

    override fun showButtonStateOfPasswordRecoveryVerification(isEnabled: Boolean) {
        contentView.passwordRecoveryNewPassword.run {
            btnSavePassword.isEnabled = isEnabled
            ctiNewPass.showError(!isEnabled)
            ctiNewPassConfirmation.showError(!isEnabled)
        }
    }

    override fun showSecondsToResend(seconds: Int) {
        if (seconds > 0) {
            val minutes = seconds / 60
            val seconds = seconds - (minutes * 60)
            val secondsLeft = String.format("%02d:%02d", minutes, seconds)
            contentView.passwordRecoveryVerification.run {
                tvSendAgain.isVisible = true
                tvSendAgain.text =
                    getString(
                        R.string.password_recovery_verification_send_again,
                        secondsLeft
                    ).parseAsHtml()
                tvSendAgain.setOnClickListener { }
            }
        } else if (seconds == 0) {
            contentView.passwordRecoveryVerification.run {
                tvSendAgain.text =
                    getString(
                        R.string.password_recovery_verification_send_again, ""
                    ).parseAsHtml()
                tvSendAgain.setOnClickListener { presenter.recoveryPassword(contentView.passwordRecovery.ctiEmail.text()) }
            }
        }
    }

    fun checkTokenToRecovery(key: String) {
        presenter.checkKey(key)
    }

    override fun showInputNewPassword() {
        contentView.viewFlipper.displayedChild = FLIPPER_PASSWORD_RECOVERY_NEW_PASSWORD
    }

    private fun showDialogRequirements() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_password_requirements,
            ConstraintLayout(requireContext())
        )
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCancelable(true)
            .setView(dialogView)
            .create().apply {
                dialogView.findViewById<RecyclerView>(R.id.rv_requirements)?.adapter =
                    PasswordRequirementsAdapter().apply { setItems(AppConfigs.getPasswordRequirements()) }
                dialogView.findViewById<TextView>(R.id.btn_accessibly)
                    ?.setOnClickListener { dismiss() }
            }.show()
    }

    private fun startDialogCancelPasswordRecovery(viewFlipperIndex: Int) {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_cancel_password_recovery,
            ConstraintLayout(requireContext())
        )
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCancelable(true)
            .setView(dialogView)
            .create().apply {
                dialogView.findViewById<TextView>(R.id.tv_are_you_sure_interrupt_password_recovery)
                    ?.setText(R.string.are_you_sure_want_to_interrupt_the_password_recovery)
                dialogView.findViewById<TextView>(R.id.btn_yes)?.setOnClickListener {
                    presenter.toAuthorization()
                    dismiss()
                }
                dialogView.findViewById<TextView>(R.id.btn_no)?.setOnClickListener { dismiss() }
            }.show()
    }

    override fun startDialogPasswordRecoverySuccessful() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_successful_password_recovery,
            ConstraintLayout(requireContext())
        )
        dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setCancelable(true)
            .setView(dialogView)
            .create()
        dialog.apply {
            dialogView.findViewById<ImageView>(R.id.iv_success_password_recovery)
                .setImageResource(R.drawable.ic_success_password_recovery)
            dialogView.findViewById<TextView>(R.id.tv_new_password_save)
                ?.setText(R.string.dialog_password_recovery_new_password_save)
            dialogView.findViewById<TextView>(R.id.tv_password_recovery_successful)
                ?.setText(R.string.dialog_password_recovery_successful)
        }?.show()
    }

    override fun showErrorEmail(isValid: Boolean) {
        contentView.passwordRecovery.ctiEmail.showError(!isValid)
        contentView.passwordRecovery.tvError.setText(R.string.error_email_not_exist)
        contentView.passwordRecovery.tvError.isGone = isValid
        showButtonState(isValid)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        dialog = null
    }
}