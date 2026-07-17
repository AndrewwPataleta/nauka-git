package uddug.com.naukoteka.presentation.short_info_profile

import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView

@StateStrategyType(AddToEndSingleStrategy::class)
interface ShortInfoProfileView : MvpView, LoadingView, InformativeView {
    fun showButtonState(isEnabled: Boolean)
    fun showNicknameAvailable(isAvailable: Boolean)
    fun showDefaultNickname(nickname: String)
}
