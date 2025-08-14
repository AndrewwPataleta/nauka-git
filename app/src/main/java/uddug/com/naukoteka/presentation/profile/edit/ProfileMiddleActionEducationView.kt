package uddug.com.naukoteka.presentation.profile.edit

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileMiddleActionEducationView : MvpView, LoadingView, InformativeView {
    fun setEducationItems(educations: List<Education>)
    fun showDeleteDialog(education: Education)
    fun showDetailScreen(profileInfo: UserProfileFullInfo, educationId: String?, type: EducationScreenType)
    fun showAddNewEducation(profileInfo: UserProfileFullInfo, type: EducationScreenType)
}
