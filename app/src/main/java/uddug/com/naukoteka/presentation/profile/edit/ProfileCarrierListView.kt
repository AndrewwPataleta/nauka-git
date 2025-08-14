package uddug.com.naukoteka.presentation.profile.edit

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.Education
import uddug.com.domain.entities.profile.LaborActivities
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.presentation.profile.AcademicDegreeModel
import uddug.com.naukoteka.presentation.profile.edit.adapter.EducationAdapter

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfileCarrierListView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun setCarrierItems(carrier: List<LaborActivities>)
    fun showDeleteDialog(education: LaborActivities)
    fun showDetailScreen(
        profileInfo: UserProfileFullInfo,
        laborId: String?,
    )
}

