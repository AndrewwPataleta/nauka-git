package uddug.com.naukoteka.ui.fragments.profile.settings

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileEditSettingsPersonalView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun setVisibilitySettings(visibilityType: VisibilityType, visibilityMode: VisibilityMode)
    fun showVisibilityChangeDialog(visibilityType: VisibilityType, visibilityMode: VisibilityMode)
}
