package uddug.com.naukoteka.presentation.profile.navigation

import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface ContainerView : MvpView {

    fun openEditFragment(profileFullInfo: UserProfileFullInfo)

    fun openPhotoView(profileFullInfo: UserProfileFullInfo)

    fun openBannerView(profileFullInfo: UserProfileFullInfo)

    fun openAppSettings(profileFullInfo: UserProfileFullInfo)

    fun openSupportWithHelp(profileFullInfo: UserProfileFullInfo)
}
