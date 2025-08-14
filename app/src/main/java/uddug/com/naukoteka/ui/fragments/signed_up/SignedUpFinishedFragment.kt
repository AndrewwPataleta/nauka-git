package uddug.com.naukoteka.ui.fragments.signed_up

import android.os.Bundle
import android.view.View
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentSignUpFinishedBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.signed_up.SignedUpFinishedPresenter
import uddug.com.naukoteka.presentation.signed_up.SignedUpView
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class SignedUpFinishedFragment : BaseFragment(R.layout.fragment_sign_up_finished),
    SignedUpView {

    override val contentView by viewBinding(FragmentSignUpFinishedBinding::bind)

    @InjectPresenter
    lateinit var presenter: SignedUpFinishedPresenter

    @ProvidePresenter
    fun providePresenter(): SignedUpFinishedPresenter {
        return getScope().getInstance(SignedUpFinishedPresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.run {
            tvSkip.setOnClickListener { presenter.skip() }
            btnFillInProfile.setOnClickListener { presenter.fillProfile() }
        }
    }

}