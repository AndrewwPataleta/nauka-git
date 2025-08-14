package uddug.com.naukoteka.ui.fragments.profile.settings

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileChangePasswordView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun setUpdateButtonStatus(passwordButtonStatus: PasswordButtonStatus)
    fun setVisibilityNewPassword(visibility: Boolean)
    fun setVisibilityNewPasswordConfirm(visibility: Boolean)
    fun setVisibilityCurrentPasswordConfirm(visibility: Boolean)
    fun showPasswordUpdateToast()
    fun showPasswordUpdateFailToast()
}

enum class PasswordButtonStatus {
    ENABLED,
    DISABLED
}
