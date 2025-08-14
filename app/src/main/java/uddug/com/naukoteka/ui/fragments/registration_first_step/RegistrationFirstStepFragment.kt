package uddug.com.naukoteka.ui.fragments.registration_first_step

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentRegistrationFirstStepBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.registration_first_step.RegistrationFirstStepPresenter
import uddug.com.naukoteka.presentation.registration_first_step.RegistrationFirstStepView
import uddug.com.naukoteka.utils.BackButtonListener
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.naukoteka.core.deeplink.launchCustomTabsByUrl

class RegistrationFirstStepFragment : BaseFragment(R.layout.fragment_registration_first_step),
    RegistrationFirstStepView, BackButtonListener {

    override val contentView by viewBinding(FragmentRegistrationFirstStepBinding::bind)

    @InjectPresenter
    lateinit var presenter: RegistrationFirstStepPresenter

    @ProvidePresenter
    fun providePresenter(): RegistrationFirstStepPresenter {
        return getScope().getInstance(RegistrationFirstStepPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("on view created ${javaClass.canonicalName}")
        contentView.run {
            ctiEmail.validFieldEditTextListener = { isValid -> tvError.isGone = isValid }
            ivBack.setOnClickListener { onBackPressed() }
            ctiEmail.doAfterTextChange { checkValidField() }
            btnRegistrationNext.setOnClickListener { presenter.nextStep(ctiEmail.text()) }
            ibVkontakte.setOnClickListener { presenter.authSocial(SocialType.VK) }
            ibOk.setOnClickListener { presenter.authSocial(SocialType.OK) }
            ibFacebook.setOnClickListener { presenter.authSocial(SocialType.FACEBOOK) }
            ibMail.setOnClickListener { presenter.authSocial(SocialType.MAIL_RU) }
            ibGoogle.setOnClickListener { presenter.authSocial(SocialType.GOOGLE) }
            userAgreement.setOnClickListener {
                requireActivity().launchCustomTabsByUrl(
                    showTitle = true,
                    link = getString(R.string.eula_stage)
                )
            }
            userPrivacy.setOnClickListener {
                requireActivity().launchCustomTabsByUrl(
                    showTitle = true,
                    link = getString(R.string.policy_pdf)
                )
            }
            serviceRules.setOnClickListener {
                requireActivity().launchCustomTabsByUrl(
                    showTitle = true,
                    link = getString(R.string.service_rules_pdf_link)
                )
            }
        }
    }

    private fun checkValidField() {
        contentView.run {
            val email = ctiEmail.text()
            presenter.isValidField(email)
        }
    }

    override fun onBackPressed(): Boolean {
        presenter.exit()
        return true
    }

    override fun showButtonState(isEnabled: Boolean) {
        contentView.btnRegistrationNext.isEnabled = isEnabled
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

}