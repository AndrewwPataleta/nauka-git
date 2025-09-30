package uddug.com.naukoteka.presentation.profile.edit

import androidx.annotation.StringRes
import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(AddToEndSingleStrategy::class)
interface ProfileEditPersonalInfoView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun setMaxInputRange(maxDefault: Int, maxDescription: Int)
    fun updateLengthInputs(maxDefault: Int, maxDescription: Int)
    fun setGenders(@StringRes genres: List<Int>, selectedPos: Int)
    fun profileSuccessfulUpdate()
}
