package uddug.com.naukoteka.ui.fragments.short_info_profile

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import uddug.com.domain.interactors.user_profile.model.ShortInfoUi
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentShortInfoProfileBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.short_info_profile.ShortInfoProfilePresenter
import uddug.com.naukoteka.presentation.short_info_profile.ShortInfoProfileView
import uddug.com.naukoteka.utils.viewBinding
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class ShortInfoProfileFragment : BaseFragment(R.layout.fragment_short_info_profile),
    ShortInfoProfileView {

    override val contentView by viewBinding(FragmentShortInfoProfileBinding::bind)

    @InjectPresenter
    lateinit var presenter: ShortInfoProfilePresenter

    @ProvidePresenter
    fun providePresenter(): ShortInfoProfilePresenter {
        return getScope().getInstance(ShortInfoProfilePresenter::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.run {
            ctiLink.doAfterTextChange {
                presenter.checkFreeNickname(it)
                checkValidFields()
            }
            ctiName.doAfterTextChange { checkValidFields() }
            ctiPatronymic.doAfterTextChange { checkValidFields() }
            ctiSurname.doAfterTextChange { checkValidFields() }
            btnCompleteRegistration.setOnClickListener {
                presenter.fillProfile(
                    ShortInfoUi(
                        surname = ctiSurname.text(),
                        name = ctiName.text(),
                        nickname = ctiLink.text(),
                        middleName = ctiPatronymic.text()
                    )
                )
            }
            cbWithoutPatronymic.setOnCheckedChangeListener { _, isChecked -> checkValidFields() }
        }
    }

    private fun checkValidFields() {
        contentView.run {
            val surname = ctiSurname.text()
            val name = ctiName.text()
            val patronymic = ctiPatronymic.text()
            val link = ctiLink.text()
            presenter.isValidFields(surname, name, patronymic, cbWithoutPatronymic.isChecked, link)
        }
    }

    override fun showButtonState(isEnabled: Boolean) {
        contentView.btnCompleteRegistration.isEnabled = isEnabled
    }

    override fun showNicknameAvailable(isAvailable: Boolean) {
        contentView.run {
            tvErrorNickname.isGone = isAvailable
            ctiLink.showIsChecked(isAvailable)
        }
        checkValidFields()
    }

    override fun showDefaultNickname(nickname: String) {
        contentView.ctiLink.setText(nickname)
        showNicknameAvailable(true)
    }

}