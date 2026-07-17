package uddug.com.naukoteka.ui.fragments.profile.settings

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileEditSettingsPersonalView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun setVisibilitySettings(visibilityType: VisibilityType, visibilityMode: VisibilityMode)
    fun showVisibilityChangeDialog(visibilityType: VisibilityType, visibilityMode: VisibilityMode)
}
