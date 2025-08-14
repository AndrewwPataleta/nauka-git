package uddug.com.naukoteka.presentation.registration_third_step

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface RegistrationThirdStepView : MvpView, LoadingView, InformativeView {
    fun showSecondsToResend(seconds: Int)
}