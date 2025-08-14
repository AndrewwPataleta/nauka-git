package uddug.com.naukoteka.presentation.profile.edit

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.presentation.profile.AcademicDegreeModel

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileEditEducationContainerView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun setNavigationScreen( typeScreen: EducationScreenType)

}

enum class EducationScreenType {
    MIDDLE,
    HIGH,
    ADDITIONAL
}
