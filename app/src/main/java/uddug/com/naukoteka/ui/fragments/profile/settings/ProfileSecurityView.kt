package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileSecurityView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun openLoginPage()
    fun openLogoutDialog()
    fun openDeleteAccountDialog()
    fun showFrozenAccount()
    fun openChangePasswordDialog()
}
