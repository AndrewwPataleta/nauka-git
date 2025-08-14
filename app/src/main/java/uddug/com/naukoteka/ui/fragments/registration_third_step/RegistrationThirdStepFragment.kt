package uddug.com.naukoteka.ui.fragments.registration_third_step

import android.os.Bundle
import android.view.View
import androidx.core.text.parseAsHtml
import androidx.core.view.isVisible
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentRegistrationThirdStepBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.registration_third_step.RegistrationThirdStepPresenter
import uddug.com.naukoteka.presentation.registration_third_step.RegistrationThirdStepView
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class RegistrationThirdStepFragment : BaseFragment(R.layout.fragment_registration_third_step),
    RegistrationThirdStepView {

    @InjectPresenter
    lateinit var presenter: RegistrationThirdStepPresenter

    @ProvidePresenter
    fun providePresenter(): RegistrationThirdStepPresenter {
        return getScope().getInstance(RegistrationThirdStepPresenter::class.java)
    }

    override val contentView by viewBinding(FragmentRegistrationThirdStepBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.tvSendLetter.setOnClickListener { presenter.startTimerResendCode() }
    }

    override fun showSecondsToResend(seconds: Int) {
        if (seconds > 0) {
            val minutes = seconds / 60
            val seconds = seconds - (minutes * 60)
            val secondsLeft = String.format("%02d:%02d", minutes, seconds)
            contentView.run {
                tvSendLetter.isVisible = true
                tvSendLetter.text =
                    getString(R.string.registration_send_letter, secondsLeft).parseAsHtml()
                tvSendLetter.setOnClickListener {  }
            }
        } else if (seconds == 0) {
            contentView.run {
                tvSendLetter.text =
                    getString(R.string.registration_send_letter, "").parseAsHtml()
                tvSendLetter.setOnClickListener { presenter.resendCode() }
            }
        }
    }

    fun checkTokenToRegistration(key: String) {
        presenter.checkKey(key)
    }
}