package uddug.com.naukoteka.presentation.profile.edit

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(AddToEndSingleStrategy::class)
interface ProfileEditPersonalIdsView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)

    fun setSpinCode(spinCode: String)
    fun setOrchid(orchid: String)
    fun setReserch(reserched: String)
    fun userIdsSuccessUpdated()
    fun showIdsUpdatedSuccess()
}
