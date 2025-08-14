package uddug.com.naukoteka.ui.fragments.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.franmontiel.localechanger.LocaleChanger
import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentLoginBinding
import uddug.com.naukoteka.ext.data.AR_LOCALE
import uddug.com.naukoteka.ext.data.EN_LOCALE
import uddug.com.naukoteka.ext.data.RU_LOCALE
import uddug.com.naukoteka.ext.data.SUPPORTED_LOCALES_CUSTOM
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.login.LoginPresenter
import uddug.com.naukoteka.presentation.login.LoginView
import uddug.com.naukoteka.ui.custom.square_toast.SquareToast
import uddug.com.naukoteka.ui.custom.square_toast.ToastInfo
import uddug.com.naukoteka.utils.BackButtonListener
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.ui.activities.main.ContainerActivity

class LoginFragment : BaseFragment(R.layout.fragment_login), LoginView, BackButtonListener {


    override val contentView by viewBinding(FragmentLoginBinding::bind)

    @InjectPresenter
    lateinit var presenter: LoginPresenter

    @ProvidePresenter
    fun providePresenter(): LoginPresenter {
        return getScope().getInstance(LoginPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.activity = requireActivity()
        contentView.run {
            tvRegistration.setOnClickListener { presenter.toRegistration() }
            ctiEmail.validFieldFocusListener = { isValid -> showIsValidEmail(isValid) }
            ctiEmail.doAfterTextChange { checkValidFields() }
            ctiPass.doAfterTextChange { checkValidFields() }
            btnEnter.setOnClickListener { presenter.enter(ctiEmail.text(), ctiPass.text(), contentView.cbRememberMe.isChecked) }
            if (BuildConfig.DEBUG){
                btnEnter.setOnLongClickListener { presenter.enterTest(); true }
            }
            ibVkontakte.setOnClickListener { presenter.authSocial(SocialType.VK) }
            ibOk.setOnClickListener { presenter.authSocial(SocialType.OK) }
            ibFacebook.setOnClickListener { presenter.authSocial(SocialType.FACEBOOK) }
            ibMail.setOnClickListener { presenter.authSocial(SocialType.MAIL_RU) }
            ibGoogle.setOnClickListener { presenter.authSocial(SocialType.GOOGLE) }
            tvForgetPassword.setOnClickListener { presenter.toPasswordRecovery() }
            llLanguage.setOnClickListener {
                showPopupMenu(it)
                ivArrowDown.setImageResource(R.drawable.ic_arrow_up)
            }
            tvLanguage.text =
                SUPPORTED_LOCALES_CUSTOM.find { LocaleChanger.getLocale() == it.locale }?.languageName
        }
    }

    override fun onPause() {
        contentView.ctiEmail.validFieldFocusListener = null
        super.onPause()
    }

    private fun checkValidFields() {
        contentView.run {
            val email = ctiEmail.text()
            val password = ctiPass.text()
            presenter.isValidFields(email, password)
        }
    }

    override fun showButtonState(isEnabled: Boolean) {
        contentView.btnEnter.isEnabled = isEnabled
    }

    override fun showSuccessLogin() {
        startActivity(Intent(requireContext(), ContainerActivity::class.java))
    }

    private fun showIsValidEmail(isValid: Boolean){
        contentView.tvError.setText(R.string.error_email_format)
        val needToHideError = isValid || contentView.ctiEmail.text().isEmpty()
        contentView.tvError.isGone = needToHideError
        contentView.ctiEmail.showError(!needToHideError)
    }

    override fun showErrorCredentials() {
        showMessage(
            ToastInfo(
                text = getString(R.string.error_incorrect_credentials),
                type = SquareToast.Type.ERROR
            )
        )
        contentView.btnEnter.isEnabled = false
    }

    override fun showSocialTypes(socialTypes: List<SocialType>) {
        contentView.run {
            ibVkontakte.isVisible = socialTypes.contains(SocialType.VK)
            ibOk.isVisible = socialTypes.contains(SocialType.OK)
            ibFacebook.isVisible = socialTypes.contains(SocialType.FACEBOOK)
            ibMail.isVisible = socialTypes.contains(SocialType.MAIL_RU)
            ibGoogle.isVisible = socialTypes.contains(SocialType.GOOGLE)
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.exit()
        return true
    }

    private fun showPopupMenu(v: View): PopupWindow = contentView.run {
        var child = LayoutInflater.from(requireContext()).inflate(R.layout.popup_languages, null)
        val popupWindow = PopupWindow(requireContext())
        with(popupWindow) {
            contentView = child
            setBackgroundDrawable(null)
            elevation = 12F
            isFocusable = true
            isOutsideTouchable = true
            showAsDropDown(v, 0, -20)
            setOnDismissListener {
                ivArrowDown.setImageResource(R.drawable.ic_arrow_down)
                tvLanguage.setTextColor(resources.getColor(R.color.text_inactive))
            }
            tvLanguage.setTextColor(resources.getColor(R.color.white))
            with(child) {

                findViewById<TextView>(R.id.tv_english).apply {
                }.setOnClickListener {
                    presenter.onLanguageChange(EN_LOCALE)
                    dismiss()
                }
                findViewById<TextView>(R.id.tv_arab).apply {
                }.setOnClickListener {
                    presenter.onLanguageChange(AR_LOCALE)
                    dismiss()
                }
                findViewById<TextView>(R.id.tv_russian).apply {
                }.setOnClickListener {
                    presenter.onLanguageChange(RU_LOCALE)
                    dismiss()
                }
            }
        }
        return popupWindow
    }

    override fun showLanguages(locales: List<CustomLanguage>) {

    }
}
