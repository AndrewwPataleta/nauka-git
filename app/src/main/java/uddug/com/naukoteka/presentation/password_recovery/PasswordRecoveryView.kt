package uddug.com.naukoteka.presentation.password_recovery

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface PasswordRecoveryView : MvpView, LoadingView, InformativeView {

    fun showButtonState(isEnabled: Boolean)

    fun showSecondsToResend(seconds: Int)

    fun showButtonStateOfPasswordRecoveryVerification(isEnabled: Boolean)
    fun showTimer()
    fun showInputNewPassword()
    fun startDialogPasswordRecoverySuccessful()
    fun showErrorEmail(isValid: Boolean)

}