package uddug.com.naukoteka.presentation.profile

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.ProfileInfoModel
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(AddToEndSingleStrategy::class)
interface ProfileFullView : MvpView, LoadingView, InformativeView {

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo)

}
